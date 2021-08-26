package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.util.Grid;
import com.cas.rendering.util.Range;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * This class represents a curve: 2 dimensional function with y dependent on x
 *
 * @author Abd-El-Aziz Zayed
 */
public class Curve extends Plot {

    private static final int MAX_RESOLUTION = 10000; // the resolution of the line

    /*
     * the number of floats needed to represent the data (size) and the buffer to
     * store the data
     */
    private static final FloatBuffer buffer;

    static {
        buffer = BufferUtils.createFloatBuffer(MAX_RESOLUTION * 2);
    }

    public Curve(Equation eq, BufferedImage image, boolean visible) {
        super(eq, image, 2, visible);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void update(Grid grid, HashMap<Character, Double> varValues) {
        if (!visible)
            return;

        double dx = grid.getX().getLength() / MAX_RESOLUTION;
        double xmin = grid.getX().getMin();

        int i;
        double x;
        int size = buffer.capacity();
        for (i = 0, x = xmin; i < size /* && x <= xmax */; i += 2, x += dx) {
            double y = eval(x, varValues);

            Range yRange = grid.getY();
            if (!yRange.inRange(eval(x - dx, varValues)) && !yRange.inRange(y)
                    && !yRange.inRange(eval(x + dx, varValues)))
                y = Float.NaN;

            buffer.put((float) x);
            buffer.put((float) y);
        }
        buffer.flip();

        render();
    }

    @Override
    protected void drawModel() {
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, buffer);
        GL15.glDrawArrays(GL15.GL_LINE_STRIP, 0, MAX_RESOLUTION);
//		glDrawArrays(GL_LINES, 0, MAX_RESOLUTION);
//		glDrawArrays(GL_LINES, 1, MAX_RESOLUTION - 1);
    }

    /**
     * evaluate the value of the function at the following values
     *
     * @param x         - value of x
     * @param varValues - value for all parameters
     * @return the value of y, or the evaluation of the function at the given values
     */
    private double eval(double x, HashMap<Character, Double> varValues) {
        varValues.put('x', x);
        return equation.valueAt(varValues);
    }
}
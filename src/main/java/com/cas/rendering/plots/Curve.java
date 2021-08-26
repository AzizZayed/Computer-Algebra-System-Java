package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.util.Grid;
import com.cas.rendering.util.Range;
import org.lwjgl.BufferUtils;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_LINE_STRIP;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;

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
    private static FloatBuffer buffer;

    static {
        buffer = BufferUtils.createFloatBuffer(MAX_RESOLUTION * 2);
    }

    public Curve(Equation eq, BufferedImage image, boolean visible) {
        super(eq, image, 2, visible);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
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
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glDrawArrays(GL_LINE_STRIP, 0, MAX_RESOLUTION);
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
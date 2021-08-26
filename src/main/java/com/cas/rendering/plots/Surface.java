package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.util.Grid;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * This class represents a surface: 3 dimensional function with z dependent on x
 * and y
 *
 * @author Abd-El-Aziz Zayed
 */
public class Surface extends Plot {

    public static final int MAX_RESOLUTION = 120;

    /*
     * the buffer to store the data
     */
    private static final FloatBuffer buffer;

    static {
        buffer = BufferUtils.createFloatBuffer((MAX_RESOLUTION + 1) * MAX_RESOLUTION * 6);
    }

    public Surface(Equation eq, BufferedImage image, boolean visible) {
        super(eq, image, 3, visible);
    }

    @Override
    public void update(Grid grid, HashMap<Character, Double> varValues) {
        if (!visible)
            return;

        double dx, dy;
        double xmin = grid.getX().getMin();
        double ymin = grid.getY().getMin();

        dx = grid.getX().getLength() / MAX_RESOLUTION;
        dy = grid.getY().getLength() / MAX_RESOLUTION;

        int i, j;
        double x, y;
        for (y = ymin, j = 0; j < MAX_RESOLUTION; j++, y += dy) {
            for (x = xmin, i = 0; i <= MAX_RESOLUTION; i++, x += dx) {
                double z = eval(x, y, varValues);

                buffer.put((float) x);
                buffer.put((float) y);
                buffer.put((float) z);

                double yNext = y + dy;

                buffer.put((float) x);
                buffer.put((float) yNext);
                buffer.put((float) eval(x, yNext, varValues));
            }
        }
        buffer.flip();

        render();
    }

    @Override
    protected void drawModel() {
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glPolygonMode(GL15.GL_FRONT_AND_BACK, GL15.GL_LINE);
        int count = MAX_RESOLUTION * 2;
        for (int i = 0; i < MAX_RESOLUTION; i++) {
            GL15.glDrawArrays(GL15.GL_TRIANGLE_STRIP, (count + 2) * i, count);
        }
        GL15.glPolygonMode(GL15.GL_FRONT_AND_BACK, GL15.GL_FILL);
    }

    /**
     * evaluate the value of the function at the following values
     *
     * @param x         - value of x
     * @param y         - value of y
     * @param varValues - value for all parameters
     * @return the value of z, or the evaluation of the function at the given values
     */
    private double eval(double x, double y, HashMap<Character, Double> varValues) {
        varValues.put('x', x);
        varValues.put('y', y);
        return equation.valueAt(varValues);
    }
}
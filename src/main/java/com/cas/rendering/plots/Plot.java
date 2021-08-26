package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.gui.Texture;
import com.cas.rendering.util.Grid;
import net.jafama.FastMath;
import org.lwjgl.opengl.GL15;

import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * This class represents a plot and all the data common between all types of
 * plots
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class Plot {

    private final float[] color; // the color of the plot
    private final Texture texture; // the texture for the equation of the plot
    private final int vertexCount; // the number of coorfinates each vertex
    protected Equation equation; // the function of the plot
    protected boolean visible; // if the plot is visible
    protected int vbo; // the GPU buffer to carry the data

    /*
     * constructor
     */
    public Plot(Equation eq, BufferedImage image, int vertexCount, boolean visible) {
        equation = eq;
        color = new float[]{(float) FastMath.random(), (float) FastMath.random(), (float) FastMath.random(), 1f};
        texture = new Texture(image);
        this.vertexCount = vertexCount;
        this.visible = visible;
        vbo = GL15.glGenBuffers();
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the function
     */
    public Equation getEquation() {
        return equation;
    }

    /**
     * @return the color
     */
    public float[] getColor() {
        return color;
    }

    /**
     * @return the texture
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * cleanup the memory allocated by OpenGL
     */
    public void cleanup() {
        GL15.glDeleteBuffers(vbo);
        texture.cleanup();
    }

    /**
     * preparation to render the plot and unbinding afterwards
     */
    public void render() {
        GL15.glEnableClientState(GL15.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glVertexPointer(vertexCount, GL15.GL_FLOAT, 0, 0);
        GL15.glColor4d(color[0], color[1], color[2], color[3]);
        drawModel();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDisableClientState(GL15.GL_VERTEX_ARRAY);
    }

    /**
     * render the plot model
     */
    protected abstract void drawModel();

    /**
     * update the date for the plot
     *
     * @param grid      - The coordinates system to generate data from
     * @param varValues - the parameter values
     */
    public abstract void update(Grid grid, HashMap<Character, Double> varValues);
}
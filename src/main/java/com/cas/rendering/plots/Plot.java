package com.cas.rendering.plots;

import com.cas.core.Equation;
import com.cas.rendering.gui.Texture;
import com.cas.rendering.util.Grid;
import net.jafama.FastMath;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor4d;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 * This class represents a plot and all the data common between all types of
 * plots
 *
 * @author Abd-El-Aziz Zayed
 */
public abstract class Plot {

    protected Equation equation; // the function of the plot
    private float[] color; // the color of the plot
    private Texture texture; // the texture for the equation of the plot
    protected boolean visible; // if the plot is visible
    protected int vbo; // the GPU buffer to carry the data
    private int vertexCount; // the number of coorfinates each vertex

    /*
     * constructor
     */
    public Plot(Equation eq, BufferedImage image, int vertexCount, boolean visible) {
        equation = eq;
        color = new float[]{(float) FastMath.random(), (float) FastMath.random(), (float) FastMath.random(), 1f};
        texture = new Texture(image);
        this.vertexCount = vertexCount;
        this.visible = visible;
        vbo = glGenBuffers();
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
        glDeleteBuffers(vbo);
        texture.cleanup();
    }

    /**
     * preparation to render the plot and unbinding afterwards
     */
    public void render() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glVertexPointer(vertexCount, GL_FLOAT, 0, 0);
        glColor4d(color[0], color[1], color[2], color[3]);
        drawModel();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDisableClientState(GL_VERTEX_ARRAY);
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
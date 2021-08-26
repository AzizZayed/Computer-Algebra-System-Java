package com.cas.rendering.util;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex3d;

/**
 * This class represents the coordinate system in which we draw the plots.
 *
 * @author Abd-El-Aziz Zayed
 */
public class Grid {

    private static final double DRAG_DAMPENER = 1000d;
    private static final double ZOOM_DAMPENER = 300d;

    private Range x, y, z; // range in x and y direction
    private double xRotation = 0, zRotation = 0; // rotation values

    /*
     * constructor with the all the intervals
     */
    public Grid(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) {
        x = new Range(xmin, xmax);
        y = new Range(ymin, ymax);
        z = new Range(zmin, zmax);
    }

    /**
     * @return the x
     */
    public Range getX() {
        return x;
    }

    /**
     * @return the y
     */
    public Range getY() {
        return y;
    }

    /**
     * @return the z
     */
    public Range getZ() {
        return z;
    }

    /**
     * @param x the x to set
     */
    public void setX(double min, double max) {
        x.set(min, max);
    }

    /**
     * @param y the y to set
     */
    public void setY(double min, double max) {
        y.set(min, max);
    }

    /**
     * @param z the z to set
     */
    public void setZ(double min, double max) {
        z.set(min, max);
    }

    /**
     * @return the xRotation
     */
    public double getXRotation() {
        return xRotation;
    }

    /**
     * @return the yRotation
     */
    public double getZRotation() {
        return zRotation;
    }

    /**
     * @param xRotation the xRotation to set
     */
    public void setXRotation(double xRotation) {
        this.xRotation = xRotation;
    }

    /**
     * @param zRotation the zRotation to set
     */
    public void setZRotation(double zRotation) {
        this.zRotation = zRotation;
    }

    /**
     * translate the grid
     *
     * @param dx - change in mouse x
     * @param dy = change in mouse y
     */
    public void drag(double dx, double dy) {
        dx = dx / DRAG_DAMPENER * x.getLength();
        dy = dy / DRAG_DAMPENER * y.getLength();

        x.set(x.getMin() - dx, x.getMax() - dx);
        y.set(y.getMin() + dy, y.getMax() + dy);
    }

    /**
     * zoom into the grid
     *
     * @param ds - mouse scroll change
     */
    public void zoom(double ds) {
        ds = ds / ZOOM_DAMPENER * (x.getLength() + y.getLength());

        double xmin = x.getMin() + ds;
        double xmax = x.getMax() - ds;

        if (xmin < xmax) {
            x.set(xmin, xmax);
            y.set(y.getMin() + ds, y.getMax() - ds);
            z.set(z.getMin() + ds, z.getMax() - ds);
        }
    }

    /**
     * rotate the grid by the given increments
     *
     * @param rx - x rotation increment
     * @param rz - z rotation increment
     */
    public void rotate(double rx, double rz) {
        xRotation += rx;
        zRotation += rz;
    }

    /**
     * render the x, y and z axes
     */
    public void render() {
        float scale = 1.25f;

        glColor3f(1f, 0f, 0f);
        glBegin(GL_LINES);
        glVertex3d(0d, 0d, 0d);
        glVertex3d(x.getMax() * scale, 0d, 0d);
        glEnd();

        // Draw y-axis in green
        glColor3f(0f, 1f, 0f);
        glBegin(GL_LINES);
        glVertex3d(0d, 0d, 0d);
        glVertex3d(0d, y.getMax() * scale, 0d);
        glEnd();

        // Draw z-axis in blue
        glColor3f(0f, 0f, 1f);
        glBegin(GL_LINES);
        glVertex3d(0d, 0d, 0d);
        glVertex3d(0d, 0d, z.getMax() * scale);
        glEnd();
    }
}
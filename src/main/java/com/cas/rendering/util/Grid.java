package com.cas.rendering.util;

import org.lwjgl.opengl.GL11;

/**
 * This class represents the coordinate system in which we draw the plots.
 *
 * @author Abd-El-Aziz Zayed
 */
public class Grid {

    private static final double DRAG_DAMPENER = 1000d;
    private static final double ZOOM_DAMPENER = 300d;

    private final Range x;
    private final Range y;
    private final Range z; // range in x and y direction
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


    public void setX(double min, double max) {
        x.set(min, max);
    }

    public void setY(double min, double max) {
        y.set(min, max);
    }

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
     * @param xRotation the xRotation to set
     */
    public void setXRotation(double xRotation) {
        this.xRotation = xRotation;
    }

    /**
     * @return the yRotation
     */
    public double getZRotation() {
        return zRotation;
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

        GL11.glColor3f(1f, 0f, 0f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0d, 0d, 0d);
        GL11.glVertex3d(x.getMax() * scale, 0d, 0d);
        GL11.glEnd();

        // Draw y-axis in green
        GL11.glColor3f(0f, 1f, 0f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0d, 0d, 0d);
        GL11.glVertex3d(0d, y.getMax() * scale, 0d);
        GL11.glEnd();

        // Draw z-axis in blue
        GL11.glColor3f(0f, 0f, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0d, 0d, 0d);
        GL11.glVertex3d(0d, 0d, z.getMax() * scale);
        GL11.glEnd();
    }
}
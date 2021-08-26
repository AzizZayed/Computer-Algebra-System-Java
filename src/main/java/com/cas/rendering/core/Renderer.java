package com.cas.rendering.core;

import com.cas.rendering.gui.GUIRenderer;
import com.cas.rendering.plots.CurvePair;
import com.cas.rendering.plots.SurfaceTrio;
import com.cas.rendering.util.Grid;
import net.jafama.FastMath;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the core of the application, where everything meets. This class is in
 * charge of rendering everything.
 *
 * @author Abd-El-Aziz Zayed
 */
public class Renderer {

    /*
     * properties for perspective view
     */
    private static final double FOV = 60d, ASPECT = 1d, NEAR_PLANE = 5d, FAR_PLANE = 35d;
    public static double GRID_MIN = -1d, GRID_MAX = 1d;

    private static Mode mode = Mode.RENDER_3D; // Current graphing mode

    public Renderer() {
        Display.initialize();
        start();
        Display.destroy();
    }

    /**
     * switch between 2D and 3D
     */
    public static void switchMode() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        switch (mode) {
            case RENDER_2D:
                mode = Mode.RENDER_3D;
                perspective(FOV, ASPECT, NEAR_PLANE, FAR_PLANE);
                break;
            case RENDER_3D:
                mode = Mode.RENDER_2D;
                GL11.glOrtho(0, 1, 0, 1, -1, 1);
                break;
        }
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    /**
     * Generate perspective view. Took this from the description here:
     * https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/gluPerspective.xml
     *
     * @param fovy   - field of view angle, in degrees, in the y direction.
     * @param aspect - aspect ratio of window
     * @param zNear  - distance from the viewer to the near clipping plane
     * @param zFar   - distance from the viewer to the far clipping plane
     */
    private static void perspective(double fovy, double aspect, double zNear, double zFar) {
        double f = 1.0d / FastMath.tan(fovy * Math.PI / 360d);
        double[] transformation = { //
                f / aspect, 0, 0, 0, //
                0, f, 0, 0, //
                0, 0, (zFar + zNear) / (zNear - zFar), -1, //
                0, 0, 2 * zFar * zNear / (zNear - zFar), 0 //
        };
        GL11.glMultMatrixd(transformation);
    }

    /**
     * start the rendering and the main loop
     */
    public void start() {

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        perspective(FOV, ASPECT, NEAR_PLANE, FAR_PLANE);

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClearDepth(1.0f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glEnable(GL13.GL_MULTISAMPLE);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();


        ArrayList<CurvePair> curves = new ArrayList<>();
        HashMap<Character, Double> varValues2D = new HashMap<>();
        Grid grid2D = new Grid(GRID_MIN, GRID_MAX, GRID_MIN, GRID_MAX, GRID_MIN, GRID_MAX);

        ArrayList<SurfaceTrio> surfaces = new ArrayList<>();
        HashMap<Character, Double> varValues3D = new HashMap<>();
        Grid grid3D = new Grid(GRID_MIN, GRID_MAX, GRID_MIN, GRID_MAX, GRID_MIN, GRID_MAX);

        GUIRenderer gui = GUIRenderer.getContext();

        gui.initialize();

        // Run the rendering loop until the user has attempted to close the window
        double time = 0;
        while (!Display.isCloseRequested()) {
            double currentTime = Display.getTime();
            double deltaTime = (time > 0) ? (currentTime - time) : 1f / 60f;
            time = currentTime;

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GL11.glPushMatrix();
            if (mode == Mode.RENDER_3D) {
                input3D(gui, grid3D);
                render3D(surfaces, grid3D, varValues3D);
                gui.render3D(deltaTime, surfaces, varValues3D, grid3D);
            } else {
                input2D(gui, grid2D);
                render2D(curves, grid2D, varValues2D);
                gui.render2D(deltaTime, curves, varValues2D, grid2D);
            }
            GL11.glPopMatrix();

            Display.update();
        }
        gui.destroy();
        for (CurvePair pair : curves)
            pair.cleanup();
        for (SurfaceTrio trio : surfaces)
            trio.cleanup();
    }

    /**
     * render the 3D scene
     *
     * @param surfaces  - all the surfaces to render
     * @param grid      - the data used to generate the render data
     * @param varValues - the parameter-value pair
     */
    private void render3D(ArrayList<SurfaceTrio> surfaces, Grid grid, HashMap<Character, Double> varValues) {
        transform3D(grid);

        GL11.glLineWidth(1f);
        grid.render();

        GL11.glLineWidth(0.8f);
        for (SurfaceTrio trio : surfaces)
            trio.update(grid, varValues);
    }

    /**
     * render the 2D scene
     *
     * @param curves    - all the curves to render
     * @param grid      - the data used to generate the render data
     * @param varValues - the parameter-value pair
     */
    private void render2D(ArrayList<CurvePair> curves, Grid grid, HashMap<Character, Double> varValues) {
        transform2D(grid);

        GL11.glLineWidth(1f);
        grid.render();

        GL11.glLineWidth(3f);
        /// Render Curves ///
        for (CurvePair pair : curves)
            pair.update(grid, varValues);
    }

    /**
     * transform the 3D world according to the given grid data
     *
     * @param grid
     */
    private void transform3D(Grid grid) {
        GL11.glTranslatef(0.0f, 0.0f, -15.0f);
        GL11.glRotatef(-75f, 1f, 0f, 0f);

        GL11.glRotated(grid.getXRotation(), 1d, 0d, 0d);
        GL11.glRotated(grid.getZRotation(), 0d, 0d, 1d);

        float scale = 8.0f;
        GL11.glScaled(scale / grid.getX().getLength(), scale / grid.getY().getLength(), scale / grid.getZ().getLength());
    }

    /**
     * transform the 2D world according to the given grid data
     *
     * @param grid
     */
    private void transform2D(Grid grid) {
        GL11.glScaled(1.0d / grid.getX().getLength(), 1.0d / grid.getY().getLength(), 1.0d);
        GL11.glTranslated(-grid.getX().getMin(), -grid.getY().getMin(), 0.0d);
    }

    /**
     * handle inputs for the 2D curves
     *
     * @param gui  - the event handle that carries the values we need
     * @param grid - grid to modify according to input
     */
    private void input2D(GUIRenderer gui, Grid grid) {
        grid.drag(gui.getDragX(), gui.getDragY());
        grid.zoom(gui.getMouseScroll());
    }

    /**
     * handle inputs for the 3D surfaces
     *
     * @param gui  - the event handle that carries the values we need
     * @param grid - grid to modify according to input
     */
    private void input3D(GUIRenderer gui, Grid grid) {
        grid.rotate(gui.getDragY(), gui.getDragX());
        grid.zoom(gui.getMouseScroll());
    }

    /**
     * the rendering mode, 3D or 2D
     *
     * @author Abd-El-Aziz Zayed
     */
    private enum Mode {
        RENDER_2D, RENDER_3D
    }
}
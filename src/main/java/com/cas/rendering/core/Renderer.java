package com.cas.rendering.core;

import com.cas.rendering.gui.GUIRenderer;
import com.cas.rendering.plots.CurvePair;
import com.cas.rendering.plots.SurfaceTrio;
import com.cas.rendering.util.Grid;
import net.jafama.FastMath;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glClearDepth;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glMultMatrixd;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

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

    /**
     * the rendering mode, 3D or 2D
     *
     * @author Abd-El-Aziz Zayed
     */
    private enum Mode {
        RENDER_2D, RENDER_3D
    }

    public Renderer() {
        Display.initialize();
        start();
        Display.destroy();
    }

    /**
     * switch between 2D and 3D
     */
    public static void switchMode() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        switch (mode) {
            case RENDER_2D:
                mode = Mode.RENDER_3D;
                perspective(FOV, ASPECT, NEAR_PLANE, FAR_PLANE);
                break;
            case RENDER_3D:
                mode = Mode.RENDER_2D;
                glOrtho(0, 1, 0, 1, -1, 1);
                break;
        }
        glMatrixMode(GL_MODELVIEW);
    }

    /**
     * start the rendering and the main loop
     */
    public void start() {

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        perspective(FOV, ASPECT, NEAR_PLANE, FAR_PLANE);

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClearDepth(1.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_MULTISAMPLE);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();


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

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glPushMatrix();
            if (mode == Mode.RENDER_3D) {
                input3D(gui, grid3D);
                render3D(surfaces, grid3D, varValues3D);
                gui.render3D(deltaTime, surfaces, varValues3D, grid3D);
            } else {
                input2D(gui, grid2D);
                render2D(curves, grid2D, varValues2D);
                gui.render2D(deltaTime, curves, varValues2D, grid2D);
            }
            glPopMatrix();

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

        glLineWidth(1f);
        grid.render();

        glLineWidth(0.8f);
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

        glLineWidth(1f);
        grid.render();

        glLineWidth(3f);
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
        glTranslatef(0.0f, 0.0f, -15.0f);
        glRotatef(-75f, 1f, 0f, 0f);

        glRotated(grid.getXRotation(), 1d, 0d, 0d);
        glRotated(grid.getZRotation(), 0d, 0d, 1d);

        float scale = 8.0f;
        glScaled(scale / grid.getX().getLength(), scale / grid.getY().getLength(), scale / grid.getZ().getLength());
    }

    /**
     * transform the 2D world according to the given grid data
     *
     * @param grid
     */
    private void transform2D(Grid grid) {
        glScaled(1.0d / grid.getX().getLength(), 1.0d / grid.getY().getLength(), 1.0d);
        glTranslated(-grid.getX().getMin(), -grid.getY().getMin(), 0.0d);
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
        double transformation[] = { //
                f / aspect, 0, 0, 0, //
                0, f, 0, 0, //
                0, 0, (zFar + zNear) / (zNear - zFar), -1, //
                0, 0, 2 * zFar * zNear / (zNear - zFar), 0 //
        };
        glMultMatrixd(transformation);
    }
}
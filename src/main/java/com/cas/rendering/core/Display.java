package com.cas.rendering.core;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * class to handle the GLFW window. Code from https://www.lwjgl.org/guide
 *
 * @author Abd-El-Aziz Zayed
 */
public final class Display {

    /**
     * position of the viewport
     */
    public static final int xViewport = 500;
    public static final int yViewport = 0;
    public static final int renderWidth = 1500;
    public static final int renderHeight = 1000;
    public static final int width = renderWidth + xViewport;
    public static final int height = renderHeight + yViewport;
    private static long ID; // the id of the window
    private static int bufferWidth;
    private static int bufferHeight;

    /**
     * initialize the window and show it
     */
    public static void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 16); // multi-sampling and anti-aliasing enabled

        // create the window for OpenGL
        ID = GLFW.glfwCreateWindow(width, height, "Computer Algebra System", MemoryUtil.NULL, MemoryUtil.NULL);
        if (ID == MemoryUtil.NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // setup keys
        GLFW.glfwSetKeyCallback(ID, (window, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                GLFW.glfwSetWindowShouldClose(window, true);
        });

        GLFWVidMode vid = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(ID, (vid.width() - width) / 2, (vid.height() - height) / 2);

        final int[] fbWidth = new int[1];
        final int[] fbHeight = new int[1];
        GLFW.glfwGetFramebufferSize(Display.ID, fbWidth, fbHeight);
        bufferWidth = fbWidth[0];
        bufferHeight = fbHeight[0];

        GLFW.glfwMakeContextCurrent(ID); // create context
        GLFW.glfwSwapInterval(GLFW.GLFW_TRUE); // enable v-sync
        GLFW.glfwShowWindow(ID); // window visible

        GL.createCapabilities(); // make OpenGL bindings available
        System.out.println("OpenGL Version: " + GL11.glGetString(GL11.GL_VERSION));

        GL11.glViewport(
                (int) (xViewport * xScale()),
                (int) (yViewport * xScale()),
                (int) (renderWidth * xScale()),
                (int) (renderHeight * yScale())
        );
    }

    /**
     * @return the time since this display was initialized
     */
    public static double getTime() {
        return GLFW.glfwGetTime();
    }

    /**
     * update the display every frame
     */
    public static void update() {
        GLFW.glfwSwapBuffers(ID); // swap the color buffers
        GLFW.glfwPollEvents();
    }

    /**
     * destroy the display when done
     */
    public static void destroy() {
        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(ID);
        GLFW.glfwDestroyWindow(ID);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    /**
     * @return if the user closed the display
     */
    public static boolean isCloseRequested() {
        return GLFW.glfwWindowShouldClose(ID);
    }

    public static long id() {
        return ID;
    }

    public static int bufferWidth() {
        return bufferWidth;
    }

    public static int bufferHeight() {
        return bufferHeight;
    }

    public static float xScale() {
        return (float) bufferWidth / width;
    }

    public static float yScale() {
        return (float) bufferHeight / height;
    }

    public static float xMouse() {
        double[] x = new double[1];
        GLFW.glfwGetCursorPos(ID, x, null);
        return (float) x[0];
    }

    public static float yMouse() {
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(ID, null, y);
        return (float) y[0];
    }

    public static float scaleX(int x) {
        return xScale() * x;
    }

    public static float scaleY(int y) {
        return yScale() * y;
    }
}
package com.cas.rendering.core;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;

import java.awt.Point;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_COCOA_CHDIR_RESOURCES;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwInitHint;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * class to handle the GLFW window. Code from https://www.lwjgl.org/guide
 *
 * @author Abd-El-Aziz Zayed
 */
public final class Display {

    private static long ID; // the id of the window

    /**
     * position of the viewport
     */
    public static final int xViewport = 500;
    public static final int yViewport = 0;

    public static final int renderWidth = 1500;
    public static final int renderHeight = 1000;

    public static final int width = renderWidth + xViewport;
    public static final int height = renderHeight + yViewport;

    private static int bufferWidth;
    private static int bufferHeight;

    /**
     * initialize the window and show it
     */
    public static void initialize() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_SAMPLES, 16); // multi-sampling and anti-aliasing enabled

        // create the window for OpenGL
        ID = glfwCreateWindow(width, height, "Computer Algebra System", NULL, NULL);
        if (ID == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // setup keys
        glfwSetKeyCallback(ID, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(ID, (vid.width() - width) / 2, (vid.height() - height) / 2);

        final int[] fbWidth = new int[1];
        final int[] fbHeight = new int[1];
        glfwGetFramebufferSize(Display.ID, fbWidth, fbHeight);
        bufferWidth = fbWidth[0];
        bufferHeight = fbHeight[0];

        glfwMakeContextCurrent(ID); // create context
        glfwSwapInterval(GLFW_TRUE); // enable v-sync
        glfwShowWindow(ID); // window visible

        GL.createCapabilities(); // make OpenGL bindings available
        System.out.println("OpenGL Version: " + glGetString(GL_VERSION));

        glViewport(
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
        return glfwGetTime();
    }

    /**
     * update the display every frame
     */
    public static void update() {
        glfwSwapBuffers(ID); // swap the color buffers
        glfwPollEvents();
    }

    /**
     * destroy the display when done
     */
    public static void destroy() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(ID);
        glfwDestroyWindow(ID);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    /**
     * @return if the user closed the display
     */
    public static boolean isCloseRequested() {
        return glfwWindowShouldClose(ID);
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
        glfwGetCursorPos(ID, x, null);
        return (float) x[0];
    }

    public static float yMouse() {
        double[] y = new double[1];
        glfwGetCursorPos(ID, null, y);
        return (float) y[0];
    }

    public static float scaleX(int x) {
        return xScale() * x;
    }

    public static float scaleY(int y) {
        return yScale() * y;
    }
}
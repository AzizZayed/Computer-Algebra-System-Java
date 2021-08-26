package com.cas.rendering.gui;

import com.cas.core.Equation;
import com.cas.rendering.core.Display;
import com.cas.rendering.core.Renderer;
import com.cas.rendering.plots.Curve;
import com.cas.rendering.plots.CurvePair;
import com.cas.rendering.plots.Plot;
import com.cas.rendering.plots.Surface;
import com.cas.rendering.plots.SurfaceTrio;
import com.cas.rendering.util.Grid;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImGuiStyle;
import imgui.type.ImString;
import imgui.ImVec2;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiColorEditFlags;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_ARROW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.GLFW_HAND_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_HRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_IBEAM_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_END;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SUPER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_TAB;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_2;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_3;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_4;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_5;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_VRESIZE_CURSOR;
import static org.lwjgl.glfw.GLFW.glfwCreateStandardCursor;
import static org.lwjgl.glfw.GLFW.glfwDestroyCursor;
import static org.lwjgl.glfw.GLFW.glfwGetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;

/**
 * class to render ImGui using the java bindings. Most of the setup code is
 * taken from the example here:
 * https://github.com/SpaiR/imgui-java/tree/master/imgui-lwjgl3/src/test/java
 * <p>
 * Since ImGui has no documentation, I used the demo file to find the functions
 * I need: https://github.com/ocornut/imgui/blob/master/imgui_demo.cpp
 *
 * @author Abd-El-Aziz Zayed
 */
public class GUIRenderer {

    private final ImGuiImplGl3 imGui = new ImGuiImplGl3(); // OpenGL ImGui context
    private static GUIRenderer instance = new GUIRenderer(); // singleton instance

    private final long[] mouseCursors = new long[ImGuiMouseCursor.COUNT]; // Mouse cursors provided by GLFW

    private ImString strFunction2 = new ImString("x^2", 500); // string object to record 2D input
    private ImString strFunction3 = new ImString("sin(x*y)", 500); // string object to record 2D input

    private String errorMessage = ""; // the error message that appreas when there is an error

    private HashMap<Character, Float> sliderSteps2D = new HashMap<>(); // the incrementation values for each slider 2D
    private HashMap<Character, Float> sliderSteps3D = new HashMap<>(); // the incrementation values for each slider 3D

    private ImVec2 mouseDrag = new ImVec2(0f, 0f); // the vector describing the mouse drag
    private float scroll = 0f; // mouse wheel scroll delta

    /**
     * @return the only GUIRenderer instance
     */
    public static GUIRenderer getContext() {
        return instance;
    }

    /**
     * make constructor private for singleton
     */
    private GUIRenderer() {
    }

    /**
     * initialize ImGui
     */
    public void initialize() {
        long windowPtr = Display.id();

        ImGui.createContext();

        // Initialize ImGuiIO config
        final ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null); // We don't want to save .ini file
        io.setConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Navigation with keyboard
        io.setConfigFlags(ImGuiConfigFlags.DockingEnable);
        io.setBackendFlags(ImGuiBackendFlags.HasMouseCursors); // Mouse cursors to display while resizing windows etc.
        io.setBackendPlatformName("imgui_java_impl_glfw");

        // ------------------------------------------------------------
        // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[]
        // array.
        final int[] keyMap = new int[ImGuiKey.COUNT];
        keyMap[ImGuiKey.Tab] = GLFW_KEY_TAB;
        keyMap[ImGuiKey.LeftArrow] = GLFW_KEY_LEFT;
        keyMap[ImGuiKey.RightArrow] = GLFW_KEY_RIGHT;
        keyMap[ImGuiKey.UpArrow] = GLFW_KEY_UP;
        keyMap[ImGuiKey.DownArrow] = GLFW_KEY_DOWN;
        keyMap[ImGuiKey.PageUp] = GLFW_KEY_PAGE_UP;
        keyMap[ImGuiKey.PageDown] = GLFW_KEY_PAGE_DOWN;
        keyMap[ImGuiKey.Home] = GLFW_KEY_HOME;
        keyMap[ImGuiKey.End] = GLFW_KEY_END;
        keyMap[ImGuiKey.Insert] = GLFW_KEY_INSERT;
        keyMap[ImGuiKey.Delete] = GLFW_KEY_DELETE;
        keyMap[ImGuiKey.Backspace] = GLFW_KEY_BACKSPACE;
        keyMap[ImGuiKey.Space] = GLFW_KEY_SPACE;
        keyMap[ImGuiKey.Enter] = GLFW_KEY_ENTER;
        keyMap[ImGuiKey.Escape] = GLFW_KEY_ESCAPE;
        keyMap[ImGuiKey.KeyPadEnter] = GLFW_KEY_KP_ENTER;
        keyMap[ImGuiKey.A] = GLFW_KEY_A;
        keyMap[ImGuiKey.C] = GLFW_KEY_C;
        keyMap[ImGuiKey.V] = GLFW_KEY_V;
        keyMap[ImGuiKey.X] = GLFW_KEY_X;
        keyMap[ImGuiKey.Y] = GLFW_KEY_Y;
        keyMap[ImGuiKey.Z] = GLFW_KEY_Z;
        io.setKeyMap(keyMap);

        // ------------------------------------------------------------
        // Mouse cursors mapping
        mouseCursors[ImGuiMouseCursor.Arrow] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.TextInput] = glfwCreateStandardCursor(GLFW_IBEAM_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeAll] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNS] = glfwCreateStandardCursor(GLFW_VRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeEW] = glfwCreateStandardCursor(GLFW_HRESIZE_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNESW] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.ResizeNWSE] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);
        mouseCursors[ImGuiMouseCursor.Hand] = glfwCreateStandardCursor(GLFW_HAND_CURSOR);
        mouseCursors[ImGuiMouseCursor.NotAllowed] = glfwCreateStandardCursor(GLFW_ARROW_CURSOR);

        // ------------------------------------------------------------
        // GLFW callbacks to handle user input

        glfwSetKeyCallback(windowPtr, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                io.setKeysDown(key, true);
            } else if (action == GLFW_RELEASE) {
                io.setKeysDown(key, false);
            }

            io.setKeyCtrl(io.getKeysDown(GLFW_KEY_LEFT_CONTROL) || io.getKeysDown(GLFW_KEY_RIGHT_CONTROL));
            io.setKeyShift(io.getKeysDown(GLFW_KEY_LEFT_SHIFT) || io.getKeysDown(GLFW_KEY_RIGHT_SHIFT));
            io.setKeyAlt(io.getKeysDown(GLFW_KEY_LEFT_ALT) || io.getKeysDown(GLFW_KEY_RIGHT_ALT));
            io.setKeySuper(io.getKeysDown(GLFW_KEY_LEFT_SUPER) || io.getKeysDown(GLFW_KEY_RIGHT_SUPER));
        });

        glfwSetCharCallback(windowPtr, (w, c) -> {
            if (c != GLFW_KEY_DELETE) {
                io.addInputCharacter(c);
            }
        });

        glfwSetMouseButtonCallback(windowPtr, (w, button, action, mods) -> {
            final boolean[] mouseDown = new boolean[5];

            mouseDown[0] = button == GLFW_MOUSE_BUTTON_1 && action != GLFW_RELEASE;
            mouseDown[1] = button == GLFW_MOUSE_BUTTON_2 && action != GLFW_RELEASE;
            mouseDown[2] = button == GLFW_MOUSE_BUTTON_3 && action != GLFW_RELEASE;
            mouseDown[3] = button == GLFW_MOUSE_BUTTON_4 && action != GLFW_RELEASE;
            mouseDown[4] = button == GLFW_MOUSE_BUTTON_5 && action != GLFW_RELEASE;

            io.setMouseDown(mouseDown);

            if (!io.getWantCaptureMouse() && mouseDown[1]) {
                ImGui.setWindowFocus(null);
            }

        });

        glfwSetScrollCallback(windowPtr, (w, xOffset, yOffset) -> {
            io.setMouseWheelH(io.getMouseWheelH() + (float) xOffset);
            io.setMouseWheel(io.getMouseWheel() + (float) yOffset);
        });

        io.setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(final String s) {
                glfwSetClipboardString(windowPtr, s);
            }
        });

        io.setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                final String clipboardString = glfwGetClipboardString(windowPtr);
                if (clipboardString != null) {
                    return clipboardString;
                } else {
                    return "";
                }
            }
        });

        imGui.init("#version 120");
        ImGuiStyle style = ImGui.getStyle();
        style.setFrameRounding(0f);
        style.setWindowRounding(0f);
        style.setChildRounding(0f);
        style.setScrollbarRounding(0f);
        style.setGrabRounding(0f);
        style.setTabRounding(0f);
        ImGui.styleColorsDark();
        io.setFontGlobalScale(3f);
    }

    /**
     * setup the beginning of an ImGui frame
     *
     * @param deltaTime - time between frames
     */
    private void startFrame(final float deltaTime) {
        final ImGuiIO io = ImGui.getIO();
        io.setDisplaySize(Display.bufferWidth(),  Display.bufferHeight());
        io.setDeltaTime(deltaTime);
        io.setMousePos(Display.xMouse() * Display.xScale(), Display.yMouse() * Display.yScale());

        // Update the mouse cursor
        final int imguiCursor = ImGui.getMouseCursor();
        glfwSetCursor(Display.id(), mouseCursors[imguiCursor]);
        glfwSetInputMode(Display.id(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    }

    /**
     * render main GUI elements
     *
     * @param dt        - delta time
     * @param curves    - list with all the curves to draw
     * @param varValues - map with all the variables and they're corresponding
     *                  values for the sliders
     * @param grid      - the coordinate system
     */
    public void render2D(double dt, ArrayList<CurvePair> curves, HashMap<Character, Double> varValues, Grid grid) {
        boolean mods = false; // if modifications were done to the GUI

        startFrame((float) dt);

        beginWindow("2D Functions");

        if (ImGui.button("Switch to 3D")) {
            errorMessage = "";
            Renderer.switchMode();
        }
        ImGui.sameLine();
        resetButton(grid);

        renderSliders(varValues, sliderSteps2D); // if any modifications were done

        /*
         * Render functions
         */
        ImGui.inputText("Input here", strFunction2);
        ImGui.sameLine();
        ImGuiHelp("Input your function here. Example: x^2");
        boolean add = ImGui.button("Add 2D Function");
        if (errorMessage != "")
            ImGui.textColored(1f, 0f, 0f, 1f, errorMessage);
        if (add) {
            try {
                String func = strFunction2.get();
                HashSet<Character> variables = new HashSet<>();
                CurvePair c = new CurvePair(new Equation(func, variables));
                if (variables.contains('y') || variables.contains('z'))
                    errorMessage = "y and z are reserved letters. Use others.";
                else {
                    errorMessage = "";
                    curves.add(c);
                }
                variables.forEach(key -> {
                    if (validKey(key)) {
                        varValues.putIfAbsent(key, 1d);
                        sliderSteps2D.putIfAbsent(key, 0.01f);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Parsing error. Refer to GitHub README for function syntax.";
            }
        }

        for (int i = 0; i < curves.size(); i++) {
            CurvePair curve = curves.get(i);
            Curve func = curve.getFunction();
            Curve der = curve.getDerivative();
            String name = (i + 1) + " Function y = " + func.getEquation().toFancyString() + "##F2" + i;
            if (ImGui.collapsingHeader(name, ImGuiTreeNodeFlags.DefaultOpen)) {

                if (drawPlotInfo(func, "Plot function##PlotF2" + i, "Color##Func2C" + i))
                    mods = true;

                if (der != null)
                    if (drawPlotInfo(der, "Plot derivative w.r.t. x##DX" + i, "Color##DXC" + i))
                        mods = true;

                if (ImGui.button("Delete Function##closeF2" + i)) {
                    curves.remove(i);

                    HashSet<Character> vars = new HashSet<>();
                    for (CurvePair pair : curves)
                        vars.addAll(pair.getFunction().getEquation().getVariables());

                    refreshSliders(vars, varValues.keySet(), sliderSteps2D);
                }
            }
        }

        collectInput(!mods);

        ImGui.end();
        ImGui.render();

        imGui.renderDrawData(ImGui.getDrawData());
    }

    /**
     * render main GUI elements for 3D context
     *
     * @param dt        - delta time
     * @param surfaces  - list with all the surfaces to draw
     * @param varValues - map with all the variables and they're corresponding
     *                  values for the sliders
     * @param grid      - the coordinate system
     */
    public void render3D(double dt, ArrayList<SurfaceTrio> surfaces, HashMap<Character, Double> varValues, Grid grid) {
        boolean mods = false;

        startFrame((float) dt);

        beginWindow("3D Functions");

        if (ImGui.button("Switch to 2D")) {
            errorMessage = "";
            Renderer.switchMode();
        }
        ImGui.sameLine();
        resetButton(grid);

        renderSliders(varValues, sliderSteps3D); // if any modifications were done

        /*
         * Render functions
         */
        ImGui.inputText("Input here", strFunction3);
        ImGui.sameLine();
        ImGuiHelp("Input your function here. Example: x^2 + y^2");
        boolean add = ImGui.button("Add 3D Function");
        if (errorMessage != "")
            ImGui.textColored(1f, 0f, 0f, 1f, errorMessage);
        if (add) {
            try {
                String func = strFunction3.get();
                HashSet<Character> variables = new HashSet<>();
                SurfaceTrio c = new SurfaceTrio(new Equation(func, variables));
                if (variables.contains('z'))
                    errorMessage = "z is a reserved letter. Use another.";
                else {
                    errorMessage = "";
                    surfaces.add(c);
                }
                variables.forEach(key -> {
                    if (validKey(key)) {
                        varValues.putIfAbsent(key, 1d);
                        sliderSteps3D.putIfAbsent(key, 0.01f);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Parsing error. Refer to GitHub README for function syntax.";
            }
        }

        for (int i = 0; i < surfaces.size(); i++) {
            SurfaceTrio curve = surfaces.get(i);
            Surface func = curve.getFunction();
            Surface xDer = curve.getxDerivative();
            Surface yDer = curve.getyDerivative();
            String name = (i + 1) + " Function z = " + func.getEquation().toFancyString() + "##F3" + i;
            if (ImGui.collapsingHeader(name, ImGuiTreeNodeFlags.DefaultOpen)) {

                if (drawPlotInfo(func, "Plot function##Plot3F" + i, "Color##Func3C" + i))
                    mods = true;

                if (xDer != null)
                    if (drawPlotInfo(xDer, "Plot derivative w.r.t. x##PDX" + i, "Color##PDXC" + i))
                        mods = true;

                if (yDer != null)
                    if (drawPlotInfo(yDer, "Plot derivative w.r.t. y##PDY" + i, "Color##PDYC" + i))
                        mods = true;

                if (ImGui.button("Delete Function##closeF3" + i)) {
                    surfaces.remove(i);

                    HashSet<Character> vars = new HashSet<>();
                    for (SurfaceTrio trio : surfaces)
                        vars.addAll(trio.getFunction().getEquation().getVariables());

                    refreshSliders(vars, varValues.keySet(), sliderSteps3D);
                }
            }
        }

        collectInput(!mods);

        ImGui.end();
        ImGui.render();

        imGui.renderDrawData(ImGui.getDrawData());
    }


    /**
     * refresh the list of sliders when a function is deleted so we do not have an
     * unnecessary one
     *
     * @param vars        - all the variable left after deletion of a function
     * @param keys        - set of current parameters to remove from
     * @param sliderSteps - the slider map to remove from
     */
    private void refreshSliders(HashSet<Character> vars, Set<Character> keys, HashMap<Character, Float> sliderSteps) {
        Iterator<Character> itr = keys.iterator();
        while (itr.hasNext()) {
            char key = itr.next();
            if (!vars.contains(key)) {
                itr.remove();
                sliderSteps.remove(key);
            }
        }
    }

    /**
     * render a new window with a lone button to reset the grid
     *
     * @param grid - grid to reset if button is pressed
     */
    private void resetButton(Grid grid) {
        if (ImGui.button("Reset")) {
            double min = Renderer.GRID_MIN;
            double max = Renderer.GRID_MAX;
            grid.setX(min, max);
            grid.setY(min, max);
            grid.setZ(min, max);
            grid.setXRotation(0);
            grid.setZRotation(0);
        }
        ImGui.sameLine();
        ImGuiHelp("Reset the center to (0, 0) and the range in all directions from -1 to 1.");
    }

    /**
     * draw all the info relevant for a single Plot object
     *
     * @param plot           - the plot
     * @param checkBoxLabel  - the label for the visibility toggle
     * @param colorEditLabel - the label for the color edit
     * @return true if the color edit was edited
     */
    private boolean drawPlotInfo(Plot plot, String checkBoxLabel, String colorEditLabel) {
        boolean visible = plot.isVisible();
        ImGui.checkbox(checkBoxLabel, visible);
        plot.setVisible(visible);
        boolean mod = ImGui.colorEdit4(colorEditLabel, plot.getColor(), ImGuiColorEditFlags.Float);
        Texture tex = plot.getTexture();
        ImGui.image(tex.getID(), tex.getWidth(), tex.getHeight());
        return mod;
    }

    /**
     * Begin the window frame. This should be called every start of a frame
     *
     * @param title - title of the window
     */
    private void beginWindow(String title) {
        ImGui.newFrame();
        ImGui.setNextWindowPos(Display.scaleX(5), Display.scaleY(5), ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(Display.scaleX(Display.xViewport), Display.scaleY(Display.height - 10), ImGuiCond.FirstUseEver);
        ImGui.begin(title, ImGuiWindowFlags.HorizontalScrollbar);
    }

    /**
     * render all the sliders
     *
     * @param varValues   - map with all the variables and value pairs
     * @param sliderSteps - the increments for each parameter slider
     */
    private void renderSliders(HashMap<Character, Double> varValues, HashMap<Character, Float> sliderSteps) {
        /*
         * Render sliders
         */
//		ImGui.text("Sliders");
        for (Map.Entry<Character, Double> entry : varValues.entrySet()) {
            char key = entry.getKey();
            double value = entry.getValue();
            if (validKey(key)) {
                float s = sliderSteps.get(key);
                ImDouble val = new ImDouble(value);

                // value slider
                ImGui.dragScalar(Character.toString(key), ImGuiDataType.Double, val, s);
                varValues.put(key, val.get());

                // step / incrementation slider
                ImFloat step = new ImFloat(s);
                ImGui.dragScalar("Step " + key, ImGuiDataType.Float, step, 0.001f);
                sliderSteps.put(key, step.get());

                ImGui.separator();
            }
        }

        ImGui.separator();
    }

    /**
     * @param key
     * @return true if the given key is valid to be a slider
     */
    private boolean validKey(char key) {
        return key != 'x' && key != 'y' && key != 'z';
    }

    /**
     * create a help tooltip with ImGui and the given message
     *
     * @param message - help message
     */
    private void ImGuiHelp(String message) {
        ImGui.textDisabled("(?)");
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.pushTextWrapPos(ImGui.getFontSize() * 35.0f);
            ImGui.textUnformatted(message);
            ImGui.popTextWrapPos();
            ImGui.endTooltip();
        }
    }

    /**
     * save and update the input properties
     *
     * @param recordMouseMove - if we should record mouse movement
     */
    private void collectInput(boolean recordMouseMove) {
        ImGuiIO io = ImGui.getIO();
        mouseDrag = new ImVec2(0f, 0f);
        ImVec2 mousePos = new ImVec2();
        ImGui.getMousePos(mousePos);

        if (mousePos.x > Display.xViewport) {
            if (io.getMouseDown(ImGuiMouseButton.Left) && recordMouseMove)
                io.getMouseDelta(mouseDrag);
            scroll = io.getMouseWheel();
        }
    }

    /**
     * @return the change in x when the mouse is dragged
     */
    public float getDragX() {
        return mouseDrag.x;
    }

    /**
     * @return the change in y when the mouse is dragged
     */
    public float getDragY() {
        return mouseDrag.y;
    }

    /**
     * @return the mouse wheel difference
     */
    public float getMouseScroll() {
        return scroll;
    }

    /**
     * destroy the ImGui context and it's resources
     */
    public void destroy() {
        imGui.dispose();
        ImGui.destroyContext();

        for (long mouseCursor : mouseCursors) {
            glfwDestroyCursor(mouseCursor);
        }
    }
}
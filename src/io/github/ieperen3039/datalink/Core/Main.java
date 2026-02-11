package io.github.ieperen3039.datalink.Core;

import io.github.ieperen3039.datalink.UI.Menu;
import io.github.ieperen3039.ngn.Tools.RealTimeTimer;
import io.github.ieperen3039.ngn.Version;
import io.github.ieperen3039.ngn.Camera.Camera;
import io.github.ieperen3039.ngn.Camera.PointCenteredCamera;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.FrameManagerImpl;
import io.github.ieperen3039.ngn.GUIMenu.FrameManagers.UIManager;
import io.github.ieperen3039.ngn.InputHandling.KeyControl;
import io.github.ieperen3039.ngn.InputHandling.MouseTools.MouseToolCallbacks;
import io.github.ieperen3039.ngn.Rendering.GLFWWindow;
import io.github.ieperen3039.ngn.Rendering.RenderLoop;
import io.github.ieperen3039.ngn.Settings.Settings;
import io.github.ieperen3039.ngn.Tools.Logger;
import io.github.ieperen3039.ngn.Tools.TickTime;
import io.github.ieperen3039.ngn.Tools.Vectors;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * A tool for visualising large graphs
 * @author Geert van Ieperen. Created on 13-9-2018.
 */
public class Main implements io.github.ieperen3039.ngn.Core.Main {
    private static final Version VERSION = new Version(0, 3);

    private final Thread mainThread;
    public final RenderLoop renderer;

    private final FrameManagerImpl frameManager;
    private final Settings settings;
    private final GLFWWindow window;
    private final MouseToolCallbacks inputHandler;
    private final KeyControl keyControl;
    private final Camera camera;
    private Menu menu;
    private TickTime timer;

    public Main(Settings settings) throws Exception {
        Logger.INFO.print("Starting up...");

        Logger.DEBUG.print("General debug information: "
                // manual aligning will do the trick
                + "\n\tSystem OS:          " + System.getProperty("os.name")
                + "\n\tJava VM:            " + System.getProperty("java.runtime.version")
                + "\n\tTool version:       " + getVersionNumber()
        );

        this.settings = settings;
        GLFWWindow.Settings videoSettings = new GLFWWindow.Settings(settings);

        window = new GLFWWindow(Settings.TITLE, videoSettings);
        renderer = new RenderLoop(settings.TARGET_FPS);
        inputHandler = new MouseToolCallbacks();
        keyControl = inputHandler.getKeyControl();
        frameManager = new FrameManagerImpl();
        mainThread = Thread.currentThread();
        camera = new PointCenteredCamera(Vectors.O);
        timer = new RealTimeTimer();
    }

    public void root() throws Exception {
        Logger.DEBUG.print("Initializing...");
        // init all fields
        renderer.init(this);
        inputHandler.init(this);
        frameManager.init(window, this);
        camera.init(this);

        renderer.addHudItem(frameManager::draw);
        // for 3D objects, use renderer.renderSequence()

        menu = new Menu(this);
        frameManager.setMainPanel(menu);

        window.open();

        Logger.INFO.print("Finished startup\n");

        renderer.run();

        window.cleanup();

        Logger.INFO.print("Tool has been closed successfully");
    }

    @Override
    public TickTime timer() {
        return timer;
    }

    public Camera camera() {
        return camera;
    }

    public Settings settings() {
        return settings;
    }

    public GLFWWindow window() {
        return window;
    }

    public MouseToolCallbacks inputHandling() {
        return inputHandler;
    }

    public KeyControl keyControl() {
        return keyControl;
    }

    public Version getVersionNumber() {
        return VERSION;
    }

    public UIManager gui() {
        return frameManager;
    }

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     * @param <V>    the return type of action
     * @return a reference to obtain the result of the execution, or null if it threw an exception
     */
    public <V> Future<V> computeOnRenderThread(Callable<V> action) {
        FutureTask<V> task = new FutureTask<>(() -> {
            try {
                return action.call();

            } catch (Exception ex) {
                Logger.ERROR.print(ex);
                return null;
            }
        });

        executeOnRenderThread(task);
        return task;
    }

    /**
     * Schedules the specified action to be executed in the OpenGL context. The action is guaranteed to be executed
     * before two frames have been rendered.
     * @param action the action to execute
     */
    public void executeOnRenderThread(Runnable action) {
        if (Thread.currentThread() == mainThread) {
            action.run();
        } else {
            renderer.defer(action);
        }
    }
}

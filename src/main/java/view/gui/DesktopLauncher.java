package view.gui;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import util.Log;

public final class DesktopLauncher {
    private DesktopLauncher() {
    }

    public static final long PROCESS_START = System.currentTimeMillis();

    public static void main(String[] args) {
        Log.installConsoleCapture();
        Log.info("gui", "Java " + System.getProperty("java.version")
                + " (" + System.getProperty("java.vendor") + ")");

        System.setProperty("java.awt.headless", "true");

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Plants vs. Zombies 2");
        config.setWindowedMode(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT);
        config.setWindowSizeLimits(960, 540, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new PvzGame(Boolean.getBoolean("pvz.tour")), config);
    }
}

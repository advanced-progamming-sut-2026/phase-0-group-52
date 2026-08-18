package view.gui;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import util.Log;

/**
 * Entry point for the graphical build.
 *
 * <p>The console build still starts from {@code Main}; this is a second, parallel
 * front end over the same model and controllers. Console capture is installed
 * first so anything the controllers print during a click is available to the
 * interface as a notification.
 */
public final class DesktopLauncher {

    private DesktopLauncher() {
    }

    public static void main(String[] args) {
        Log.installConsoleCapture();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Plants vs. Zombies 2");
        config.setWindowedMode(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT);
        config.setWindowSizeLimits(960, 540, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // -Dpvz.tour=true walks every screen, captures it, and exits.
        new Lwjgl3Application(new PvzGame(Boolean.getBoolean("pvz.tour")), config);
    }
}

package view.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import util.Log;

public final class Display {
    private static int windowedWidth = Theme.WORLD_WIDTH;
    private static int windowedHeight = Theme.WORLD_HEIGHT;

    private Display() {
    }

    public static boolean isFullscreen() {
        return Gdx.graphics != null && Gdx.graphics.isFullscreen();
    }

    public static void toggle() {
        setFullscreen(!isFullscreen());
    }

    public static void setFullscreen(boolean wanted) {
        if (Gdx.graphics == null || wanted == isFullscreen()) {
            return;
        }
        if (wanted) {
            windowedWidth = Gdx.graphics.getWidth();
            windowedHeight = Gdx.graphics.getHeight();
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            if (mode == null || !Gdx.graphics.setFullscreenMode(mode)) {
                Log.warn("gui", "Could not switch to fullscreen");
                return;
            }
            Log.info("gui", "Fullscreen on (" + mode.width + "x" + mode.height + ")");
            return;
        }
        if (!Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight)) {
            Log.warn("gui", "Could not return to windowed mode");
            return;
        }
        Log.info("gui", "Fullscreen off (" + windowedWidth + "x" + windowedHeight + ")");
    }
}

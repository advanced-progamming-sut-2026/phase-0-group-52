package view.gui;

import model.App;
import model.User;

public final class GameContext {
    private final App app;
    private final UiKit ui;
    private final Toasts toasts;
    private final Settings settings;

    public GameContext(App app, UiKit ui, Toasts toasts, Settings settings) {
        this.app = app;
        this.ui = ui;
        this.toasts = toasts;
        this.settings = settings;
    }

    public App app() {
        return app;
    }

    public UiKit ui() {
        return ui;
    }

    public Toasts toasts() {
        return toasts;
    }

    public Settings settings() {
        return settings;
    }

    public User user() {
        return app.getCurrentuser();
    }

    public static final class Settings {
        private int gameSpeed = 1;
        private boolean showGrid;
        private boolean debugMode;

        public int getGameSpeed() {
            return gameSpeed;
        }

        public void setGameSpeed(int gameSpeed) {
            this.gameSpeed = Math.max(1, Math.min(3, gameSpeed));
        }

        public boolean isShowGrid() {
            return showGrid;
        }

        public void setShowGrid(boolean showGrid) {
            this.showGrid = showGrid;
        }

        public boolean isDebugMode() {
            return debugMode;
        }

        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }
    }
}

package view.gui;

import model.App;
import model.User;

public final class GameContext {
    private final App app;
    private final UiKit ui;
    private final Toasts toasts;
    private final Settings settings;
    private final Assets assets;

    public GameContext(App app, UiKit ui, Toasts toasts, Settings settings, Assets assets) {
        this.app = app;
        this.ui = ui;
        this.toasts = toasts;
        this.settings = settings;
        this.assets = assets;
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

    public Assets assets() {
        return assets;
    }

    public User user() {
        return app.getCurrentuser();
    }

    public static final class Settings {
        private final App app;
        private final controller.menu.SettingMenuController controller;

        private int gameSpeed = 1;
        private boolean showGrid;
        private boolean debugMode;
        private boolean uiEditMode;

        public Settings(App app) {
            this.app = app;
            this.controller = new controller.menu.SettingMenuController(app);
        }

        private model.User signedIn() {
            return app == null ? null : app.getLoggedInUser();
        }

        private void send(String... command) {
            controller.handleCommand(command);
        }

        public int getGameSpeed() {
            model.User user = signedIn();
            return user == null ? gameSpeed : user.getGameSpeed();
        }

        public void setGameSpeed(int value) {
            int clamped = Math.max(1, Math.min(3, value));
            if (signedIn() == null) {
                gameSpeed = clamped;
                return;
            }
            send("menu", "settings", "set-speed", "-v", String.valueOf(clamped));
        }

        public boolean isShowGrid() {
            model.User user = signedIn();
            return user == null ? showGrid : user.isShowGrid();
        }

        public void setShowGrid(boolean value) {
            if (signedIn() == null) {
                showGrid = value;
                return;
            }
            if (signedIn().isShowGrid() != value) {
                send("menu", "settings", "toggle-grid");
            }
        }

        public boolean isDebugMode() {
            model.User user = signedIn();
            return user == null ? debugMode : user.isDebugMode();
        }

        public void setDebugMode(boolean value) {
            if (signedIn() == null) {
                debugMode = value;
                return;
            }
            if (signedIn().isDebugMode() != value) {
                send("menu", "settings", "toggle-debug");
            }
        }

        public boolean isUiEditMode() {
            model.User user = signedIn();
            return user == null ? uiEditMode : user.isUiEditMode();
        }

        public void setUiEditMode(boolean value) {
            if (signedIn() == null) {
                uiEditMode = value;
                return;
            }
            if (signedIn().isUiEditMode() != value) {
                send("menu", "settings", "toggle-ui-edit");
            }
        }
    }
}

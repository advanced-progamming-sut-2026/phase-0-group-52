package view.gui;

import model.App;
import model.User;

/**
 * The handful of shared objects every screen needs, passed down instead of reached
 * for through statics.
 *
 * <p>This is a view-side container only. It exposes the model for reading and the
 * shared interface services; it deliberately offers no setters that would let a
 * screen mutate game state directly. Screens change state by calling controllers,
 * which is what keeps the view layer free of game rules.
 */
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

    /** The model root. Screens read from it; they never write to it. */
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

    /** The signed-in user, or {@code null} on the auth screens. */
    public User user() {
        return app.getCurrentuser();
    }

    /**
     * View-only preferences introduced by the graphics phase.
     *
     * <p>Game speed, grid overlay and debug mode affect presentation rather than
     * rules, so they live here rather than being pushed into the model. Difficulty
     * is not here: it already exists on {@link User} and is changed through
     * {@code SettingMenuController}.
     */
    public static final class Settings {

        private int gameSpeed = 1;
        private boolean showGrid;
        private boolean debugMode;

        /** 1..3, matching the specification's speed setting. */
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

        /** When on, cheat controls appear in the top bar and on the lawn. */
        public boolean isDebugMode() {
            return debugMode;
        }

        public void setDebugMode(boolean debugMode) {
            this.debugMode = debugMode;
        }
    }
}

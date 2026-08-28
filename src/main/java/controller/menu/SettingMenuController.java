package controller.menu;

import controller.SaveService;
import model.App;
import model.Result;
import model.User;
import model.enums.MenuType;

public class SettingMenuController {

    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 3;
    public static final int MIN_DIFFICULTY = 1;
    public static final int MAX_DIFFICULTY = 5;

    private final App app;
    private final SaveService saves = new SaveService();

    public SettingMenuController(App app) {
        this.app = app;
    }

    public Result setGameSpeed(int speed) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (speed < MIN_SPEED || speed > MAX_SPEED) {
            return failure("Speed must be between " + MIN_SPEED + " and " + MAX_SPEED + ".");
        }
        user.setGameSpeed(speed);
        return saved(user, "Game speed set to " + speed + "x.");
    }

    public Result setDifficulty(int level) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (level < MIN_DIFFICULTY || level > MAX_DIFFICULTY) {
            return failure("Difficulty must be between "
                    + MIN_DIFFICULTY + " and " + MAX_DIFFICULTY + ".");
        }
        user.setDifficultyLevel(level);
        return saved(user, "Difficulty set to " + level + ".");
    }

    public Result toggleGrid() {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        user.setShowGrid(!user.isShowGrid());
        return saved(user, "Grid " + (user.isShowGrid() ? "shown." : "hidden."));
    }

    public Result toggleDebug() {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        user.setDebugMode(!user.isDebugMode());
        return saved(user, "Debug mode " + (user.isDebugMode() ? "on." : "off."));
    }

    public Result toggleUiEditMode() {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        user.setUiEditMode(!user.isUiEditMode());
        return saved(user, "UI edit mode " + (user.isUiEditMode() ? "on." : "off."));
    }

    public MenuType currentMenu() {
        return app.getCurrentmenu();
    }

    private User signedIn() {
        return app == null ? null : app.getCurrentuser();
    }

    private Result saved(User user, String message) {
        saves.persist(user);
        return new Result(true, message, null);
    }

    private Result failure(String message) {
        return new Result(false, message, null);
    }
}

package controller.menu;

import controller.Navigation;
import controller.SaveService;
import model.App;
import view.MenuType;
import view.SettingMenu;

public class SettingMenuController {

    private final App app;
    private final SettingMenu view;
    private final SaveService saves = new SaveService();

    public SettingMenuController(App app) {
        this.app = app;
        this.view = new SettingMenu();
    }

    public void handleCommand(String[] parts) {
        if (parts.length < 2) {
            view.showError("Invalid command.");
            return;
        }
        switch (parts[1]) {
            case "show":
                if (parts.length >= 3 && parts[2].equals("current"))
                    System.out.println("Current menu: " + app.getCurrentmenu());
                else
                    view.showError("Usage: menu show current");
                break;
            case "enter":
                handleEnter(parts);
                break;
            case "settings":
                handleSettings(parts);
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
    }

    private void handleSettings(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu settings <change-difficulty|set-speed|"
                    + "toggle-grid|toggle-debug|toggle-ui-edit>");
            return;
        }
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;
        }
        if (parts[2].equals("set-speed")) {
            handleSetSpeed(parts);
        } else if (parts[2].equals("toggle-grid")) {
            app.getCurrentuser().setShowGrid(!app.getCurrentuser().isShowGrid());
            saves.persist(app.getCurrentuser());
        } else if (parts[2].equals("toggle-debug")) {
            app.getCurrentuser().setDebugMode(!app.getCurrentuser().isDebugMode());
            saves.persist(app.getCurrentuser());
        } else if (parts[2].equals("toggle-ui-edit")) {
            app.getCurrentuser().setUiEditMode(!app.getCurrentuser().isUiEditMode());
            saves.persist(app.getCurrentuser());
        } else {
            handleChangeDifficulty(parts);
        }
    }

    private void handleSetSpeed(String[] parts) {
        if (parts.length < 5 || !parts[3].equals("-v")) {
            view.showError("Usage: menu settings set-speed -v <1-3>");
            return;
        }
        int speed;
        try {
            speed = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            view.showError("Invalid speed: " + parts[4]);
            return;
        }
        if (speed < 1 || speed > 3) {
            view.showError("Speed must be between 1 and 3.");
            return;
        }
        app.getCurrentuser().setGameSpeed(speed);
        saves.persist(app.getCurrentuser());
    }

    private void handleChangeDifficulty(String[] parts) {
        if (app.getCurrentuser() == null) {
            view.showError("No user is logged in.");
            return;
        }
        if (parts.length < 5 || !parts[2].equals("change-difficulty") || !parts[3].equals("-l")) {
            view.showError("Usage: menu settings change-difficulty -l <1-5>");
            return;
        }
        int level;
        try {
            level = Integer.parseInt(parts[4]);
        } catch (NumberFormatException e) {
            view.showError("Invalid level: " + parts[4]);
            return;
        }
        if (level < 1 || level > 5) {
            view.showInvalidDifficulty();
            return;
        }
        app.getCurrentuser().setDifficultyLevel(level);
        saves.persist(app.getCurrentuser());
        view.showDifficultyChanged(level);
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>");
            return;
        }
        String navError = Navigation.enter(app, parts[2]);
        if (navError != null) view.showError(navError);
    }
}

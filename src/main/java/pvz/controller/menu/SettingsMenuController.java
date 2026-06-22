package pvz.controller.menu;

import pvz.model.App;
import pvz.view.MenuType;
import pvz.view.SettingsMenu;

public class SettingsMenuController{

    private final App app;
    private final SettingsMenu view;

    public SettingsMenuController(App app) {
        this.app = app;
        this.view = new SettingsMenu();
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
                handleChangeDifficulty(parts);
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
    }

    private void handleChangeDifficulty(String[] parts) {
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
        view.showDifficultyChanged(level);
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>");
            return;
        }
        try {
            MenuType target = MenuType.valueOf(parts[2].toUpperCase());
            app.setCurrentmenu(target);
        } catch (IllegalArgumentException e) {
            view.showError("Unknown menu: " + parts[2]);
        }
    }
}

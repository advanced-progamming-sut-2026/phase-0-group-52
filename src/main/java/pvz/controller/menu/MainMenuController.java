package controller.menu;

import model.App;
import view.MainMenu;
import view.MenuType;

public class MainMenuController {

    private final App app;
    private final MainMenu view;

    public MainMenuController(App app) {
        this.app = app;
        this.view = new MainMenu();
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
            case "logout":
                app.getCurrentuser().setLogged(false);
                app.setCurrentuser(null);
                view.showLoggedOut();
                app.setCurrentmenu(MenuType.LOGIN_MENU);
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
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

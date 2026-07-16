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
        if (parts.length == 1 && parts[0].equals("logout"))
            parts = new String[]{"menu", "logout"};
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
                if (app.getCurrentuser() == null) { view.showError("No user is logged in."); break; }
                app.getCurrentuser().setLogged(false);
                app.setCurrentuser(null);
                view.showLoggedOut();
                app.setCurrentmenu(MenuType.LOGIN_MENU);
                app.setCurrentMenu(model.enums.Menu.LoginMenu);
                break;
            case "leaderboard":
                handleLeaderboard(parts);
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
    }

    private void handleLeaderboard(String[] parts) {
        String column = "score";
        boolean ascending = false;
        for (int i = 2; i + 1 < parts.length; i += 2) {
            if (parts[i].equals("-s")) column = parts[i + 1].toLowerCase();
            else if (parts[i].equals("-o")) ascending = parts[i + 1].equalsIgnoreCase("asc");
        }
        java.util.List<model.Leaderboard.Entry> entries =
                new model.Leaderboard().getEntries(column, ascending);
        view.showLeaderboard(entries, column, ascending);
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>");
            return;
        }
        try {
            MenuType target = MenuType.fromName(parts[2]);
            if (target == null) throw new IllegalArgumentException();
            app.setCurrentmenu(target);
            if (target.toMenu() != null) app.setCurrentMenu(target.toMenu());
        } catch (IllegalArgumentException e) {
            view.showError("Unknown menu: " + parts[2]);
        }
    }
}

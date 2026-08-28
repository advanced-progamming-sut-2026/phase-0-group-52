package controller;

import model.App;
import model.enums.MenuType;

public final class Navigation {

    private Navigation() {}

    public static String enter(App app, String targetName) {
        MenuType target = MenuType.fromName(targetName);
        if (target == null) return "Unknown menu: " + targetName;
        return enter(app, target);
    }

    public static String enter(App app, MenuType target) {
        if (target == null) return "Unknown menu.";

        boolean loggedIn = app.getCurrentuser() != null;
        boolean authMenu = (target == MenuType.LOGIN_MENU || target == MenuType.SIGNUP_MENU);

        if (!loggedIn && !authMenu)
            return "You must log in first.";
        if (loggedIn && authMenu)
            return "You are already logged in. Use 'menu logout' to sign out first.";

        if (target == MenuType.LOGIN_MENU) {
            MenuType from = app.getCurrentmenu();
            if (from != MenuType.SIGNUP_MENU && from != MenuType.MAIN_MENU)
                return "You can only reach the login menu from the sign-up or main menu.";
        }

        app.setCurrentmenu(target);
        app.setCurrentmenu(target);
        return null;
    }

    public static void go(App app, MenuType target) {
        app.setCurrentmenu(target);
        app.setCurrentmenu(target);
    }
}

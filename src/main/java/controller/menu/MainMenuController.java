package controller.menu;

import controller.Navigation;
import model.App;
import model.Leaderboard;
import model.Result;
import model.User;
import model.enums.MenuType;

import java.util.List;

public class MainMenuController {

    private final App app;

    public MainMenuController(App app) {
        this.app = app;
    }

    public Result enter(MenuType target) {
        if (target == null) {
            return new Result(false, "Unknown menu.", null);
        }
        String error = Navigation.enter(app, target);
        return error == null
                ? new Result(true, "Entered " + target + ".", target)
                : new Result(false, error, null);
    }

    public Result logout() {
        User user = app.getCurrentuser();
        if (user == null) {
            return new Result(false, "No user is signed in.", null);
        }
        user.setLogged(false);
        app.setCurrentuser(null);
        app.setCurrentmenu(MenuType.LOGIN_MENU);
        return new Result(true, "Signed out.", null);
    }

    public List<Leaderboard.Entry> leaderboard(String column, boolean ascending) {
        return new Leaderboard().getEntries(column, ascending);
    }

    public MenuType currentMenu() {
        return app.getCurrentmenu();
    }
}

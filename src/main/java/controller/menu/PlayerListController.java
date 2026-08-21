package controller.menu;

import database.UserRepository;
import model.App;
import model.Result;
import model.User;

import java.util.List;

public class PlayerListController {
    private final App app;
    private final UserRepository repository = new UserRepository();

    public PlayerListController(App app) {
        this.app = app;
    }

    public List<User> allPlayers() {
        return repository.getAllUsers();
    }

    public User signedIn() {
        return app.getCurrentuser();
    }

    public boolean isSignedIn(User user) {
        User current = app.getCurrentuser();
        return current != null && user != null && current.getId() == user.getId();
    }

    public Result delete(User user) {
        if (user == null) {
            return new Result(false, "Select a player first.", null);
        }
        if (!isSignedIn(user)) {
            return new Result(false, "You can only delete the account you are signed in to.", null);
        }
        if (!repository.delete(user.getId())) {
            return new Result(false, "That account no longer exists.", null);
        }
        app.setCurrentuser(null);
        App.loggedInUser = null;
        return new Result(true, "Account " + user.getUsername() + " deleted.", null);
    }

    public boolean isStaySignedIn() {
        User current = app.getCurrentuser();
        return current != null && current.isStayLoggedIn();
    }

    public Result setStaySignedIn(boolean value) {
        User current = app.getCurrentuser();
        if (current == null) {
            return new Result(false, "Sign in first.", null);
        }
        current.setStayLoggedIn(value);
        repository.setStayLoggedIn(current.getId(), value);
        return new Result(true, value
                ? "You will stay signed in next time."
                : "You will start signed out next time.", null);
    }

    public Result signOut() {
        User current = app.getCurrentuser();
        if (current == null) {
            return new Result(false, "Nobody is signed in.", null);
        }
        repository.setStayLoggedIn(current.getId(), false);
        current.setLogged(false);
        app.setCurrentuser(null);
        App.loggedInUser = null;
        return new Result(true, "Signed out.", null);
    }

    public int completedLevels(User user) {
        return (user == null) ? 0 : repository.getPassedLevels(user.getId());
    }
}

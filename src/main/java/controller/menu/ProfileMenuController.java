package controller.menu;

import controller.HashUtil;
import database.UserRepository;
import model.App;
import model.Result;
import model.User;

public class ProfileMenuController {

    public static final int MIN_USERNAME = 3;
    public static final int MIN_NICKNAME = 3;
    public static final int MAX_NICKNAME = 30;
    private static final String EMAIL_SHAPE = "^[^@]+@[^@]+\\.[^@]+$";

    private final App app;
    private final UserRepository repository = new UserRepository();

    public ProfileMenuController(App app) {
        this.app = app;
    }

    public Result changeUsername(String name) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        String value = name == null ? "" : name.trim();
        if (value.length() < MIN_USERNAME) {
            return failure("Username must be at least " + MIN_USERNAME + " characters.");
        }
        if (repository.getUserByUsername(value) != null) {
            return failure("Username already taken.");
        }
        user.setUsername(value);
        repository.updateUsername(user.getId(), value);
        return new Result(true, "Username changed to " + value + ".", value);
    }

    public Result changeNickname(String name) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        String value = name == null ? "" : name.trim();
        if (value.length() < MIN_NICKNAME || value.length() > MAX_NICKNAME) {
            return failure("Nickname length must be between "
                    + MIN_NICKNAME + " and " + MAX_NICKNAME + ".");
        }
        user.setNickname(value);
        repository.updateNickname(user.getId(), value);
        return new Result(true, "Nickname changed to " + value + ".", value);
    }

    public Result changeEmail(String value) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        String email = value == null ? "" : value.trim();
        if (!email.matches(EMAIL_SHAPE)) {
            return failure("Invalid email format.");
        }
        user.setEmail(email);
        repository.updateEmail(user.getId(), email);
        return new Result(true, "Email changed to " + email + ".", email);
    }

    public Result changePassword(String oldPassword, String newPassword) {
        User user = signedIn();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (!user.getPasswordHash().equals(HashUtil.hashPassword(oldPassword))) {
            return failure("Old password is incorrect.");
        }
        String hash = HashUtil.hashPassword(newPassword);
        user.setPasswordHash(hash);
        repository.updatePassword(user.getUsername(), hash);
        return new Result(true, "Password changed successfully.", null);
    }

    private User signedIn() {
        return app == null ? null : app.getCurrentuser();
    }

    private Result failure(String message) {
        return new Result(false, message, null);
    }
}

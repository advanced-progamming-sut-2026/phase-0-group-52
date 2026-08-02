package controller.menu;

import controller.HashUtil;
import controller.Navigation;
import database.UserRepository;
import model.App;
import model.User;
import model.enums.SecurityQuestions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileMenuController {

    private static final Pattern CHANGE_NICK = Pattern.compile("^profile\\s+change\\s+-n\\s+(.+)$");
    private static final Pattern CHANGE_EMAIL = Pattern.compile("^profile\\s+change\\s+-e\\s+(\\S+)$");
    private static final Pattern CHANGE_PASS = Pattern.compile("^profile\\s+change\\s+-p\\s+(\\S+)\\s+(\\S+)$");
    private static final Pattern DOC_NICK = Pattern.compile("^menu\\s+profile\\s+change-nickname\\s+-u\\s+(.+)$");
    private static final Pattern DOC_UNAME = Pattern.compile("^menu\\s+profile\\s+change-username\\s+-u\\s+(\\S+)$");
    private static final Pattern DOC_EMAIL = Pattern.compile("^menu\\s+profile\\s+change-email\\s+-e\\s+(\\S+)$");
    private static final Pattern DOC_PASS = Pattern.compile("^menu\\s+profile\\s+change-password\\s+-p\\s+(\\S+)\\s+-o\\s+(\\S+)$");

    private final App app;
    private final UserRepository repository = new UserRepository();

    public ProfileMenuController(App app) {
        this.app = app;
    }

    public void handleCommand(String line) {
        String command = line.trim();
        Matcher m;
        if (command.equals("show profile") || command.equals("menu profile show-info")) {
            if (requireUser() != null) showProfile(requireUser());
            return;
        }
        if ((m = DOC_UNAME.matcher(command)).matches()) { if (requireUser() != null) changeUsername(requireUser(), m.group(1)); return; }
        if ((m = DOC_NICK.matcher(command)).matches()) { if (requireUser() != null) changeNickname(requireUser(), m.group(1)); return; }
        if ((m = DOC_EMAIL.matcher(command)).matches()) { if (requireUser() != null) changeEmail(requireUser(), m.group(1)); return; }
        if ((m = DOC_PASS.matcher(command)).matches()) { if (requireUser() != null) changePassword(requireUser(), m.group(2), m.group(1)); return; }
        String[] parts = command.split("\\s+");
        if (parts[0].equals("menu")) {
            handleMenu(parts);
            return;
        }
        User user = requireUser();
        if (user == null) return;
        if ((m = CHANGE_NICK.matcher(command)).matches()) {
            changeNickname(user, m.group(1));
        } else if ((m = CHANGE_EMAIL.matcher(command)).matches()) {
            changeEmail(user, m.group(1));
        } else if ((m = CHANGE_PASS.matcher(command)).matches()) {
            changePassword(user, m.group(1), m.group(2));
        } else {
            System.out.println("invalid command");
        }
    }

    private User requireUser() {
        User user = app.getCurrentuser();
        if (user == null) System.out.println("Error: No user is logged in.");
        return user;
    }

    private void changeUsername(User user, String name) {
        String u = name.trim();
        if (u.length() < 3) { System.out.println("Error: Username must be at least 3 characters."); return; }
        if (repository.getUserByUsername(u) != null) { System.out.println("Error: Username already taken."); return; }
        user.setUsername(u);
        repository.updateUsername(user.getId(), u);
        System.out.println("Username changed to " + u + ".");
    }

    private void changeNickname(User user, String name) {
        String nick = name.trim();
        if (nick.length() < 3 || nick.length() > 30) {
            System.out.println("Error: Nickname length must be between 3 and 30.");
            return;
        }
        user.setNickname(nick);
        repository.updateNickname(user.getId(), nick);
        System.out.println("Nickname changed to " + nick + ".");
    }

    private void changeEmail(User user, String value) {
        String email = value.trim();
        if (!email.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            System.out.println("Error: Invalid email format.");
            return;
        }
        user.setEmail(email);
        repository.updateEmail(user.getId(), email);
        System.out.println("Email changed to " + email + ".");
    }

    private void changePassword(User user, String oldPass, String newPass) {
        if (!user.getPasswordHash().equals(HashUtil.hashPassword(oldPass))) {
            System.out.println("Error: Old password is incorrect.");
            return;
        }
        String hash = HashUtil.hashPassword(newPass);
        user.setPasswordHash(hash);
        repository.updatePassword(user.getUsername(), hash);
        System.out.println("Password changed successfully.");
    }

    private void showProfile(User user) {
        System.out.println("== Profile ==");
        System.out.println("Username:   " + user.getUsername());
        System.out.println("Nickname:   " + user.getNickname());
        System.out.println("Email:      " + user.getEmail());
        System.out.println("Gender:     " + user.getGender());
        System.out.println("Security Q: " + SecurityQuestions.getQuestionByIndex(user.getSecurityQuestion()));
        System.out.println("Coins:      " + user.getCoins());
        System.out.println("Diamonds:   " + user.getGems());
        System.out.println("Difficulty: " + user.getDifficultyLevel());
        System.out.println("High score: " + user.getMaxPoint());
        System.out.println("Meow Points:" + user.getMostMeowPoint());
        System.out.println("Minigames:  " + user.getMiniGamesPlayed());
        System.out.println("Commands: profile change -n <nickname> | profile change -e <email>"
                + " | profile change -p <old> <new>");
    }

    private void handleMenu(String[] parts) {
        if (parts.length >= 3 && parts[1].equals("show") && parts[2].equals("current")) {
            System.out.println("Current menu: " + app.getCurrentmenu());
            return;
        }
        if (parts.length >= 3 && parts[1].equals("enter")) {
            String navError = Navigation.enter(app, parts[2]);
            if (navError != null) System.out.println("Error: " + navError);
            return;
        }
        System.out.println("Error: Usage: menu show current  |  menu enter <menu_name>");
    }
}

package view.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import controller.menu.LoginMenuController;
import controller.menu.PlayerListController;
import controller.menu.ProfileMenuController;
import controller.menu.SignupMenuController;
import model.Result;
import model.User;
import model.enums.SecurityQuestions;

public final class AccountFormPopup extends Popup {
    public enum Mode { REGISTER, SIGN_IN, PROFILE }

    private final GameContext context;
    private final User target;
    private final Runnable onChanged;

    private final SignupMenuController signup = new SignupMenuController();
    private final LoginMenuController login = new LoginMenuController();
    private final ProfileMenuController profile;
    private final PlayerListController players;

    private TextField username;
    private TextField password;
    private TextField passwordConfirm;
    private TextField nickname;
    private TextField email;
    private SelectBox<String> gender;
    private SelectBox<String> question;
    private TextField answer;

    public AccountFormPopup(GameContext context, Mode mode, User target, Runnable onChanged) {
        super(context.ui(), titleFor(mode), 620f, 520f);
        this.context = context;
        this.target = target;
        this.onChanged = onChanged;
        this.profile = new ProfileMenuController(context.app());
        this.players = new PlayerListController(context.app());

        if (mode == Mode.REGISTER) {
            buildRegister();
        } else if (mode == Mode.SIGN_IN) {
            buildSignIn();
        } else {
            buildProfile();
        }
    }

    private static String titleFor(Mode mode) {
        if (mode == Mode.REGISTER) {
            return "Register";
        }
        if (mode == Mode.SIGN_IN) {
            return "Sign in";
        }
        return "Profile";
    }

    private void buildRegister() {
        Table form = new Table();
        form.defaults().pad(3f).left();

        username = new TextField("", ui.skin());
        password = passwordField();
        passwordConfirm = passwordField();
        nickname = new TextField("", ui.skin());
        email = new TextField("", ui.skin());
        gender = new SelectBox<String>(ui.skin());
        gender.setItems("male", "female");
        question = new SelectBox<String>(ui.skin());
        question.setItems(SecurityQuestions.getAllQuestions());
        answer = new TextField("", ui.skin());

        row(form, "Username", username);
        row(form, "Password", password);
        row(form, "Confirm", passwordConfirm);
        row(form, "Nickname", nickname);
        row(form, "Email", email);
        row(form, "Gender", gender);
        row(form, "Question", question);
        row(form, "Answer", answer);

        body().add(form).growX().row();
        footer().add(ui.button("Create account", new Runnable() {
            @Override
            public void run() {
                submitRegister();
            }
        })).height(46f).width(240f);
    }

    private void buildSignIn() {
        Table form = new Table();
        form.defaults().pad(3f).left();

        username = new TextField(target == null ? "" : target.getUsername(), ui.skin());
        username.setDisabled(target != null);
        password = passwordField();

        row(form, "Username", username);
        row(form, "Password", password);

        body().add(form).growX().row();
        footer().add(ui.button("Sign in", new Runnable() {
            @Override
            public void run() {
                submitSignIn();
            }
        })).height(46f).width(240f);
    }

    private Table profileDetails() {
        Table details = new Table();
        details.defaults().left().pad(2f);
        addStat(details, "Nickname", target == null ? "-" : target.getNickname());
        addStat(details, "Username", target == null ? "-" : target.getUsername());
        addStat(details, "Email", target == null ? "-" : target.getEmail());
        addStat(details, "Coins", String.valueOf(target == null ? 0 : target.getCoins()));
        addStat(details, "Gems", String.valueOf(target == null ? 0 : target.getGems()));
        addStat(details, "Games played", String.valueOf(target == null ? 0 : target.getGamesPlayed()));
        addStat(details, "Levels completed", String.valueOf(players.completedLevels(target)));
        addStat(details, "Best meow points",
                String.valueOf(target == null ? 0 : target.getMostMeowPoint()));
        addStat(details, "High score", String.valueOf(target == null ? 0 : target.getMaxPoint()));
        return details;
    }

    private void buildProfile() {
        boolean editable = players.isSignedIn(target);
        body().add(profileDetails()).growX().row();

        if (!editable) {
            Label note = new Label("Sign in to this account to edit it.", ui.skin(), "muted");
            note.setAlignment(Align.center);
            body().add(note).padTop(Theme.PAD);
            return;
        }

        body().add(ui.divider()).height(2f).growX().padTop(Theme.PAD).padBottom(Theme.PAD).row();

        Table form = new Table();
        form.defaults().pad(3f).left();
        nickname = new TextField("", ui.skin());
        email = new TextField("", ui.skin());
        password = passwordField();
        passwordConfirm = passwordField();

        editRow(form, "New nickname", nickname, new Runnable() {
            @Override
            public void run() {
                apply("menu profile change-nickname -u " + nickname.getText().trim(), nickname);
            }
        });
        editRow(form, "New email", email, new Runnable() {
            @Override
            public void run() {
                apply("menu profile change-email -e " + email.getText().trim(), email);
            }
        });
        editRow(form, "New password", password, null);
        editRow(form, "Current password", passwordConfirm, new Runnable() {
            @Override
            public void run() {
                changePassword();
            }
        });
        body().add(form).growX();
    }

    private TextField passwordField() {
        TextField field = new TextField("", ui.skin());
        field.setPasswordMode(true);
        field.setPasswordCharacter('*');
        return field;
    }

    private void row(Table table, String label, Actor field) {
        table.add(new Label(label, ui.skin(), "default")).right().padRight(Theme.PAD).width(140f);
        table.add(field).width(320f).height(32f).row();
    }

    private void editRow(Table table, String label, Actor field, Runnable action) {
        table.add(new Label(label, ui.skin(), "default")).right().padRight(Theme.PAD).width(150f);
        table.add(field).width(240f).height(32f);
        if (action == null) {
            table.add(new Table()).width(90f).row();
        } else {
            table.add(ui.button("Save", action)).width(90f).row();
        }
    }

    private void addStat(Table table, String label, String value) {
        table.add(new Label(label, ui.skin(), "muted")).left().padRight(Theme.PAD_LARGE).width(190f);
        table.add(new Label(value, ui.skin(), "default")).left().row();
    }

    private void submitRegister() {
        if (reject(signup.setUsername(username.getText().trim()), username)) {
            return;
        }
        if (reject(signup.setPassword(password.getText()), password)) {
            return;
        }
        if (reject(signup.setPasswordConfirm(passwordConfirm.getText(), password.getText()),
                passwordConfirm)) {
            return;
        }
        if (reject(signup.setNickname(nickname.getText().trim()), nickname)) {
            return;
        }
        if (reject(signup.setEmail(email.getText().trim()), email)) {
            return;
        }
        if (reject(signup.setGender(gender.getSelected()), gender)) {
            return;
        }
        String index = String.valueOf(question.getSelectedIndex() + 1);
        String reply = answer.getText().trim();
        if (reject(signup.setQuestion(index, reply, reply), answer)) {
            return;
        }
        context.toasts().success("Account created.");
        finish();
    }

    private void submitSignIn() {
        Result result = login.login(username.getText().trim(), password.getText(),
                players.isStaySignedIn());
        if (result == null || !result.isSuccess()) {
            context.toasts().error(firstLine(result == null ? null : result.getMessage()));
            Animations.shake(password);
            return;
        }
        context.toasts().success("Signed in as " + username.getText().trim() + ".");
        finish();
    }

    private void changePassword() {
        String next = password.getText().trim();
        String current = passwordConfirm.getText().trim();
        if (next.isEmpty() || current.isEmpty()) {
            context.toasts().error("Enter both the new and the current password.");
            return;
        }
        apply("menu profile change-password -p " + next + " -o " + current, password);
        passwordConfirm.setText("");
    }

    private void apply(String command, TextField source) {
        if (source.getText().trim().isEmpty()) {
            context.toasts().error("Type a value first.");
            Animations.shake(source);
            return;
        }
        profile.handleCommand(command);
        source.setText("");
        if (onChanged != null) {
            onChanged.run();
        }
    }

    private boolean reject(Result result, Actor field) {
        if (result == null || result.isSuccess()) {
            return false;
        }
        context.toasts().error(firstLine(result.getMessage()));
        Animations.shake(field);
        return true;
    }

    private String firstLine(String message) {
        if (message == null) {
            return "That did not work.";
        }
        String trimmed = message.trim();
        int newline = trimmed.indexOf('\n');
        return (newline < 0) ? trimmed : trimmed.substring(0, newline);
    }

    private void finish() {
        if (onChanged != null) {
            onChanged.run();
        }
        close();
    }
}

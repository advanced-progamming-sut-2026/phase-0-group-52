package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import controller.menu.LoginMenuController;
import model.Result;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;

/**
 * Sign-in, plus the password recovery flow.
 *
 * <p>Recovery runs in the same panel rather than a separate screen: the
 * specification leaves the presentation open, and keeping it here means the user
 * never loses the username they already typed.
 */
public final class LoginScreen extends BaseScreen {

    /** Which form the panel is currently showing. */
    private enum Mode { SIGN_IN, RECOVER_IDENTITY, RECOVER_ANSWER, RECOVER_PASSWORD }

    private final LoginMenuController controller = new LoginMenuController();

    private Mode mode = Mode.SIGN_IN;

    private TextField username;
    private TextField password;
    private CheckBox stayLoggedIn;

    private TextField recoveryEmail;
    private TextField recoveryAnswer;
    private TextField newPassword;

    private Label prompt;

    public LoginScreen(GameContext context) {
        super(context, "Sign in");
    }

    @Override
    protected void build() {
        Table panel = ui.panel();

        Label heading = new Label(mode == Mode.SIGN_IN ? "Welcome back" : "Recover your account",
                ui.skin(), "title");
        panel.add(heading).colspan(2).padBottom(Theme.PAD).row();

        prompt = new Label("", ui.skin(), "muted");
        prompt.setWrap(true);
        prompt.setAlignment(Align.left);

        switch (mode) {
            case SIGN_IN:
                buildSignIn(panel);
                break;
            case RECOVER_IDENTITY:
                buildRecoverIdentity(panel);
                break;
            case RECOVER_ANSWER:
                buildRecoverAnswer(panel);
                break;
            case RECOVER_PASSWORD:
                buildRecoverPassword(panel);
                break;
            default:
                buildSignIn(panel);
        }

        content.add(panel).center();
    }

    private void buildSignIn(Table panel) {
        username = new TextField("", ui.skin());
        password = new TextField("", ui.skin());
        password.setPasswordMode(true);
        password.setPasswordCharacter('*');
        stayLoggedIn = new CheckBox(" Keep me signed in", ui.skin());

        addRow(panel, "Username", username);
        addRow(panel, "Password", password);
        panel.add(stayLoggedIn).colspan(2).left().padTop(Theme.PAD_SMALL).row();

        Table actions = new Table();
        actions.add(ui.button("Sign in", new Runnable() {
            @Override
            public void run() {
                signIn();
            }
        })).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("Forgot password", new Runnable() {
            @Override
            public void run() {
                switchTo(Mode.RECOVER_IDENTITY);
            }
        })).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("Create account", new Runnable() {
            @Override
            public void run() {
                navigate("signup");
            }
        }));
        panel.add(actions).colspan(2).padTop(Theme.PAD).center().row();
    }

    private void buildRecoverIdentity(Table panel) {
        username = new TextField("", ui.skin());
        recoveryEmail = new TextField("", ui.skin());
        prompt.setText("Enter the username and the email it was registered with.");

        addRow(panel, "Username", username);
        addRow(panel, "Email", recoveryEmail);
        panel.add(prompt).colspan(2).width(430f).padTop(Theme.PAD_SMALL).row();
        panel.add(recoveryActions(new Runnable() {
            @Override
            public void run() {
                submitIdentity();
            }
        })).colspan(2).padTop(Theme.PAD).center().row();
    }

    private void buildRecoverAnswer(Table panel) {
        recoveryAnswer = new TextField("", ui.skin());
        addRow(panel, "Answer", recoveryAnswer);
        panel.add(prompt).colspan(2).width(430f).padTop(Theme.PAD_SMALL).row();
        panel.add(recoveryActions(new Runnable() {
            @Override
            public void run() {
                submitAnswer();
            }
        })).colspan(2).padTop(Theme.PAD).center().row();
    }

    private void buildRecoverPassword(Table panel) {
        newPassword = new TextField("", ui.skin());
        newPassword.setPasswordMode(true);
        newPassword.setPasswordCharacter('*');
        prompt.setText("Choose a new password: 8+ characters with upper, lower, digit and symbol.");

        addRow(panel, "New password", newPassword);
        panel.add(prompt).colspan(2).width(430f).padTop(Theme.PAD_SMALL).row();
        panel.add(recoveryActions(new Runnable() {
            @Override
            public void run() {
                submitNewPassword();
            }
        })).colspan(2).padTop(Theme.PAD).center().row();
    }

    private Table recoveryActions(Runnable confirm) {
        Table actions = new Table();
        actions.add(ui.button("Continue", confirm)).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("Back to sign in", new Runnable() {
            @Override
            public void run() {
                switchTo(Mode.SIGN_IN);
            }
        }));
        return actions;
    }

    private void addRow(Table table, String label, com.badlogic.gdx.scenes.scene2d.Actor field) {
        table.add(new Label(label, ui.skin(), "default")).right().padRight(Theme.PAD).width(150f);
        table.add(field).width(280f).height(34f).padBottom(Theme.PAD_SMALL).row();
    }

    // ------------------------------------------------------------- actions

    private void signIn() {
        Result result = controller.login(
                username.getText().trim(), password.getText(), stayLoggedIn.isChecked());
        if (result == null || !result.isSuccess()) {
            context.toasts().error(firstLine(result == null ? null : result.getMessage()));
            Animations.shake(password);
            return;
        }
        context.toasts().success("Signed in. Welcome back!");
        navigate("main");
    }

    private void submitIdentity() {
        Result result = controller.forgetPassword(
                username.getText().trim(), recoveryEmail.getText().trim());
        if (result == null || !result.isSuccess()) {
            context.toasts().error(firstLine(result == null ? null : result.getMessage()));
            return;
        }
        // The controller's message carries the user's security question.
        switchTo(Mode.RECOVER_ANSWER);
        prompt.setText(result.getMessage() == null ? "Answer your security question."
                : result.getMessage().trim());
    }

    private void submitAnswer() {
        Result result = controller.answerQuestion(recoveryAnswer.getText().trim());
        if (result == null || !result.isSuccess()) {
            context.toasts().error(firstLine(result == null ? null : result.getMessage()));
            switchTo(Mode.SIGN_IN);
            return;
        }
        switchTo(Mode.RECOVER_PASSWORD);
    }

    private void submitNewPassword() {
        Result result = controller.setNewPassword(newPassword.getText());
        if (result == null || !result.isSuccess()) {
            context.toasts().error(firstLine(result == null ? null : result.getMessage()));
            return;
        }
        context.toasts().success("Password changed. Sign in with the new one.");
        switchTo(Mode.SIGN_IN);
    }

    /** Rebuilds the panel in a different mode. */
    private void switchTo(Mode next) {
        mode = next;
        content.clear();
        build();
        Animations.enter(content);
    }

    /** Menu changes go through the controller so navigation rules stay in one place. */
    private void navigate(String menuName) {
        Result result = controller.enterMenu(menuName);
        if (result != null && !result.isSuccess()) {
            context.toasts().error(firstLine(result.getMessage()));
        }
    }

    private String firstLine(String message) {
        if (message == null) {
            return "That did not work.";
        }
        String trimmed = message.trim();
        int newline = trimmed.indexOf('\n');
        return (newline < 0) ? trimmed : trimmed.substring(0, newline);
    }

    @Override
    public void show() {
        // Always return to the sign-in form when the screen is re-entered.
        mode = Mode.SIGN_IN;
        super.show();
    }
}

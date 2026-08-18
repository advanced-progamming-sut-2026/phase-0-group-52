package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import controller.menu.SignupMenuController;
import model.Result;
import model.enums.SecurityQuestions;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;

/**
 * Account creation.
 *
 * <p>Runs in two steps because the controller does: the account fields are
 * validated one at a time, then a security question finalises registration. Each
 * field is checked by {@link SignupMenuController}, so the password and email rules
 * stay in the controller and this screen only reports what it is told.
 */
public final class SignupScreen extends BaseScreen {

    private final SignupMenuController controller = new SignupMenuController();

    private TextField username;
    private TextField password;
    private TextField passwordConfirm;
    private TextField nickname;
    private TextField email;
    private SelectBox<String> gender;

    private SelectBox<String> question;
    private TextField answer;
    private TextField answerConfirm;

    private Table stepOne;
    private Table stepTwo;
    private Table formHolder;
    private Label hint;
    private boolean onSecurityStep;

    public SignupScreen(GameContext context) {
        super(context, "Create an account");
    }

    @Override
    protected void build() {
        onSecurityStep = false;
        Table panel = ui.panel();
        panel.defaults().pad(Theme.PAD_SMALL).left();

        Label heading = new Label("New player", ui.skin(), "title");
        panel.add(heading).colspan(2).padBottom(Theme.PAD).row();

        buildAccountForm();
        buildSecurityForm();

        // Only one step is in the layout at a time. A hidden table would still
        // reserve its cell, leaving a gap the size of the form.
        formHolder = new Table();
        formHolder.add(stepOne);
        panel.add(formHolder).colspan(2).row();

        hint = new Label("Password needs 8+ characters with upper, lower, digit and symbol.",
                ui.skin(), "muted");
        hint.setWrap(true);
        hint.setAlignment(Align.left);
        panel.add(hint).colspan(2).width(420f).padTop(Theme.PAD_SMALL).row();

        Table actions = new Table();
        actions.add(ui.button("Continue", new Runnable() {
            @Override
            public void run() {
                submitStepOne();
            }
        })).padRight(Theme.PAD);
        actions.add(ui.secondaryButton("I already have an account", new Runnable() {
            @Override
            public void run() {
                goToLogin();
            }
        }));
        panel.add(actions).colspan(2).padTop(Theme.PAD).center();

        content.add(panel).center();
    }

    /** The account fields checked in the first step. */
    private void buildAccountForm() {
        username = new TextField("", ui.skin());
        password = new TextField("", ui.skin());
        password.setPasswordMode(true);
        password.setPasswordCharacter('*');
        passwordConfirm = new TextField("", ui.skin());
        passwordConfirm.setPasswordMode(true);
        passwordConfirm.setPasswordCharacter('*');
        nickname = new TextField("", ui.skin());
        email = new TextField("", ui.skin());

        gender = new SelectBox<String>(ui.skin());
        gender.setItems("male", "female");

        stepOne = new Table();
        stepOne.defaults().pad(Theme.PAD_SMALL).left();
        addRow(stepOne, "Username", username);
        addRow(stepOne, "Password", password);
        addRow(stepOne, "Confirm password", passwordConfirm);
        addRow(stepOne, "Nickname", nickname);
        addRow(stepOne, "Email", email);
        addRow(stepOne, "Gender", gender);
    }

    /** The security question that completes registration. */
    private void buildSecurityForm() {
        question = new SelectBox<String>(ui.skin());
        question.setItems(SecurityQuestions.getAllQuestions());
        answer = new TextField("", ui.skin());
        answerConfirm = new TextField("", ui.skin());

        stepTwo = new Table();
        stepTwo.defaults().pad(Theme.PAD_SMALL).left();
        addRow(stepTwo, "Security question", question);
        addRow(stepTwo, "Answer", answer);
        addRow(stepTwo, "Confirm answer", answerConfirm);
    }

    private void addRow(Table table, String label, com.badlogic.gdx.scenes.scene2d.Actor field) {
        table.add(new Label(label, ui.skin(), "default")).right().padRight(Theme.PAD).width(170f);
        table.add(field).width(280f).height(34f).row();
    }

    /**
     * Validates the account fields through the controller, one at a time, and
     * reveals the security question once they all pass.
     */
    private void submitStepOne() {
        if (onSecurityStep) {
            submitStepTwo();
            return;
        }

        Result result = controller.setUsername(username.getText().trim());
        if (reject(result, username)) {
            return;
        }
        result = controller.setPassword(password.getText());
        if (reject(result, password)) {
            return;
        }
        result = controller.setPasswordConfirm(passwordConfirm.getText(), password.getText());
        if (reject(result, passwordConfirm)) {
            return;
        }
        result = controller.setNickname(nickname.getText().trim());
        if (reject(result, nickname)) {
            return;
        }
        result = controller.setEmail(email.getText().trim());
        if (reject(result, email)) {
            return;
        }
        result = controller.setGender(gender.getSelected());
        if (reject(result, gender)) {
            return;
        }

        onSecurityStep = true;
        formHolder.clear();
        formHolder.add(stepTwo);
        hint.setText("Pick a question and give an answer you will remember. "
                + "It is the only way to recover the account.");
        Animations.enter(stepTwo);
        context.toasts().info("Details accepted. One more step.");
    }

    /** Finalises registration; the controller writes the user to storage. */
    private void submitStepTwo() {
        int index = question.getSelectedIndex() + 1;
        Result result = controller.setQuestion(
                String.valueOf(index), answer.getText().trim(), answerConfirm.getText().trim());
        if (reject(result, answer)) {
            return;
        }
        context.toasts().success("Account created. You can sign in now.");
        goToLogin();
    }

    /** Reports a failed step and shakes the offending field. Returns true if failed. */
    private boolean reject(Result result, com.badlogic.gdx.scenes.scene2d.Actor field) {
        if (result == null || result.isSuccess()) {
            return false;
        }
        context.toasts().error(firstLine(result.getMessage()));
        Animations.shake(field);
        return true;
    }

    /** Controller messages can carry several reasons; the toast shows the first. */
    private String firstLine(String message) {
        if (message == null) {
            return "That value was not accepted.";
        }
        String trimmed = message.trim();
        int newline = trimmed.indexOf('\n');
        return (newline < 0) ? trimmed : trimmed.substring(0, newline);
    }

    /**
     * Asks the controller to change menus. The router notices the model changed and
     * swaps the screen; this method never touches the screen stack itself.
     */
    private void goToLogin() {
        Result result = controller.enterMenu("login");
        if (result != null && !result.isSuccess()) {
            context.toasts().error(firstLine(result.getMessage()));
        }
    }
}

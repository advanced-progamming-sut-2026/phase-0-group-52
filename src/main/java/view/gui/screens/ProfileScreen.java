package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import controller.menu.ProfileMenuController;
import model.User;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;

public final class ProfileScreen extends BaseScreen {
    private final ProfileMenuController controller;

    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;
    private TextField oldPasswordField;
    private TextField newPasswordField;

    private Label statGames;
    private Label statLevels;
    private Label statMeow;
    private Label statScore;

    public ProfileScreen(GameContext context) {
        super(context, "Profile");
        this.controller = new ProfileMenuController(context.app());
    }

    @Override
    protected void build() {
        Table columns = new Table();
        columns.defaults().top().pad(Theme.PAD_SMALL);
        columns.add(buildStats()).width(360f);
        columns.add(buildEditor()).width(470f);
        content.add(columns).center();
    }

    private Table buildStats() {
        Table panel = ui.panel();
        panel.top();
        panel.add(new Label("At a glance", ui.skin(), "title")).left().padBottom(Theme.PAD).row();

        User user = context.user();
        Table box = ui.sunken();
        box.defaults().left().pad(2f);

        box.add(new Label("Username", ui.skin(), "muted")).left();
        box.add(new Label(user == null ? "-" : user.getUsername(), ui.skin(), "default")).right().row();
        box.add(new Label("Nickname", ui.skin(), "muted")).left();
        box.add(new Label(user == null ? "-" : user.getNickname(), ui.skin(), "default")).right().row();
        box.add(new Label("Email", ui.skin(), "muted")).left();
        box.add(new Label(user == null ? "-" : user.getEmail(), ui.skin(), "default")).right().row();

        statGames = new Label("0", ui.skin(), "default");
        statLevels = new Label("0", ui.skin(), "default");
        statMeow = new Label("0", ui.skin(), "default");
        statScore = new Label("0", ui.skin(), "default");

        box.add(new Label("Games played", ui.skin(), "muted")).left();
        box.add(statGames).right().row();
        box.add(new Label("Levels completed", ui.skin(), "muted")).left();
        box.add(statLevels).right().row();
        box.add(new Label("Best meow points", ui.skin(), "muted")).left();
        box.add(statMeow).right().row();
        box.add(new Label("High score", ui.skin(), "muted")).left();
        box.add(statScore).right().row();

        panel.add(box).growX();
        return panel;
    }

    private Table buildEditor() {
        Table panel = ui.panel();
        panel.top();
        panel.add(new Label("Edit details", ui.skin(), "title")).left().colspan(3)
                .padBottom(Theme.PAD).row();

        createFields();
        editRow(panel, "Username", usernameField, new Runnable() {
            @Override
            public void run() {
                send("menu profile change-username -u " + usernameField.getText().trim(), usernameField);
            }
        });
        editRow(panel, "Nickname", nicknameField, new Runnable() {
            @Override
            public void run() {
                send("menu profile change-nickname -u " + nicknameField.getText().trim(), nicknameField);
            }
        });
        editRow(panel, "Email", emailField, new Runnable() {
            @Override
            public void run() {
                send("menu profile change-email -e " + emailField.getText().trim(), emailField);
            }
        });

        panel.add(new Label("New password", ui.skin(), "default")).right()
                .padRight(Theme.PAD).width(120f);
        panel.add(newPasswordField).width(200f).height(32f).row();
        panel.add(new Label("Current password", ui.skin(), "default")).right()
                .padRight(Theme.PAD).width(120f);
        panel.add(oldPasswordField).width(200f).height(32f);
        panel.add(ui.button("Change", new Runnable() {
            @Override
            public void run() {
                changePassword();
            }
        })).padLeft(Theme.PAD).row();

        panel.add(ui.secondaryButton("Back to main menu", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("menu enter main");
            }
        })).colspan(3).padTop(Theme.PAD).center();

        return panel;
    }

    private void createFields() {
        usernameField = new TextField("", ui.skin());
        nicknameField = new TextField("", ui.skin());
        emailField = new TextField("", ui.skin());
        oldPasswordField = new TextField("", ui.skin());
        oldPasswordField.setPasswordMode(true);
        oldPasswordField.setPasswordCharacter('*');
        newPasswordField = new TextField("", ui.skin());
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
    }

    private void editRow(Table table, String label, final TextField field, Runnable apply) {
        table.add(new Label(label, ui.skin(), "default")).right().padRight(Theme.PAD).width(120f);
        table.add(field).width(200f).height(32f);
        table.add(ui.button("Save", apply)).padLeft(Theme.PAD).row();
    }

    private void changePassword() {
        String next = newPasswordField.getText().trim();
        String current = oldPasswordField.getText().trim();
        if (next.isEmpty() || current.isEmpty()) {
            context.toasts().error("Enter both the new and the current password.");
            return;
        }
        send("menu profile change-password -p " + next + " -o " + current, newPasswordField);
        newPasswordField.setText("");
        oldPasswordField.setText("");
    }

    private void send(String command, TextField source) {
        if (source.getText().trim().isEmpty()) {
            context.toasts().error("Type a value first.");
            view.gui.Animations.shake(source);
            return;
        }
        controller.handleCommand(command);
        source.setText("");
    }

    @Override
    protected void refresh() {
        User user = context.user();
        if (user == null || statGames == null) {
            return;
        }
        statGames.setText(String.valueOf(user.getGamesPlayed()));
        statLevels.setText(String.valueOf(
                Math.max(0, (user.getLastChapter() - 1) * 4 + (user.getLastLevel() - 1))));
        statMeow.setText(String.valueOf(user.getMostMeowPoint()));
        statScore.setText(String.valueOf(user.getMaxPoint()));
    }
}

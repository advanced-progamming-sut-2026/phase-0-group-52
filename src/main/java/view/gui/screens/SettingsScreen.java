package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import controller.menu.SettingMenuController;
import model.User;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;

public final class SettingsScreen extends BaseScreen {
    private final SettingMenuController controller;

    private final TextButton[] difficultyButtons = new TextButton[5];
    private final TextButton[] speedButtons = new TextButton[3];

    public SettingsScreen(GameContext context) {
        super(context, "Settings");
        this.controller = new SettingMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();
        panel.defaults().left().pad(Theme.PAD_SMALL);

        panel.add(new Label("Difficulty", ui.skin(), "title")).left().row();
        panel.add(new Label("Higher levels send tougher waves.", ui.skin(), "muted")).left().row();
        panel.add(buildDifficulty()).left().padBottom(Theme.PAD).row();

        panel.add(ui.divider()).height(2f).growX().padTop(Theme.PAD_SMALL)
                .padBottom(Theme.PAD_SMALL).row();

        panel.add(new Label("Game speed", ui.skin(), "title")).left().row();
        panel.add(new Label("How fast a level advances.", ui.skin(), "muted")).left().row();
        panel.add(buildSpeed()).left().padBottom(Theme.PAD).row();

        panel.add(ui.divider()).height(2f).growX().padTop(Theme.PAD_SMALL)
                .padBottom(Theme.PAD_SMALL).row();

        panel.add(buildGridToggle()).left().row();
        panel.add(buildDebugToggle()).left().row();

        panel.add(ui.secondaryButton("Back to main menu", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{"menu", "enter", "main"});
            }
        })).padTop(Theme.PAD).left();

        content.add(panel).width(560f).center();
    }

    private CheckBox buildGridToggle() {
        final CheckBox grid = new CheckBox(" Show the lawn grid", ui.skin());
        grid.setChecked(context.settings().isShowGrid());
        grid.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                context.settings().setShowGrid(grid.isChecked());
            }
        });
        return grid;
    }

    private CheckBox buildDebugToggle() {
        final CheckBox debug = new CheckBox(" Debug mode (cheat buttons)", ui.skin());
        debug.setChecked(context.settings().isDebugMode());
        debug.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                context.settings().setDebugMode(debug.isChecked());
                context.toasts().info(debug.isChecked()
                        ? "Debug mode on: cheat buttons are visible."
                        : "Debug mode off.");
            }
        });
        return debug;
    }

    private Table buildDifficulty() {
        Table row = new Table();
        for (int i = 1; i <= 5; i++) {
            final int level = i;
            TextButton button = ui.styledButton(String.valueOf(i), "secondary", new Runnable() {
                @Override
                public void run() {
                    setDifficulty(level);
                }
            });
            difficultyButtons[i - 1] = button;
            row.add(button).width(52f).padRight(Theme.PAD_SMALL);
        }
        markDifficulty();
        return row;
    }

    private Table buildSpeed() {
        Table row = new Table();
        String[] labels = {"1x", "2x", "3x"};
        for (int i = 0; i < labels.length; i++) {
            final int speed = i + 1;
            TextButton button = ui.styledButton(labels[i], "secondary", new Runnable() {
                @Override
                public void run() {
                    context.settings().setGameSpeed(speed);
                    markSpeed();
                    context.toasts().info("Game speed set to " + speed + "x.");
                }
            });
            speedButtons[i] = button;
            row.add(button).width(62f).padRight(Theme.PAD_SMALL);
        }
        markSpeed();
        return row;
    }

    private void setDifficulty(int level) {
        controller.handleCommand(new String[]{
                "menu", "settings", "change-difficulty", "-l", String.valueOf(level)});
        markDifficulty();
    }

    private void markDifficulty() {
        User user = context.user();
        int current = (user == null) ? 1 : user.getDifficultyLevel();
        for (int i = 0; i < difficultyButtons.length; i++) {
            boolean active = (i + 1) == current;
            difficultyButtons[i].setColor(active ? Theme.GREEN : Theme.PANEL_SUNKEN);
        }
    }

    private void markSpeed() {
        int current = context.settings().getGameSpeed();
        for (int i = 0; i < speedButtons.length; i++) {
            boolean active = (i + 1) == current;
            speedButtons[i].setColor(active ? Theme.GREEN : Theme.PANEL_SUNKEN);
        }
    }

    @Override
    protected void refresh() {
        markDifficulty();
    }
}

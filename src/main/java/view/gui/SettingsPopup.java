package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.SettingMenuController;
import model.User;

public final class SettingsPopup extends Popup {
    private final GameContext context;
    private final SettingMenuController controller;

    private Table grid;

    public SettingsPopup(GameContext context) {
        super(context.ui(), "Settings", 860f, 470f);
        this.context = context;
        this.controller = new SettingMenuController(context.app());
        grid = new Table();
        body().add(grid).grow();
        rebuild();
    }

    private void rebuild() {
        grid.clear();
        grid.top();
        grid.defaults().pad(3f).growX().uniformX();

        grid.add(valueRow("Difficulty", difficultyText(), new Runnable() {
            @Override
            public void run() {
                cycleDifficulty();
            }
        }));
        grid.add(valueRow("Game Speed", context.settings().getGameSpeed() + "x", new Runnable() {
            @Override
            public void run() {
                cycleSpeed();
            }
        }));
        grid.row();

        addToggles();

        grid.add(valueRow("Language", "English", null));
        grid.add(new Table());
        grid.row();
    }

    private void addToggles() {
        grid.add(toggleRow("Show Grid", context.settings().isShowGrid(), new Runnable() {
            @Override
            public void run() {
                context.settings().setShowGrid(!context.settings().isShowGrid());
                rebuild();
            }
        }));
        grid.add(toggleRow("Debug Mode", context.settings().isDebugMode(), new Runnable() {
            @Override
            public void run() {
                context.settings().setDebugMode(!context.settings().isDebugMode());
                rebuild();
            }
        }));
        grid.row();

        grid.add(toggleRow("Allow Cheat", context.settings().isDebugMode(), new Runnable() {
            @Override
            public void run() {
                context.settings().setDebugMode(!context.settings().isDebugMode());
                rebuild();
            }
        }));
        grid.add(toggleRow("Fullscreen (F11)", Display.isFullscreen(), new Runnable() {
            @Override
            public void run() {
                Display.toggle();
                rebuild();
            }
        }));
        grid.row();
    }

    private Table valueRow(String label, String value, Runnable action) {
        Table row = new Table();
        row.setBackground(ui.primitives().rounded(6,
                Theme.lighten(Theme.PANEL, 0.25f), Theme.OUTLINE_SOFT, 2));
        row.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        Label name = new Label(label, ui.skin(), "default");
        row.add(name).left().expandX();

        Label current = new Label(value, ui.skin(), "value");
        current.setAlignment(Align.right);
        row.add(current).right();

        if (action != null) {
            Animations.attachPress(row);
            UiKit.onClick(row, action);
        }
        return row;
    }

    private Table toggleRow(String label, boolean on, Runnable action) {
        Color face = on ? Theme.ROW_ON : Theme.ROW_OFF;
        Table row = new Table();
        row.setBackground(ui.primitives().rounded(6, face, Theme.darken(face, 0.3f), 2));
        row.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        Label name = new Label(label, ui.skin(), "onDark");
        row.add(name).left().expandX();

        Label state = new Label(on ? "ON" : "OFF", ui.skin(), "onDark");
        row.add(state).right();

        Animations.attachPress(row);
        UiKit.onClick(row, action);
        return row;
    }

    private String difficultyText() {
        User user = context.user();
        int level = (user == null) ? 1 : user.getDifficultyLevel();
        StringBuilder peppers = new StringBuilder();
        for (int i = 0; i < Math.max(1, level); i++) {
            peppers.append('*');
        }
        return peppers + "  (" + Math.max(1, level) + ")";
    }

    private void cycleDifficulty() {
        User user = context.user();
        if (user == null) {
            context.toasts().error("Sign in to change difficulty.");
            return;
        }
        int next = user.getDifficultyLevel() >= 5 ? 1 : user.getDifficultyLevel() + 1;
        controller.handleCommand(new String[]{
                "menu", "settings", "change-difficulty", "-l", String.valueOf(next)});
        rebuild();
    }

    private void cycleSpeed() {
        int next = context.settings().getGameSpeed() >= 3 ? 1 : context.settings().getGameSpeed() + 1;
        context.settings().setGameSpeed(next);
        rebuild();
    }
}

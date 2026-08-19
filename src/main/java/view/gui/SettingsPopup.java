package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.SettingMenuController;
import model.User;

public final class SettingsPopup extends Popup {
    private static final int DIFFICULTY_LEVELS = 5;
    private static final float CHILLI_SIZE = 34f;

    private final GameContext context;
    private final SettingMenuController controller;

    private Table grid;

    public SettingsPopup(GameContext context) {
        super(context.ui(), "Settings", 720f, 520f);
        this.context = context;
        this.controller = new SettingMenuController(context.app());
        grid = new Table();
        body().add(grid).grow();
        rebuild();
    }

    private void rebuild() {
        grid.clear();
        grid.top();
        grid.defaults().pad(4f).growX();

        grid.add(difficultyRow()).row();
        grid.add(valueRow("Game Speed", context.settings().getGameSpeed() + "x", new Runnable() {
            @Override
            public void run() {
                cycleSpeed();
            }
        })).row();

        addToggles();
    }

    private void addToggles() {
        grid.add(toggleRow("Show Grid", context.settings().isShowGrid(), new Runnable() {
            @Override
            public void run() {
                context.settings().setShowGrid(!context.settings().isShowGrid());
                rebuild();
            }
        })).row();
        grid.add(toggleRow("Debug Mode", context.settings().isDebugMode(), new Runnable() {
            @Override
            public void run() {
                context.settings().setDebugMode(!context.settings().isDebugMode());
                rebuild();
            }
        })).row();

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

    private Table difficultyRow() {
        Table row = new Table();
        row.setBackground(ui.primitives().rounded(6,
                Theme.lighten(Theme.PANEL, 0.25f), Theme.OUTLINE_SOFT, 2));
        row.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        row.add(new Label("Difficulty", ui.skin(), "default")).left().expandX();

        User user = context.user();
        int level = (user == null) ? 1 : Math.max(1, user.getDifficultyLevel());
        for (int i = 1; i <= DIFFICULTY_LEVELS; i++) {
            row.add(chilli(i, i <= level)).size(CHILLI_SIZE).padLeft(Theme.PAD_SMALL);
        }
        return row;
    }

    private com.badlogic.gdx.scenes.scene2d.ui.Image chilli(final int level, boolean lit) {
        com.badlogic.gdx.scenes.scene2d.ui.Image pip =
                new com.badlogic.gdx.scenes.scene2d.ui.Image(
                        ui.drawable(lit ? "chilliOn" : "chilliOff"));
        pip.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        Animations.attachPress(pip);
        UiKit.onClick(pip, new Runnable() {
            @Override
            public void run() {
                setDifficulty(level);
            }
        });
        return pip;
    }

    private void setDifficulty(int level) {
        User user = context.user();
        if (user == null) {
            context.toasts().error("Sign in to change difficulty.");
            return;
        }
        controller.handleCommand(new String[]{
                "menu", "settings", "change-difficulty", "-l", String.valueOf(level)});
        rebuild();
    }

    private void cycleSpeed() {
        int next = context.settings().getGameSpeed() >= 3 ? 1 : context.settings().getGameSpeed() + 1;
        context.settings().setGameSpeed(next);
        rebuild();
    }
}

package view.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.List;

public final class Overlays {
    private Overlays() {
    }

    public static Table levelStart(UiKit ui, String chapter, int level,
            List<String> objectives, final Runnable onBegin) {
        Table panel = ui.panel();
        panel.pad(Theme.PAD_LARGE);

        Label heading = new Label(chapter + "  -  Level " + level, ui.skin(), "title");
        heading.setAlignment(Align.center);
        panel.add(heading).padBottom(Theme.PAD_SMALL).row();

        Label subtitle = new Label("Your objectives", ui.skin(), "muted");
        subtitle.setAlignment(Align.center);
        panel.add(subtitle).padBottom(Theme.PAD).row();

        Table list = ui.sunken();
        list.left();
        if (objectives == null || objectives.isEmpty()) {
            list.add(new Label("Do not let the zombies reach your house.",
                    ui.skin(), "default")).left().row();
        } else {
            for (String objective : objectives) {
                Table row = new Table();
                row.add(ui.token(14, Theme.GREEN)).size(14f).padRight(Theme.PAD_SMALL);
                Label text = new Label(objective, ui.skin(), "default");
                text.setWrap(true);
                row.add(text).width(360f).left();
                list.add(row).growX().padBottom(4f).row();
            }
        }
        panel.add(list).width(420f).padBottom(Theme.PAD).row();

        panel.add(ui.button("Start", new Runnable() {
            @Override
            public void run() {
                onBegin.run();
            }
        })).width(180f).height(46f);

        return wrap(ui, panel);
    }

    public static Table pause(UiKit ui, final Runnable onResume,
            final Runnable onRestart, final Runnable onSaveAndExit) {
        Table panel = ui.panel();
        panel.pad(Theme.PAD_LARGE);

        Label heading = new Label("Paused", ui.skin(), "title");
        heading.setAlignment(Align.center);
        panel.add(heading).padBottom(Theme.PAD_LARGE).row();

        panel.add(ui.button("Resume", onResume)).width(240f).height(46f)
                .padBottom(Theme.PAD_SMALL).row();
        panel.add(ui.styledButton("Restart level", "info", onRestart)).width(240f).height(46f)
                .padBottom(Theme.PAD_SMALL).row();
        panel.add(ui.dangerButton("Save and quit", onSaveAndExit)).width(240f).height(46f);

        return wrap(ui, panel);
    }

    public static Table result(UiKit ui, boolean won, int score,
            final Runnable onRetry, final Runnable onExit) {
        Table panel = ui.panel();
        panel.pad(Theme.PAD_LARGE);

        Label heading = new Label(won ? "Level complete!" : "The zombies ate your brains",
                ui.skin(), "title");
        heading.setAlignment(Align.center);
        heading.setColor(won ? Theme.GREEN_DARK : Theme.RED);
        panel.add(heading).padBottom(Theme.PAD).row();

        Table box = ui.sunken();
        box.add(new Label("Score", ui.skin(), "muted")).left().padRight(Theme.PAD_LARGE);
        box.add(new Label(String.valueOf(score), ui.skin(), "default")).right().row();
        panel.add(box).width(300f).padBottom(Theme.PAD).row();

        Table actions = new Table();
        if (!won && onRetry != null) {
            actions.add(ui.button("Try again", onRetry)).width(160f).height(44f)
                    .padRight(Theme.PAD_SMALL);
        }
        actions.add(ui.secondaryButton("Leave", onExit)).width(160f).height(44f);
        panel.add(actions);

        return wrap(ui, panel);
    }

    private static Table wrap(UiKit ui, Table panel) {
        Table root = new Table();
        root.setFillParent(true);
        root.setTouchable(Touchable.enabled);

        root.setBackground(ui.drawable("scrim"));
        root.center();
        root.add(panel);

        Animations.enter(root);
        return root;
    }

    public static void show(Stage stage, Table overlay) {
        stage.addActor(overlay);
    }

    public static void dismiss(final Actor overlay) {
        Animations.exit(overlay, new Runnable() {
            @Override
            public void run() {
                overlay.remove();
            }
        });
    }
}

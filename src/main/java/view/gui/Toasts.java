package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import util.Log;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Toasts extends Group {
    private static final int MAX_VISIBLE = 4;
    private static final float LIFETIME = 3.2f;

    private final UiKit ui;
    private final Deque<Table> live = new ArrayDeque<Table>();

    private Log.Listener listener;

    private int ignoreBefore;

    public Toasts(UiKit ui) {
        this.ui = ui;
        setTouchable(Touchable.childrenOnly);
    }

    public void info(String message) {
        show(message, Theme.PANEL, Theme.OUTLINE, Theme.TEXT);
    }

    public void success(String message) {
        show(message, Theme.GREEN, Theme.GREEN_DARK, Theme.TEXT_ON_DARK);
    }

    public void error(String message) {
        show(message, Theme.RED, Theme.darken(Theme.RED, 0.3f), Theme.TEXT_ON_DARK);
    }

    public void fromLevel(Log.Level level, String message) {
        if (level == Log.Level.ERROR) {
            error(message);
        } else if (level == Log.Level.WARN) {
            show(message, Theme.SUN_DEEP, Theme.darken(Theme.SUN_DEEP, 0.3f), Theme.TEXT);
        } else {
            info(message);
        }
    }

    private void show(String message, Color face, Color border, Color text) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        final Table toast = new Table();
        toast.setBackground(ui.primitives().rounded(8, face, border, 2));
        Label label = new Label(message.trim(), ui.skin(), "small");
        label.setColor(text);
        label.setWrap(true);
        label.setAlignment(Align.left);
        toast.add(label).width(320f).pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);
        toast.pack();

        addActor(toast);
        toFront();
        live.addLast(toast);
        while (live.size() > MAX_VISIBLE) {
            Table oldest = live.pollFirst();
            if (oldest != null) {
                oldest.clearActions();
                oldest.remove();
            }
        }

        toast.getColor().a = 0f;
        toast.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.fadeIn(0.16f),
                        Actions.sequence(
                                Actions.moveBy(26f, 0f),
                                Actions.moveBy(-26f, 0f, 0.2f, com.badlogic.gdx.math.Interpolation.pow3Out))),
                Actions.delay(LIFETIME),
                Actions.fadeOut(0.28f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        live.remove(toast);
                        toast.remove();
                    }
                }),
                Actions.removeActor()));

        reflow();
    }

    private void reflow() {
        float y = 0f;
        Table[] items = live.toArray(new Table[0]);
        for (int i = items.length - 1; i >= 0; i--) {
            Table toast = items[i];
            toast.setPosition(getWidth() - toast.getWidth(), y);
            y += toast.getHeight() + Theme.PAD_SMALL;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        reflow();
    }

    public void listenToLog() {
        if (listener != null) {
            return;
        }
        ignoreBefore = Log.size();
        listener = new Log.Listener() {
            @Override
            public void onEntry(final Log.Entry entry) {
                if (entry.getLevel() == Log.Level.DEBUG || entry.getLevel() == Log.Level.INFO) {
                    return;
                }

                com.badlogic.gdx.Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fromLevel(entry.getLevel(), strip(entry.getMessage()));
                    }
                });
            }
        };
        Log.addListener(listener);
    }

    private String strip(String message) {
        if (message == null) {
            return "";
        }
        String trimmed = message.trim();
        if (trimmed.toLowerCase().startsWith("error:")) {
            return trimmed.substring(6).trim();
        }
        return trimmed;
    }

    public void stopListening() {
        if (listener != null) {
            Log.removeListener(listener);
            listener = null;
        }
    }

    public int getIgnoreBefore() {
        return ignoreBefore;
    }
}

package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import view.gui.Theme;
import view.gui.UiKit;

public final class LevelTrack extends Widget {
    private static final float BAR_HEIGHT = 13f;
    private static final float TICK_WIDTH = 2f;
    private static final int NODE = 15;
    private static final int MARKER = 23;
    private static final float SKULL_WIDTH = 27f;
    private static final float SKULL_HEIGHT = 23f;
    private static final float PREF_HEIGHT = 26f;

    private final UiKit ui;
    private final int segments;
    private final int completed;
    private final boolean[] special;
    private final TextureRegion skull;

    public LevelTrack(UiKit ui, int segments, int completed, boolean[] special,
            TextureRegion skull) {
        this.ui = ui;
        this.segments = Math.max(1, segments);
        this.completed = Math.max(0, Math.min(completed, Math.max(1, segments)));
        this.special = special;
        this.skull = skull;
    }

    @Override
    public float getPrefHeight() {
        return PREF_HEIGHT;
    }

    @Override
    public float getMinWidth() {
        return 0f;
    }

    @Override
    public float getMinHeight() {
        return PREF_HEIGHT;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float pad = MARKER / 2f;
        float left = getX() + pad;
        float span = getWidth() - pad * 2f;
        if (span <= 1f) {
            return;
        }
        float barY = getY() + (getHeight() - BAR_HEIGHT) / 2f;
        Color tint = getColor();
        float alpha = tint.a * parentAlpha;

        batch.setColor(tint.r, tint.g, tint.b, alpha);
        trough().draw(batch, left, barY, span, BAR_HEIGHT);

        float done = span * completed / segments;
        if (done >= BAR_HEIGHT) {
            fill().draw(batch, left, barY, done, BAR_HEIGHT);
        }

        Drawable tick = ui.primitives().flat(Theme.darken(Theme.OUTLINE, 0.15f));
        for (int i = 1; i < segments; i++) {
            tick.draw(batch, left + span * i / segments - TICK_WIDTH / 2f,
                    barY, TICK_WIDTH, BAR_HEIGHT);
        }

        marker(batch, left + span * completed / segments, barY, alpha);

        for (int i = 0; special != null && i < special.length && i < segments - 1; i++) {
            if (special[i]) {
                dot(batch, left + span * (i + 1) / segments, barY, alpha);
            }
        }
        boss(batch, left + span, barY, alpha);
        batch.setColor(Color.WHITE);
    }

    private void dot(Batch batch, float centreX, float barY, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(ui.primitives().circle(NODE, Theme.SUN_DEEP,
                        Theme.darken(Theme.SUN_DEEP, 0.45f), 2),
                centreX - NODE / 2f, barY + BAR_HEIGHT / 2f - NODE / 2f, NODE, NODE);
    }

    private void boss(Batch batch, float centreX, float barY, float alpha) {
        float y = barY + BAR_HEIGHT / 2f - SKULL_HEIGHT / 2f;
        if (skull == null) {
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(ui.primitives().circle(MARKER, Theme.LOCKED,
                            Theme.darken(Theme.LOCKED, 0.5f), 2),
                    centreX - MARKER / 2f, barY + BAR_HEIGHT / 2f - MARKER / 2f, MARKER, MARKER);
            return;
        }
        boolean cleared = completed >= segments;
        if (cleared) {
            batch.setColor(1f, 1f, 1f, alpha);
        } else {
            batch.setColor(0.62f, 0.62f, 0.66f, alpha);
        }
        batch.draw(skull, centreX - SKULL_WIDTH / 2f, y, SKULL_WIDTH, SKULL_HEIGHT);
    }

    private void marker(Batch batch, float centreX, float barY, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(ui.primitives().circle(MARKER, Theme.PANEL, Theme.OUTLINE, 3),
                centreX - MARKER / 2f, barY + BAR_HEIGHT / 2f - MARKER / 2f, MARKER, MARKER);
    }

    private Drawable trough() {
        return ui.primitives().rounded((int) (BAR_HEIGHT / 2f),
                Theme.darken(Theme.PANEL_SUNKEN, 0.62f), Theme.darken(Theme.OUTLINE, 0.25f), 2);
    }

    private Drawable fill() {
        return ui.primitives().rounded((int) (BAR_HEIGHT / 2f),
                Theme.GREEN, Theme.GREEN_DARK, 2);
    }
}

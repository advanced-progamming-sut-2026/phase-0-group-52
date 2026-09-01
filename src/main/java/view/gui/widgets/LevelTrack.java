package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import view.gui.Theme;
import view.gui.UiKit;

public final class LevelTrack extends Widget {
    private static final float BAR_HEIGHT = 18f;
    private static final float RIM = 3f;
    private static final float GLOSS_INSET = 4f;
    private static final float GLOSS_HEIGHT = 5f;
    private static final float TICK_WIDTH = 2f;
    private static final int NODE = 15;
    private static final int MARKER = 25;
    private static final int GLOW = 44;
    private static final float SKULL_WIDTH = 30f;
    private static final float SKULL_HEIGHT = 26f;
    private static final float PREF_HEIGHT = 32f;

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
        float done = span * completed / segments;

        batch.setColor(tint.r, tint.g, tint.b, alpha);
        trough().draw(batch, left, barY, span, BAR_HEIGHT);
        ticks(batch, left, span, barY, done);
        fill(batch, left, barY, done);
        marker(batch, left + done, barY, alpha);
        dots(batch, left, span, barY, alpha);
        boss(batch, left + span, barY, alpha);
        batch.setColor(Color.WHITE);
    }

    private void fill(Batch batch, float left, float barY, float done) {
        if (done < BAR_HEIGHT) {
            return;
        }
        bar().draw(batch, left, barY, done, BAR_HEIGHT);
        float glossWidth = done - GLOSS_INSET * 2f;
        if (glossWidth > GLOSS_HEIGHT * 2f) {
            gloss().draw(batch, left + GLOSS_INSET,
                    barY + BAR_HEIGHT - RIM - GLOSS_HEIGHT, glossWidth, GLOSS_HEIGHT);
        }
    }

    private void ticks(Batch batch, float left, float span, float barY, float done) {
        Drawable tick = ui.primitives().flat(Theme.alpha(Theme.OUTLINE, 0.45f));
        for (int i = 1; i < segments; i++) {
            float x = span * i / segments;
            if (x <= done) {
                continue;
            }
            tick.draw(batch, left + x - TICK_WIDTH / 2f,
                    barY + RIM, TICK_WIDTH, BAR_HEIGHT - RIM * 2f);
        }
    }

    private void dots(Batch batch, float left, float span, float barY, float alpha) {
        for (int i = 0; special != null && i < special.length && i < segments - 1; i++) {
            if (special[i]) {
                dot(batch, left + span * (i + 1) / segments, barY, alpha);
            }
        }
    }

    private void dot(Batch batch, float centreX, float barY, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(ui.primitives().circle(NODE, Theme.SUN,
                        Theme.darken(Theme.SUN_DEEP, 0.35f), 2),
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
            glow(batch, centreX, barY, alpha * 0.7f, Theme.SUN);
            batch.setColor(1f, 1f, 1f, alpha);
        } else {
            batch.setColor(0.62f, 0.62f, 0.66f, alpha);
        }
        batch.draw(skull, centreX - SKULL_WIDTH / 2f, y, SKULL_WIDTH, SKULL_HEIGHT);
    }

    private void glow(Batch batch, float centreX, float barY, float alpha, Color core) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(ui.primitives().radialGlow(GLOW, Theme.alpha(core, 0.85f)),
                centreX - GLOW / 2f, barY + BAR_HEIGHT / 2f - GLOW / 2f, GLOW, GLOW);
    }

    private void marker(Batch batch, float centreX, float barY, float alpha) {
        glow(batch, centreX, barY, alpha * 0.55f, Theme.GREEN_LIGHT);
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(ui.primitives().circle(MARKER, Theme.PANEL, Theme.OUTLINE, 3),
                centreX - MARKER / 2f, barY + BAR_HEIGHT / 2f - MARKER / 2f, MARKER, MARKER);
    }

    private Drawable trough() {
        return ui.primitives().rounded((int) (BAR_HEIGHT / 2f),
                Theme.darken(Theme.PANEL_SUNKEN, 0.68f), Theme.darken(Theme.OUTLINE, 0.3f), 3);
    }

    private Drawable bar() {
        return ui.primitives().rounded((int) (BAR_HEIGHT / 2f),
                Theme.GREEN, Theme.GREEN_DARK, 3);
    }

    private Drawable gloss() {
        return ui.primitives().rounded((int) (GLOSS_HEIGHT / 2f),
                Theme.alpha(Theme.GREEN_LIGHT, 0.75f),
                Theme.alpha(Theme.GREEN_LIGHT, 0.35f), 1);
    }
}

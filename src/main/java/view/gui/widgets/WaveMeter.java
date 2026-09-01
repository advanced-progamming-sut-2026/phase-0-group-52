package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Assets;

public final class WaveMeter extends Widget {

    private static final float PREF_WIDTH = 340f;
    private static final float PREF_HEIGHT = 46f;
    private static final float FLAG_HEIGHT = 1.15f;
    private static final float FLAG_RISE = 0.82f;
    private static final float HEAD = 58f;
    private static final float INSET = 6f;

    private final Assets assets;

    private int waves;
    private float progress;

    public WaveMeter(Assets assets) {
        this.assets = assets;
    }

    public void set(int waveCount, float value) {
        waves = waveCount;
        progress = Math.max(0f, Math.min(1f, value));
    }

    @Override
    public float getPrefWidth() {
        return PREF_WIDTH;
    }

    @Override
    public float getPrefHeight() {
        return PREF_HEIGHT;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (assets == null) {
            return;
        }
        float alpha = getColor().a * parentAlpha;
        batch.setColor(1f, 1f, 1f, alpha);

        TextureRegion trough = assets.region("IMAGE_UI_HUD_INGAME_PROGRESS_METER");
        if (trough != null) {
            batch.draw(trough, getX(), getY(), getWidth(), getHeight());
        }

        float runX = getX() + INSET;
        float runWidth = getWidth() - INSET * 2f;
        TextureRegion fill = assets.region("IMAGE_UI_HUD_INGAME_PROGRESS_METER_FILL");
        if (fill != null && progress > 0f) {
            float span = runWidth * progress;
            batch.draw(fill, runX + runWidth - span, getY() + INSET,
                    span, getHeight() - INSET * 2f);
        }

        flags(batch, runX, runWidth);

        TextureRegion head = assets.region("IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD");
        if (head != null) {
            float at = progress >= 0.999f ? runX
                    : runX + runWidth * (1f - progress);
            batch.draw(head, at - HEAD / 2f,
                    getY() + getHeight() / 2f - HEAD / 2f, HEAD, HEAD);
        }
        batch.setColor(Color.WHITE);
    }

    private void flags(Batch batch, float runX, float runWidth) {
        TextureRegion pole =
                assets.region("IMAGE_ZOMBIE_LNY_FLAG_ZOMBIE_LNY_FLAG_ZOMBIE_123X95");
        if (pole == null) {
            return;
        }
        float height = getHeight() * FLAG_HEIGHT;
        float width = height * pole.getRegionWidth() / (float) pole.getRegionHeight();
        for (int i = 1; i <= waves; i++) {
            float at = runX + runWidth * (1f - i / (float) Math.max(1, waves));
            batch.draw(pole, at - width / 2f,
                    getY() + getHeight() - height * FLAG_RISE, width, height);
        }
    }
}

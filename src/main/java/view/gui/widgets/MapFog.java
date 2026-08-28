package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import view.gui.Primitives;
import view.gui.Theme;

public final class MapFog extends Widget {

    private static final int BANDS = 18;
    private static final float PEAK = 0.20f;
    private static final float RISE = 2.2f;

    private final TextureRegion cloud;

    public MapFog(Primitives primitives) {
        setTouchable(Touchable.disabled);
        this.cloud = new TextureRegion(primitives.radialGlow(128,
                Theme.alpha(Theme.PORTAL_FOG, 1f)));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float alpha = getColor().a * parentAlpha;
        float step = getWidth() / BANDS;
        float size = getHeight() * RISE;
        for (int i = 0; i < BANDS; i++) {
            float ramp = (i + 1f) / BANDS;
            batch.setColor(1f, 1f, 1f, alpha * PEAK * ramp * ramp);
            batch.draw(cloud, getX() + step * i - size / 2f + step / 2f,
                    getY() + getHeight() / 2f - size / 2f, size, size);
        }
        batch.setColor(Color.WHITE);
    }
}

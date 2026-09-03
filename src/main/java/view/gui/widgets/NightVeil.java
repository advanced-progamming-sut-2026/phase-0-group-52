package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

public final class NightVeil extends Widget {

    public static final float NEAR_BRIGHT = 0.2f;
    public static final float FAR_BRIGHT = 0.7f;
    public static final float ENTITY_BRIGHT = 0.6f;

    private static final int STEPS = 128;
    private static final float FALLOFF = 2.4f;

    private final Texture ramp;

    public NightVeil() {
        setTouchable(Touchable.disabled);
        Pixmap map = new Pixmap(STEPS, STEPS, Pixmap.Format.RGBA8888);
        map.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < STEPS; y++) {
            for (int x = 0; x < STEPS; x++) {
                float dx = (STEPS - 1 - x) / (float) (STEPS - 1);
                float dy = y / (float) (STEPS - 1);
                float far = Math.min(1f, (float) Math.sqrt(dx * dx + dy * dy)
                        / (float) Math.sqrt(2d));
                float eased = (float) (1d - Math.exp(-FALLOFF * far)) 
                        / (float) (1d - Math.exp(-FALLOFF));
                float bright = NEAR_BRIGHT + (FAR_BRIGHT - NEAR_BRIGHT) * eased;
                map.setColor(0f, 0f, 0.04f, 1f - bright);
                map.drawPixel(x, y);
            }
        }
        ramp = new Texture(map);
        ramp.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        map.dispose();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        validate();
        Color was = batch.getColor().cpy();
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.draw(ramp, getX(), getY(), getWidth(), getHeight());
        batch.setColor(was);
    }
}

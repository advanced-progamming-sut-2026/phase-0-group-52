package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import model.ChapterType;
import view.gui.Assets;
import view.gui.WorldMapArt;

import java.util.ArrayList;
import java.util.List;

public final class MapBackdrop extends Widget {

    private static final float[] PARALLAX = {0.22f, 0.44f};
    private static final float[] HEIGHT = {0.66f, 0.34f};
    private static final float[] SCALE = {1.5f, 1.1f};
    private static final float[] ALPHA = {0.55f, 0.42f};
    private static final float REPEAT = 1.4f;
    private static final float FAR_PARALLAX = 0.09f;
    private static final float FAR_SCALE = 0.34f;
    private static final float FAR_ALPHA = 0.34f;
    private static final float FAR_SHIFT = 0.12f;
    private static final int FAR_COUNT = 18;
    private static final float[] FAR_JITTER =
            {1f, 0.72f, 1.24f, 0.88f, 1.1f, 0.64f, 1.35f};
    private static final float[] FAR_Y =
            {0.82f, 0.22f, 0.74f, 0.16f, 0.88f, 0.28f, 0.70f, 0.12f,
             0.92f, 0.19f, 0.78f, 0.25f, 0.85f};

    private final List<Piece> pieces = new ArrayList<Piece>();
    private float offset;

    public MapBackdrop(Assets assets, ChapterType chapter, float span) {
        setTouchable(Touchable.disabled);
        if (assets == null) {
            return;
        }
        distant(assets, chapter, span);
        int[] mist = WorldMapArt.nebulas(chapter);
        for (int band = 0; band < mist.length; band++) {
            TextureRegion art = assets.region(WorldMapArt.island(chapter, mist[band]));
            if (art == null) {
                continue;
            }
            float scale = SCALE[band % SCALE.length];
            float stride = art.getRegionWidth() * scale * REPEAT;
            float reach = span + stride;
            for (float x = -stride; x < reach; x += stride) {
                pieces.add(new Piece(art, x, HEIGHT[band % HEIGHT.length],
                        PARALLAX[band % PARALLAX.length], scale,
                        ALPHA[band % ALPHA.length]));
            }
        }
    }

    private void distant(Assets assets, ChapterType chapter, float span) {
        int[] far = WorldMapArt.distant(chapter);
        if (far.length == 0) {
            return;
        }
        float reach = span * FAR_PARALLAX + view.gui.Theme.WORLD_WIDTH;
        float step = reach / (FAR_COUNT + 1);
        for (int i = 0; i < FAR_COUNT; i++) {
            TextureRegion art =
                    assets.region(WorldMapArt.island(chapter, far[i % far.length]));
            if (art != null) {
                pieces.add(new Piece(art, step * (i + 1) - reach * FAR_SHIFT,
                        FAR_Y[i % FAR_Y.length], FAR_PARALLAX,
                        FAR_SCALE * FAR_JITTER[i % FAR_JITTER.length], FAR_ALPHA));
            }
        }
    }

    public void setOffset(float value) {
        this.offset = value;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float alpha = getColor().a * parentAlpha;
        for (Piece piece : pieces) {
            float width = piece.art.getRegionWidth() * piece.scale;
            float height = piece.art.getRegionHeight() * piece.scale;
            float x = getX() + piece.x - offset * piece.parallax;
            if (x + width < getX() || x > getX() + getWidth()) {
                continue;
            }
            batch.setColor(1f, 1f, 1f, alpha * piece.alpha);
            batch.draw(piece.art, x, getY() + getHeight() * piece.y - height / 2f,
                    width, height);
        }
        batch.setColor(Color.WHITE);
    }

    private static final class Piece {
        private final TextureRegion art;
        private final float x;
        private final float y;
        private final float parallax;
        private final float scale;
        private final float alpha;

        private Piece(TextureRegion art, float x, float y, float parallax,
                float scale, float alpha) {
            this.art = art;
            this.x = x;
            this.y = y;
            this.parallax = parallax;
            this.scale = scale;
            this.alpha = alpha;
        }
    }
}

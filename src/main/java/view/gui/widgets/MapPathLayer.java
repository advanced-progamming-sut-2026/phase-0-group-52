package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import model.ChapterType;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.WorldMapArt;

import java.util.ArrayList;
import java.util.List;

public final class MapPathLayer extends Widget {

    private static final float BRIGHT_THICK = 13f;
    private static final float FAINT_THICK = 7f;
    private static final float BRIGHT_ALPHA = 0.95f;
    private static final float FAINT_ALPHA = 0.35f;
    private static final float GLOW = 34f;
    private static final float INSET = 0.12f;
    private static final int MAX_MOTES = 60;
    private static final float MOTE_CHANCE = 0.012f;
    private static final float MOTE_SLOW = 0.22f;
    private static final float MOTE_FAST = 0.48f;
    private static final float MOTE_SPREAD = 11f;
    private static final float MOTE_SIZE = 7f;
    private static final float MOTE_ALPHA = 0.9f;

    private final TextureRegion beam;
    private final TextureRegion glow;
    private final List<float[]> legs = new ArrayList<float[]>();
    private final List<float[]> motes = new ArrayList<float[]>();

    public MapPathLayer(UiKit ui, Assets assets, ChapterType chapter) {
        TextureRegion line = assets == null
                ? null : assets.region(WorldMapArt.pathBeam(chapter));
        beam = line != null ? line
                : new TextureRegion(ui.primitives().rect(8, 2, Theme.HIGHLIGHT));
        glow = assets == null ? null : assets.region(WorldMapArt.pathGlow(chapter));
        setTouchable(Touchable.disabled);
    }

    public void connect(float fromX, float fromY, float toX, float toY, boolean cleared) {
        legs.add(new float[]{fromX, fromY, toX, toY, cleared ? 1f : 0f});
    }

    public void clearLegs() {
        legs.clear();
        motes.clear();
    }

    private void spawn() {
        for (int i = 0; i < legs.size(); i++) {
            if (legs.get(i)[4] > 0.5f && motes.size() < MAX_MOTES
                    && MathUtils.random() < MOTE_CHANCE) {
                motes.add(new float[]{i, 0f, MathUtils.random(MOTE_SLOW, MOTE_FAST),
                    MathUtils.random(-MOTE_SPREAD, MOTE_SPREAD)});
            }
        }
    }

    private void advance(float delta) {
        spawn();
        for (int i = motes.size() - 1; i >= 0; i--) {
            float[] mote = motes.get(i);
            mote[1] += delta * mote[2];
            if (mote[1] >= 1f) {
                motes.remove(i);
            }
        }
    }

    private void drawMotes(Batch batch) {
        for (int i = 0; i < motes.size(); i++) {
            float[] mote = motes.get(i);
            float[] leg = legs.get((int) mote[0]);
            float travel = mote[1];
            float x = getX() + leg[0] + (leg[2] - leg[0]) * travel;
            float y = getY() + leg[1] + (leg[3] - leg[1]) * travel + mote[3];
            float fade = MathUtils.sin(travel * MathUtils.PI);
            batch.setColor(Theme.HIGHLIGHT.r, Theme.HIGHLIGHT.g, Theme.HIGHLIGHT.b,
                    fade * MOTE_ALPHA);
            batch.draw(beam, x - MOTE_SIZE / 2f, y - MOTE_SIZE / 2f, MOTE_SIZE, MOTE_SIZE);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        advance(com.badlogic.gdx.Gdx.graphics.getDeltaTime());
        float base = getColor().a * parentAlpha;
        for (float[] leg : legs) {
            boolean cleared = leg[4] > 0.5f;
            batch.setColor(1f, 1f, 1f,
                    base * (cleared ? BRIGHT_ALPHA : FAINT_ALPHA));
            stretch(batch, leg, cleared ? BRIGHT_THICK : FAINT_THICK, cleared);
        }
        drawMotes(batch);
        batch.setColor(Color.WHITE);
    }

    private void stretch(Batch batch, float[] leg, float thick, boolean cleared) {
        float dx = leg[2] - leg[0];
        float dy = leg[3] - leg[1];
        float fromX = getX() + leg[0] + dx * INSET;
        float fromY = getY() + leg[1] + dy * INSET;
        float run = dx * (1f - INSET * 2f);
        float rise = dy * (1f - INSET * 2f);
        float length = (float) Math.sqrt(run * run + rise * rise);
        float angle = (float) Math.toDegrees(Math.atan2(rise, run));
        batch.draw(beam, fromX, fromY - thick / 2f, 0f, thick / 2f,
                length, thick, 1f, 1f, angle);
        if (cleared && glow != null) {
            cap(batch, fromX, fromY);
            cap(batch, fromX + run, fromY + rise);
        }
    }

    private void cap(Batch batch, float x, float y) {
        batch.draw(glow, x - GLOW / 2f, y - GLOW / 2f, GLOW, GLOW);
    }
}

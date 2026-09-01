package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import model.ChapterType;
import model.entities.zombies.ZombieRecord;
import view.gui.Assets;
import view.gui.ChapterArt;
import view.gui.LawnGeometry;
import view.gui.Theme;

public final class LawnView extends WidgetGroup {

    private static final float DEFAULT_ZOMBIE_WIDTH = 150f;
    private static final float DEFAULT_ZOMBIE_HEIGHT = 200f;
    private static final float ZOMBIE_SCALE = 0.78f;
    private static final float DEPTH_STEP = 0.03f;
    private static final int SEAM = 1;

    private final Assets assets;
    private final Group world = new Group();
    private final java.util.List<Actor> performers = new java.util.ArrayList<Actor>();

    private float lawnWidth = Theme.WORLD_WIDTH;
    private float camera;

    public LawnView(Assets assets, ChapterType chapter) {
        this.assets = assets;
        setTouchable(Touchable.disabled);
        setFillParent(true);
        addActor(world);
        addBackdrop(chapter);
    }

    public float lawnWidth() {
        return lawnWidth;
    }

    public float maxCamera() {
        return Math.max(0f, lawnWidth - Theme.WORLD_WIDTH);
    }

    public float camera() {
        return camera;
    }

    public void setCamera(float value) {
        camera = Math.max(0f, Math.min(value, maxCamera()));
        world.setX(-Math.round(camera));
    }

    public void addPerformer(Actor actor, float x, int row,
            float nativeWidth, float nativeHeight) {
        float depth = 1f - (LawnGeometry.ROWS - 1 - row) * DEPTH_STEP;
        float width = LawnGeometry.scaled(nativeWidth) * ZOMBIE_SCALE * depth;
        float height = LawnGeometry.scaled(nativeHeight) * ZOMBIE_SCALE * depth;
        actor.setBounds(x - width / 2f, LawnGeometry.rowFeet(row), width, height);
        world.addActor(actor);
        performers.add(actor);
    }

    public void clearPerformers() {
        for (Actor actor : performers) {
            actor.remove();
        }
        performers.clear();
    }

    public Actor addZombie(ZombieRecord record, int index) {
        Actor performer = performer(record);
        if (performer == null) {
            return null;
        }
        Rectangle box = boxOf(record);
        addPerformer(performer, LawnGeometry.musterX(index), LawnGeometry.rowOf(index),
                box == null ? DEFAULT_ZOMBIE_WIDTH : box.width,
                box == null ? DEFAULT_ZOMBIE_HEIGHT : box.height);
        return performer;
    }

    private Rectangle boxOf(ZombieRecord record) {
        if (assets == null || record == null || !record.hasAnimation()) {
            return null;
        }
        return assets.player().bounds(record.getAnimationPath(), clipOf(record));
    }

    private Actor performer(ZombieRecord record) {
        if (assets == null || record == null) {
            return null;
        }
        String clip = clipOf(record);
        if (record.isComposite()) {
            HeadSwapActor swap = new HeadSwapActor(assets, record.getBodyPath(),
                    record.getHeadPath(), clip, record.getHideParts());
            return swap.isReady() ? swap : null;
        }
        if (!record.hasAnimation()) {
            return null;
        }
        PamActor actor = new PamActor(assets, record.getAnimationPath(), clip).setFit(true);
        return actor.isReady() ? actor : null;
    }

    private static String clipOf(ZombieRecord record) {
        java.util.List<String> clips = record.getClips();
        if (clips == null || clips.isEmpty()) {
            return "idle";
        }
        if (clips.contains("idle")) {
            return "idle";
        }
        return clips.contains("walk") ? "walk" : clips.get(0);
    }

    private void addBackdrop(ChapterType chapter) {
        String prefix = "IMAGE_BACKGROUNDS_" + ChapterArt.world(chapter) + "_TEXTURE";
        TextureRegion middle = region(prefix);
        if (middle == null) {
            return;
        }
        float x = 0f;
        x = lay(region(prefix + "_LEFT"), x);
        x = lay(middle, x);
        x = lay(region(prefix + "_RIGHT"), x);
        lawnWidth = LawnGeometry.scaled(x);
    }

    private float lay(TextureRegion art, float nativeX) {
        if (art == null) {
            return nativeX;
        }
        place(art, nativeX);
        return nativeX + art.getRegionWidth();
    }

    private void place(TextureRegion art, float nativeX) {
        Image piece = new Image(new TextureRegionDrawable(art));
        piece.setScaling(Scaling.stretch);
        float height = LawnGeometry.scaled(art.getRegionHeight());
        float top = LawnGeometry.scaled(LawnGeometry.NATIVE_HEIGHT);
        piece.setBounds(Math.round(LawnGeometry.scaled(nativeX)),
                Math.round(top - height),
                Math.round(LawnGeometry.scaled(art.getRegionWidth())) + SEAM,
                Math.round(height));
        world.addActor(piece);
    }

    private TextureRegion region(String imageId) {
        return assets == null ? null : assets.region(imageId);
    }

    @Override
    public void layout() {
    }

    @Override
    public float getPrefWidth() {
        return 0f;
    }

    @Override
    public float getPrefHeight() {
        return 0f;
    }
}

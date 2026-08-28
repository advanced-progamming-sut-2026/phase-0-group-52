package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import model.ChapterType;
import model.adventure.MapNode;
import model.adventure.MapNodeKind;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.WorldMapArt;

public final class MapNodeActor extends WidgetGroup {

    public enum State { CLEARED, CURRENT, LOCKED }

    private static final float NUMBER_DROP = 0.46f;
    private static final float NUMBER_CAP = 96f;
    private static final float BOSS_NODE = 0.26f;
    private static final float NUMBER_GAP = 22f;
    private static final float SKULL_SHARE = 0.52f;
    private static final float SKULL_LIFT = 0.30f;
    private static final float HOLOGRAM_SCALE = 2.1f;
    private static final float HOLOGRAM_LIFT = 0.85f;
    private static final float HIT_PAD = 14f;
    private static final float BOSS_SINK = 0.42f;

    private final MapNode node;
    private final State state;
    private final Assets art;
    private final String rigPath;

    private final PamActor rig;
    private PamActor dome;
    private Image skull;
    private PamActor hologram;
    private Label number;

    public MapNodeActor(UiKit ui, Assets assets, ChapterType chapter,
            MapNode node, State state) {
        this.node = node;
        this.state = state;
        this.art = assets;
        this.rigPath = path(chapter);
        setTransform(false);

        this.rig = WorldMapArt.rigged(assets, rigPath, stableClip(), clip());
        addActor(rig);

        if (node.getKind() == MapNodeKind.ZOMBOSS) {
            dome = WorldMapArt.rigged(assets, WorldMapArt.LEVEL_NODE,
                    WorldMapArt.NODE_CLEARED, levelClip());
            dome.setColor(Theme.RED_LIGHT);
            addActor(dome);
            skull = skullMark(assets, chapter);
            if (state != State.LOCKED) {
                hologram = WorldMapArt.rigged(assets, WorldMapArt.HOLOGRAM, "idle", "idle");
                addActor(hologram);
            }
        }
        addNumber(ui);
    }

    public MapNode node() {
        return node;
    }

    public boolean isPlayable() {
        return state != State.LOCKED;
    }

    public void playOpening(Runnable done) {
        transition(WorldMapArt.NODE_OPENING, WorldMapArt.DANGER_OPENING, done);
    }

    public void playClearing(Runnable done) {
        transition(WorldMapArt.NODE_OPEN, WorldMapArt.DANGER_OPENING, done);
    }

    private void transition(String levelClip, String dangerClip, Runnable done) {
        PamActor target = dome != null ? dome : rig;
        String wanted = dome == null && node.getKind() == MapNodeKind.SPECIAL
                ? dangerClip : levelClip;
        String safe = WorldMapArt.clipOr(art, target == dome
                ? WorldMapArt.LEVEL_NODE : rigPath, wanted, null);
        if (safe == null) {
            done.run();
            return;
        }
        target.getColor().a = 1f;
        target.play(safe, false, done);
    }

    private Image skullMark(Assets assets, ChapterType chapter) {
        com.badlogic.gdx.graphics.g2d.TextureRegion art = assets == null ? null
                : assets.region(WorldMapArt.bossSkull(chapter, state != State.LOCKED));
        if (art == null) {
            return null;
        }
        Image mark = new Image(
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(art));
        mark.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        addActor(mark);
        return mark;
    }

    private void addNumber(UiKit ui) {
        String text = String.valueOf(node.getLevelNumber());
        if (!node.getLabel().isEmpty()) {
            text = text + " - " + node.getLabel();
        }
        number = new Label(text, ui.skin(), "titleOnDark");
        number.setAlignment(Align.center);
        number.setColor(state == State.LOCKED ? Theme.TEXT_DISABLED : Theme.TEXT_ON_DARK);
        addActor(number);
    }

    private String path(ChapterType chapter) {
        switch (node.getKind()) {
            case SPECIAL:
                return WorldMapArt.dangerNode(chapter);
            case ZOMBOSS:
                return WorldMapArt.zombossNode(chapter);
            default:
                return WorldMapArt.LEVEL_NODE;
        }
    }

    private String stableClip() {
        switch (node.getKind()) {
            case SPECIAL:
                return WorldMapArt.DANGER_LOCKED;
            case ZOMBOSS:
                return WorldMapArt.ZOMBOSS_OPEN;
            default:
                return WorldMapArt.NODE_CLEARED;
        }
    }

    private String clip() {
        if (node.getKind() == MapNodeKind.ZOMBOSS) {
            return state == State.CLEARED
                    ? WorldMapArt.ZOMBOSS_CLEARED : WorldMapArt.ZOMBOSS_OPEN;
        }
        if (node.getKind() == MapNodeKind.SPECIAL) {
            return state == State.LOCKED
                    ? WorldMapArt.DANGER_LOCKED : WorldMapArt.DANGER_OPEN;
        }
        return levelClip();
    }

    private String levelClip() {
        switch (state) {
            case CLEARED:
                return WorldMapArt.NODE_CLEARED;
            case CURRENT:
                return WorldMapArt.NODE_OPEN;
            default:
                return WorldMapArt.NODE_LOCKED;
        }
    }

    @Override
    public void layout() {
        float width = getWidth();
        float height = getHeight();
        view.gui.layout.UiLayout.placeAt(this, rig, 0f, 0f, width, height);
        float top = height * NUMBER_DROP;
        if (dome != null) {
            float size = width * BOSS_NODE;
            float domeX = (width - size) / 2f;
            float domeY = height * BOSS_SINK;
            view.gui.layout.UiLayout.placeAt(this, dome, 
                    domeX, domeY, size, size);
            if (skull != null) {
                float mark = size * SKULL_SHARE;
                float tall = mark * skull.getPrefHeight()
                        / Math.max(1f, skull.getPrefWidth());
                view.gui.layout.UiLayout.placeAt(this, skull, 
                        (width - mark) / 2f, domeY + size * SKULL_LIFT, mark, tall);
            }
            if (hologram != null) {
                float lift = size * HOLOGRAM_SCALE;
                view.gui.layout.UiLayout.placeAt(this, hologram, 
                        (width - lift) / 2f, domeY + size * HOLOGRAM_LIFT, lift, lift);
            }
            top = domeY - NUMBER_GAP;
        }
        place(number, width, Math.min(top, height - NUMBER_CAP));
    }

    private void place(Actor actor, float width, float y) {
        if (actor == null) {
            return;
        }
        view.gui.layout.UiLayout.placeAt(this, actor, 0f, y, width,
                actor instanceof Label
                        ? ((Label) actor).getPrefHeight() : actor.getHeight());
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && getTouchable() == com.badlogic.gdx.scenes.scene2d.Touchable.disabled) {
            return null;
        }
        if (node.getKind() == MapNodeKind.ZOMBOSS) {
            return x >= 0f && x < getWidth() && y >= 0f && y < getHeight() ? this : null;
        }
        Actor target = dome == null ? rig : dome;
        float left = target.getX() - HIT_PAD;
        float bottom = target.getY() - HIT_PAD;
        float right = target.getX() + target.getWidth() + HIT_PAD;
        float top = target.getY() + target.getHeight() + HIT_PAD;
        return x >= left && x < right && y >= bottom && y < top ? this : null;
    }

    @Override
    public float getPrefWidth() {
        return getWidth();
    }

    @Override
    public float getPrefHeight() {
        return getHeight();
    }
}

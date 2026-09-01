package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import model.entities.plants.PlantRecord;
import view.gui.Assets;
import view.gui.ChapterArt;
import view.gui.Theme;
import view.gui.UiKit;

public final class PlantStage extends Table {

    public static final int RADIUS = 12;
    public static final int BORDER = 3;

    private static final float COVERAGE = 1.72f;
    private static final float SHADOW_OFFSET = 5f;
    private static final float DROP = 0.30f;
    private static final int TURF_CROP_W = 420;
    private static final int TURF_CROP_H = 300;
    private static final float DEFAULT_CANVAS = 390f;
    private static final Color DIMMED = new Color(0.55f, 0.55f, 0.58f, 1f);

    private final UiKit ui;
    private final Assets assets;
    private final Table overlay = new Table();

    private PamActor plant;
    private PamActor shadow;

    public PlantStage(UiKit ui, Assets assets) {
        this.ui = ui;
        this.assets = assets;
        setBackground(ui.primitives().rounded(RADIUS, Theme.PANEL_SUNKEN,
                Theme.OUTLINE_SOFT, BORDER));
        pad(BORDER + 1f);
    }

    public Table overlay() {
        return overlay;
    }

    public boolean isReady() {
        return plant != null;
    }

    public void setDimmed(boolean value) {
        setColor(value ? DIMMED : Color.WHITE);
    }

    public static PamActor anchored(view.gui.Assets assets, String path, String clip,
            float canvasWidth, float canvasHeight) {
        PamActor actor = new PamActor(assets, path, clip).setFit(true);
        float widest = Math.max(canvasWidth, canvasHeight);
        float half = (widest <= 0f ? DEFAULT_CANVAS : widest) / 2f;
        actor.setExtent(-half, -half + half * DROP, half * 2f, half * 2f);
        return actor;
    }

    public static String clipOf(PlantRecord record, String wanted) {
        if (record == null || record.getAnimations() == null) {
            return wanted;
        }
        java.util.Set<String> names = record.getAnimations().getClips().keySet();
        if (names.isEmpty()) {
            return wanted;
        }
        if (wanted != null && names.contains(wanted)) {
            return wanted;
        }
        for (String name : names) {
            if (name.startsWith("idle")) {
                return name;
            }
        }
        return names.contains("loop") ? "loop" : names.iterator().next();
    }

    public void show(PlantRecord record, String wanted) {
        String clip = clipOf(record, wanted);
        clearChildren();
        overlay.clearChildren();
        plant = null;
        shadow = null;

        Stack stack = new Stack();
        Table turfLayer = new Table();
        turfLayer.setBackground(turf(record));
        stack.add(turfLayer);

        if (record != null && record.getAnimations().hasPlant() && assets != null) {
            PamActor shade = actor(record, clip, true);
            if (shade.isReady()) {
                shadow = shade;
                Table holder = new Table();
                holder.add(shade).grow().padLeft(SHADOW_OFFSET).padTop(SHADOW_OFFSET);
                stack.add(holder);
            }
            PamActor real = actor(record, clip, false);
            if (real.isReady()) {
                plant = real;
                Table holder = new Table();
                holder.add(real).grow();
                stack.add(holder);
            }
        }
        stack.add(overlay);
        add(stack).grow();
    }

    public void play(String clip) {
        if (plant != null) {
            plant.play(clip, true, null);
        }
        if (shadow != null) {
            shadow.play(clip, true, null);
        }
    }

    private PamActor actor(PlantRecord record, String clip, boolean asShadow) {
        PamActor actor = new PamActor(assets, record.getAnimations().getPlant(), clip)
                .setFit(true)
                .setCoverage(COVERAGE)
                .setClipped(true);
        float half = canvas(record) / 2f;
        actor.setExtent(-half, -half + half * DROP, half * 2f, half * 2f);
        if (asShadow) {
            actor.setColor(0f, 0f, 0f, 0.32f);
        }
        return actor;
    }

    private static float canvas(PlantRecord record) {
        int widest = Math.max(record.getAnimations().getCanvasWidth(),
                record.getAnimations().getCanvasHeight());
        return widest <= 0 ? DEFAULT_CANVAS : widest;
    }

    private Drawable turf(PlantRecord record) {
        TextureRegion base = assets == null ? null : assets.region(ChapterArt.turf(record));
        if (base == null) {
            return ui.primitives().flat(Theme.PANEL_SUNKEN);
        }
        int w = Math.min(base.getRegionWidth(), TURF_CROP_W);
        int h = Math.min(base.getRegionHeight(), TURF_CROP_H);
        int x = base.getRegionX() + (base.getRegionWidth() - w) / 2;
        int y = base.getRegionY() + (base.getRegionHeight() - h) / 2;
        return new TextureRegionDrawable(new TextureRegion(base.getTexture(), x, y, w, h));
    }
}

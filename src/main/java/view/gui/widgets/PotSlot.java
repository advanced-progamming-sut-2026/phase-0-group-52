package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.greenhouse.Pot;
import view.gui.Assets;

public final class PotSlot extends Group {

    public static final float SLOT_WIDTH = 152f;
    public static final float SLOT_HEIGHT = 156f;

    public static final String SLOT =
            "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161";
    public static final String WATER =
            "IMAGE_ZEN_GARDEN_ZEN_POT_WATER_ZEN_POT_WATER_160X97";

    private static final float SOIL_LINE = 0.34f;
    private static final float PLANT_SPAN = 1.15f;
    private static final float TRAY_SHARE = 0.72f;
    private static final float TRAY_FOOT = 0.12f;
    private static final float TRAY_ASPECT = 118f / 103f;
    private static final float POOL_ASPECT = 103f / 62f;
    private static final float SHIMMER_RATE = 6f;
    private static final float POUR_SPAN = 2.2f;
    private static final float SEEDLING_SHARE = 0.35f;

    public static String keyFor(String base, model.greenhouse.Pot pot) {
        if (pot == null) {
            return base;
        }
        return base + "|" + (pot.isMarigold() || pot.getPlantType() == null
                ? "MARIGOLD" : pot.getPlantType().name());
    }

    public static view.gui.EntityTuning.Tune seedlingTune(model.greenhouse.Pot pot) {
        return tuneFor(SEEDLING_KEY, pot);
    }

    public static view.gui.EntityTuning.Tune fullTune(model.greenhouse.Pot pot) {
        return tuneFor(PLANT_KEY, pot);
    }

    private static view.gui.EntityTuning.Tune tuneFor(String base,
            model.greenhouse.Pot pot) {
        String own = keyFor(base, pot);
        if (!view.gui.EntityTuning.has(own)) {
            view.gui.EntityTuning.Tune made = view.gui.EntityTuning.edit(own);
            made.copyFrom(view.gui.EntityTuning.of(base));
            if (SEEDLING_KEY.equals(base)) {
                made.scale = SEEDLING_SHARE;
            }
        }
        return view.gui.EntityTuning.of(own);
    }

    private static float mix(float from, float to, float at) {
        return from + (to - from) * Math.max(0f, Math.min(1f, at));
    }


    public static final String PLANT_KEY = "pot|plant";
    public static final String SEEDLING_KEY = "pot|seedling";
    public static final String WATER_KEY = "pot|water";
    public static final String POUR_KEY = "pot|pour";

    private final Assets assets;
    private final Pot pot;

    private final Image tray;
    private final Image pool;
    private final Label caption;

    private PamActor bloom;
    private String showing;
    private boolean shimmer;
    private String playing;
    private float pulse;

    public PotSlot(Assets assets, Pot pot,
            com.badlogic.gdx.scenes.scene2d.ui.Skin skin) {
        this.assets = assets;
        this.pot = pot;
        this.tray = art(SLOT);
        this.pool = art(WATER);
        this.caption = new Label("", skin, "small");
        caption.setAlignment(Align.center);
        addActor(tray);
        addActor(pool);
        addActor(caption);
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
    }

    public Pot pot() {
        return pot;
    }

    private Image art(String id) {
        TextureRegion region = assets == null ? null : assets.region(id);
        Image made = region == null ? new Image() : new Image(region);
        made.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        made.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        return made;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        sync();
    }

    private void sync() {
        tray.setVisible(pot.isUnlocked());
        if (shimmer) {
            pulse += com.badlogic.gdx.Gdx.graphics.getDeltaTime() * SHIMMER_RATE;
            float glow = 0.82f + 0.18f * (float) Math.sin(pulse);
            tray.setColor(1f, 1f, glow, 1f);
        } else {
            pulse = 0f;
            tray.setColor(1f, 1f, 1f, 1f);
        }
        pool.setVisible(pot.isUnlocked() && pot.isOccupied() && pot.holdsWaterPlant());
        caption.setText(label());
        String wanted = rigWanted();
        if (wanted != null && !wanted.equals(showing)) {
            showing = wanted;
            if (bloom != null) {
                bloom.remove();
            }
            bloom = new PamActor(assets, wanted, PlantStage.clipOf(record(), "idle"))
                    .setFit(true);
            if (!bloom.isReady()) {
                bloom = null;
            } else {
                addActor(bloom);
            }
        }
        if (wanted == null && bloom != null) {
            bloom.remove();
            bloom = null;
            showing = null;
        }
        layoutParts();
    }

    private PlantRecord record() {
        return pot.getPlantType() == null ? null : PlantData.record(pot.getPlantType());
    }

    private String rigWanted() {
        if (!pot.isUnlocked() || !pot.isOccupied()) {
            return null;
        }
        if (pot.isMarigold()) {
            return "768/INITIAL/PLANT/MARIGOLD/MARIGOLD.PAM";
        }
        PlantRecord record = record();
        return record == null || !record.getAnimations().hasPlant()
                ? null : record.getAnimations().getPlant();
    }

    private String label() {
        return pot.caption();
    }

    public void setWatering(boolean value) {
        if (bloom == null || record() == null) {
            return;
        }
        String wanted = value ? "water" : "idle";
        String clip = PlantStage.clipOf(record(), wanted);
        if (!clip.equals(playing)) {
            playing = clip;
            bloom.play(clip, true, null);
        }
    }

    public void splash(String rig) {
        final PamActor pour = new PamActor(assets, rig, "animation").setFit(true);
        if (!pour.isReady()) {
            return;
        }
        view.gui.EntityTuning.Tune tune = view.gui.EntityTuning.of(POUR_KEY);
        float size = getWidth() * POUR_SPAN * tune.scale;
        pour.setBounds((getWidth() - size) / 2f + tune.dx,
                tray.getY() + tray.getHeight() * SOIL_LINE + tune.dy, size, size);
        addActor(pour);
        pour.play("animation", false, new Runnable() {
            @Override
            public void run() {
                pour.remove();
            }
        });
    }

    public void setShimmering(boolean value) {
        shimmer = value;
    }

    public boolean isShimmering() {
        return shimmer;
    }

    private void layoutParts() {
        float w = getWidth();
        float h = getHeight();
        float trayH = Math.min(h * TRAY_SHARE, w / TRAY_ASPECT);
        float trayW = trayH * TRAY_ASPECT;
        tray.setBounds((w - trayW) / 2f, h * TRAY_FOOT, trayW, trayH);
        float poolW = trayW * 0.86f;
        float poolH = poolW / POOL_ASPECT;
        view.gui.EntityTuning.Tune wet = view.gui.EntityTuning.of(WATER_KEY);
        poolW *= wet.scale;
        poolH = poolW / POOL_ASPECT;
        pool.setBounds((w - poolW) / 2f + wet.dx,
                h * TRAY_FOOT + trayH * 0.22f + wet.dy, poolW, poolH);
        caption.setBounds(0f, 0f, w, h * 0.13f);
        if (bloom != null) {
            view.gui.EntityTuning.Tune seed = tuneFor(SEEDLING_KEY, pot);
            view.gui.EntityTuning.Tune full = tuneFor(PLANT_KEY, pot);
            float grown = (float) pot.growth();
            float scale = mix(seed.scale, full.scale, grown);
            float span = tray.getWidth() * PLANT_SPAN * scale;
            float soil = tray.getY() + tray.getHeight() * SOIL_LINE;
            bloom.setBounds(tray.getX() + (tray.getWidth() - span) / 2f
                            + mix(seed.dx, full.dx, grown),
                    soil + mix(seed.dy, full.dy, grown), span, span);
        }
    }

}

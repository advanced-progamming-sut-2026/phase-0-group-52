package tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import model.ChapterType;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;
import model.entities.zombies.Zombies;
import view.gui.Assets;
import view.gui.EntityTuning;
import view.gui.LawnGeometry;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.HeadSwapActor;
import view.gui.widgets.LawnView;
import view.gui.widgets.PamActor;
import view.gui.widgets.PlantStage;

public final class EntityTuner extends ApplicationAdapter {

    private static final float STEP = 1f;
    private static final float COARSE = 10f;
    private static final float SCALE_STEP = 0.02f;
    private static final float SPEED_STEP = 0.05f;
    private static final float REPEAT_DELAY = 0.32f;
    private static final float REPEAT_RATE = 0.035f;
    private static final float BOOST = 10f;
    private static final float MOWER_SCALE = 2.8f;
    private static final float TOMB_CANVAS = 390f;
    private static final float ICE_SPAN = 1.6f;
    private static final float POT_X = 420f;
    private static final float POT_Y = 180f;
    private static final float POUR_LOOP = 1.6f;

    private Assets assets;
    private UiKit ui;
    private Stage stage;
    private LawnView lawn;
    private Table hud;
    private Label readout;

    private ChapterType chapter = ChapterType.ANCIENT_EGYPT;
    private boolean zombieMode;
    private int plantIndex;
    private int zombieIndex;
    private int row = 2;
    private int column = 4;
    private Actor performer;
    private java.util.List<String> clips = new java.util.ArrayList<String>();
    private int clipIndex;
    private boolean gridMode;
    private boolean mowerMode;
    private boolean tombMode;
    private boolean shotMode;
    private boolean potMode;
    private boolean iceMode;
    private int icePiece;
    private int potPiece;
    private view.gui.widgets.PotSlot potSlot;
    private float pourClock;
    private int variantIndex;
    private int portIndex;
    private java.util.List<model.entities.plants.Muzzle> muzzles =
            new java.util.ArrayList<model.entities.plants.Muzzle>();
    private final java.util.List<Actor> ports = new java.util.ArrayList<Actor>();
    private Actor companion;
    private boolean running;
    private float runColumn = (float) model.entities.Lawnmower.START_COLUMN;
    private final java.util.Map<Integer, Float> held = new java.util.HashMap<Integer, Float>();

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("PvZ2 entity tuner");
        config.setWindowedMode(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new EntityTuner(), config);
    }

    @Override
    public void create() {
        assets = new Assets();
        ui = new UiKit(assets);
        stage = new Stage(new FitViewport(Theme.WORLD_WIDTH, Theme.WORLD_HEIGHT), assets.batch());
        Gdx.input.setInputProcessor(stage);
        stage.addListener(new GridDrag());
        readout = new Label("", ui.skin(), "onDark");
        rebuild();
    }

    private void rebuild() {
        EntityTuning.applyGrid(chapter.name());
        stage.clear();
        lawn = new LawnView(assets, chapter);
        lawn.setCamera(0f);
        stage.addActor(lawn);
        companion = shotMode ? companionActor() : null;
        if (companion != null) {
            stage.addActor(companion);
        }
        performer = iceMode ? iceActor() : potMode ? potActor()
                : shotMode ? shotActors() : tombMode ? tombActor()
                : mowerMode ? mowerActor() : zombieMode ? zombieActor() : plantActor();
        if (performer != null) {
            stage.addActor(performer);
        }
        stage.addActor(new GridOverlay(ui));
        hud = new Table();
        hud.setFillParent(true);
        hud.top().left();
        hud.add(readout).left().pad(10f);
        stage.addActor(hud);
        place();
    }

    private final class GridDrag extends com.badlogic.gdx.scenes.scene2d.InputListener {
        private float lastX;
        private float lastY;

        @Override
        public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                float x, float y, int pointer, int button) {
            lastX = x;
            lastY = y;
            return gridMode;
        }

        @Override
        public void touchDragged(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                float x, float y, int pointer) {
            if (!gridMode) {
                return;
            }
            EntityTuning.Tune tune = EntityTuning.edit(EntityTuning.gridKey(chapter.name()));
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                tune.dw += x - lastX;
                tune.dh += y - lastY;
            } else {
                tune.dx += x - lastX;
                tune.dy += y - lastY;
            }
            lastX = x;
            lastY = y;
            EntityTuning.applyGrid(chapter.name());
            EntityTuning.touch();
            place();
        }
    }

    private static final class GridOverlay extends Actor {
        private final UiKit kit;

        GridOverlay(UiKit kit) {
            this.kit = kit;
            setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }

        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            com.badlogic.gdx.scenes.scene2d.utils.Drawable edge =
                    kit.primitives().flat(Theme.alpha(
                            com.badlogic.gdx.graphics.Color.CYAN, 0.75f));
            com.badlogic.gdx.scenes.scene2d.utils.Drawable soft =
                    kit.primitives().flat(Theme.alpha(
                            com.badlogic.gdx.graphics.Color.WHITE, 0.35f));
            batch.setColor(1f, 1f, 1f, parentAlpha);
            float w = LawnGeometry.cellWidth();
            float h = LawnGeometry.cellHeight();
            for (int c = 0; c < LawnGeometry.COLUMNS; c++) {
                for (int r = 0; r < LawnGeometry.ROWS; r++) {
                    float x = LawnGeometry.areaX() + c * w;
                    float y = LawnGeometry.areaY() + r * h;
                    soft.draw(batch, x, y, w, 1f);
                    soft.draw(batch, x, y, 1f, h);
                }
            }
            float ax = LawnGeometry.areaX();
            float ay = LawnGeometry.areaY();
            float aw = LawnGeometry.areaWidth();
            float ah = LawnGeometry.areaHeight();
            edge.draw(batch, ax, ay, aw, 2f);
            edge.draw(batch, ax, ay + ah - 2f, aw, 2f);
            edge.draw(batch, ax, ay, 2f, ah);
            edge.draw(batch, ax + aw - 2f, ay, 2f, ah);
            batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        }
    }

    private model.entities.Projectile.Kind shotKind() {
        return model.entities.Projectile.kindOf(plants()[plantIndex]);
    }

    private Actor shotActors() {
        clips = new java.util.ArrayList<String>();
        ports.clear();
        muzzles = muzzlesOf(plants()[plantIndex], shotVariant());
        for (int i = 0; i < muzzles.size(); i++) {
            PamActor actor = view.gui.ShotArt.actor(assets, plants()[plantIndex],
                    shotKind(), shotVariant(), muzzles.get(i).getName());
            if (actor == null) {
                continue;
            }
            if (i != Math.floorMod(portIndex, muzzles.size())) {
                actor.setTint(1f, 1f, 1f, 0.35f);
            }
            ports.add(actor);
            stage.addActor(actor);
        }
        int chosen = Math.floorMod(portIndex, Math.max(1, muzzles.size()));
        return ports.isEmpty() ? null : ports.get(Math.min(chosen, ports.size() - 1));
    }

    private static java.util.List<model.entities.plants.Muzzle> muzzlesOf(Plants type,
            String state) {
        model.entities.plants.Plant made =
                model.entities.plants.PlantFactory.create(type, new model.Vec2(0, 0));
        if (made instanceof model.entities.plants.Shooter) {
            return ((model.entities.plants.Shooter) made).portsFor(state);
        }
        return java.util.Collections.singletonList(
                model.entities.plants.Muzzle.forward());
    }

    private double frameOfSelected() {
        return frameOf(portName());
    }

    private double frameOf(String port) {
        for (model.entities.plants.Muzzle muzzle : muzzles) {
            if (muzzle.getName().equals(port)) {
                return muzzle.frameIn(plants()[plantIndex], shotVariant());
            }
        }
        return 0d;
    }

    private void nudgeFrame(double step) {
        String port = portName();
        double next = Math.max(0d, Math.min(1d, frameOf(port) + step));
        model.entities.plants.MuzzleTiming.set(plants()[plantIndex], port,
                shotVariant(), next);
    }

    private String portName() {
        return muzzles.isEmpty() ? model.entities.plants.Muzzle.MAIN
                : muzzles.get(Math.floorMod(portIndex, muzzles.size())).getName();
    }

    private java.util.List<String> shotVariants() {
        java.util.List<String> all = new java.util.ArrayList<String>();
        Plants plant = plants()[plantIndex];
        if (plant == Plants.PEA_POD) {
            for (int grown = 1; grown <= model.entities.plants.types.PeaPod.MAX_HEADS;
                    grown++) {
                all.add(model.entities.plants.types.PeaPod.HEADS + grown);
            }
            all.add(model.entities.Projectile.FED);
            return all;
        }
        if (plant == Plants.BOWLING_BULB) {
            all.add(model.entities.Projectile.BULB + 1);
            all.add(model.entities.Projectile.FED + 1);
            return all;
        }
        all.add("");
        all.add(model.entities.Projectile.FED);
        return all;
    }

    private String shotVariant() {
        java.util.List<String> all = shotVariants();
        return all.get(Math.floorMod(variantIndex, all.size()));
    }

    private Actor companionActor() {
        PlantRecord record = PlantData.record(plants()[plantIndex]);
        if (record == null || !record.getAnimations().hasPlant()) {
            return null;
        }
        PamActor actor = PlantStage.anchored(assets, record.getAnimations().getPlant(),
                companionClip(record), record.getAnimations().getCanvasWidth(),
                record.getAnimations().getCanvasHeight());
        if (shotMode) {
            actor.poseAt((float) frameOfSelected());
        } else {
            actor.setRate(EntityTuning.of(EntityTuning.plantKey(
                    plants()[plantIndex].name())).speed);
        }
        return actor.isReady() ? actor : null;
    }

    private String companionClip(PlantRecord record) {
        java.util.Set<String> have = record.getAnimations().getClips().keySet();
        String variant = shotVariant();
        if (variant.startsWith(model.entities.Projectile.FED)) {
            String tail = variant.substring(model.entities.Projectile.FED.length());
            for (String wanted : new String[] {"plantfood" + tail, "plantfood",
                "plantfood_loop", "plantfood_on", "pf"}) {
                if (have.contains(wanted)) {
                    return wanted;
                }
            }
        } else if (variant.startsWith(model.entities.Projectile.BULB)) {
            String index = variant.substring(model.entities.Projectile.BULB.length());
            for (String wanted : new String[] {"special" + index, "special"}) {
                if (have.contains(wanted)) {
                    return wanted;
                }
            }
        } else if (variant.startsWith(model.entities.plants.types.PeaPod.HEADS)) {
            String numbered = "attack "
                    + variant.substring(model.entities.plants.types.PeaPod.HEADS.length());
            if (have.contains(numbered)) {
                return numbered;
            }
        }
        for (String wanted : new String[] {"attack", "special_stage1", "special"}) {
            if (have.contains(wanted)) {
                return wanted;
            }
        }
        return PlantStage.clipOf(record, "idle");
    }

    private static final String[] POT_KEYS = {
        view.gui.widgets.PotSlot.SEEDLING_KEY,
        view.gui.widgets.PotSlot.PLANT_KEY,
        view.gui.widgets.PotSlot.WATER_KEY,
        view.gui.widgets.PotSlot.POUR_KEY,
    };

    private static java.util.List<Plants> gardenPlants() {
        java.util.List<Plants> able = new java.util.ArrayList<Plants>();
        for (Plants type : Plants.values()) {
            model.entities.plants.PlantRecord record =
                    model.entities.plants.PlantData.record(type);
            if (record != null && record.isBoostable()) {
                able.add(type);
            }
        }
        return able;
    }

    private Plants potPlant() {
        java.util.List<Plants> able = gardenPlants();
        int at = Math.floorMod(plantIndex, able.size() + 1);
        return at == able.size() ? null : able.get(at);
    }

    private static final String[] ICE_KEYS = {
        view.gui.FrostArt.PLANT_BLOCK_KEY,
        view.gui.FrostArt.ZOMBIE_BLOCK_KEY,
        view.gui.FrostArt.TILE_KEY,
    };

    private String iceKey() {
        return ICE_KEYS[Math.floorMod(icePiece, ICE_KEYS.length)];
    }

    private Actor iceActor() {
        clips = new java.util.ArrayList<String>();
        String key = iceKey();
        if (view.gui.FrostArt.TILE_KEY.equals(key)) {
            com.badlogic.gdx.graphics.g2d.TextureRegion art =
                    assets.region(view.gui.FrostArt.TILE_ICE);
            return art == null ? null : new com.badlogic.gdx.scenes.scene2d.ui.Image(art);
        }
        boolean plant = view.gui.FrostArt.PLANT_BLOCK_KEY.equals(key);
        return view.gui.FrostArt.rig(assets,
                plant ? view.gui.FrostArt.ICE_PLANT : view.gui.FrostArt.ICE_ZOMBIE,
                "freeze_idle", "idle", "animation");
    }

    private String potBase() {
        return POT_KEYS[Math.floorMod(potPiece, POT_KEYS.length)];
    }

    private String potKey() {
        String base = potBase();
        if (!base.equals(view.gui.widgets.PotSlot.SEEDLING_KEY)
                && !base.equals(view.gui.widgets.PotSlot.PLANT_KEY)) {
            return base;
        }
        Plants held = potPlant();
        String own = base + "|" + (held == null ? "MARIGOLD" : held.name());
        if (!EntityTuning.has(own)) {
            EntityTuning.edit(own).copyFrom(EntityTuning.of(base));
        }
        return own;
    }

    private Actor potActor() {
        clips = new java.util.ArrayList<String>();
        model.greenhouse.Pot pot = new model.greenhouse.Pot(1, 1, true);
        Plants held = potPlant();
        if (held == null) {
            pot.plantMarigold();
        } else {
            pot.plantSpecial(held);
        }
        boolean seedling = potBase().equals(view.gui.widgets.PotSlot.SEEDLING_KEY);
        long now = System.currentTimeMillis();
        pot.setTimestamps(seedling ? now : now - model.greenhouse.Pot.GROW_MILLIS,
                seedling ? now + model.greenhouse.Pot.GROW_MILLIS : now - 1L);
        potSlot = new view.gui.widgets.PotSlot(assets, pot, ui.skin());
        return potSlot;
    }

    private void repour() {
        if (potSlot != null && potBase().equals(view.gui.widgets.PotSlot.POUR_KEY)) {
            potSlot.splash(view.gui.screens.GreenhouseScreen.POUR_RIG);
            pourClock = POUR_LOOP;
        }
    }

    private String key() {
        if (iceMode) {
            return iceKey();
        }
        if (potMode) {
            return potKey();
        }
        if (shotMode) {
            return EntityTuning.shotKey(plants()[plantIndex], portName(), shotVariant());
        }
        if (tombMode) {
            return "tomb|" + chapter.name();
        }
        if (mowerMode) {
            return "mower|" + view.gui.ChapterArt.world(chapter);
        }
        return zombieMode
                ? EntityTuning.zombieKey(zombies()[zombieIndex].name())
                : EntityTuning.plantKey(plants()[plantIndex].name());
    }

    private static Plants[] plants() {
        return Plants.values();
    }

    private static Zombies[] zombies() {
        return Zombies.values();
    }

    private Actor tombActor() {
        String rig = view.gui.ChapterArt.gravestone(chapter,
                chapter == ChapterType.DARK_AGES ? tombBonus() : null);
        clips = new java.util.ArrayList<String>(java.util.Arrays.asList(
                "undamaged", "damage1", "damage2", "damage3", "damage4"));
        String clip = clips.get(Math.floorMod(clipIndex, clips.size()));
        PamActor actor = PlantStage.anchored(assets, rig,
                view.gui.FrostArt.clipOf(assets, rig, clip, "undamaged"),
                TOMB_CANVAS, TOMB_CANVAS);
        return actor != null && actor.isReady() ? actor : null;
    }

    private String tombBonus() {
        int pick = Math.floorMod(plantIndex, 3);
        return pick == 0 ? null : pick == 1 ? "sun" : "plant food";
    }

    private Actor mowerActor() {
        String world = view.gui.ChapterArt.world(chapter);
        String rig = "EGYPT".equals(world)
                ? "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM"
                : "768/FULL/MOWERS/MOWER_" + world + "/MOWER_" + world + ".PAM";
        clips = new java.util.ArrayList<String>(
                java.util.Arrays.asList("idle", "transition", "attack"));
        PamActor actor = PlantStage.anchored(assets, rig,
                clips.get(Math.floorMod(clipIndex, clips.size())), 390f, 390f);
        return actor.isReady() ? actor : null;
    }

    private Actor plantActor() {
        PlantRecord record = PlantData.record(plants()[plantIndex]);
        if (record == null || !record.getAnimations().hasPlant()) {
            return null;
        }
        clips = new java.util.ArrayList<String>(record.getAnimations().getClips().keySet());
        if (clips.isEmpty()) {
            clips.add(PlantStage.clipOf(record, "idle"));
        }
        String clip = clips.get(Math.floorMod(clipIndex, clips.size()));
        PamActor actor = PlantStage.anchored(assets, record.getAnimations().getPlant(), clip,
                record.getAnimations().getCanvasWidth(),
                record.getAnimations().getCanvasHeight());
        actor.setRate(EntityTuning.of(key()).speed);
        return actor.isReady() ? actor : null;
    }

    private Actor zombieActor() {
        ZombieRecord record = ZombieData.of(zombies()[zombieIndex]);
        if (record == null) {
            return null;
        }
        clips = record.getClips() == null || record.getClips().isEmpty()
                ? new java.util.ArrayList<String>(java.util.Arrays.asList("idle"))
                : new java.util.ArrayList<String>(record.getClips());
        String clip = clips.get(Math.floorMod(clipIndex, clips.size()));
        if (record.isComposite()) {
            HeadSwapActor swap = new HeadSwapActor(assets, record.getBodyPath(),
                    record.getHeadPath(), clip, record.getHideParts());
            return swap.isReady() ? swap : null;
        }
        if (!record.hasAnimation()) {
            return null;
        }
        PamActor actor = PlantStage.anchored(assets, record.getAnimationPath(), clip,
                record.getCanvasWidth(),
                record.getCanvasHeight());
        actor.setParts(worn(record));
        actor.setRate(EntityTuning.of(key()).speed);
        return actor.isReady() ? actor : null;
    }

    private static java.util.Map<String, Boolean> worn(ZombieRecord record) {
        if (record.getArmorParts() == null || record.getArmorParts().isEmpty()) {
            return null;
        }
        java.util.Map<String, Boolean> parts = new java.util.HashMap<String, Boolean>();
        for (String part : record.getArmorParts()) {
            parts.put(part, Boolean.TRUE);
        }
        return parts;
    }

    private boolean repeat(int key) {
        if (Gdx.input.isKeyJustPressed(key)) {
            held.put(key, -REPEAT_DELAY);
            return true;
        }
        if (!Gdx.input.isKeyPressed(key)) {
            held.remove(key);
            return false;
        }
        Float clock = held.get(key);
        if (clock == null) {
            return false;
        }
        float next = clock + Gdx.graphics.getDeltaTime();
        if (next >= 0f) {
            held.put(key, next - REPEAT_RATE);
            return true;
        }
        held.put(key, next);
        return false;
    }

    private void place() {
        if (performer == null) {
            return;
        }
        if (iceMode) {
            EntityTuning.Tune tune = EntityTuning.of(key());
            float span = LawnGeometry.cellWidth() * ICE_SPAN * tune.scale;
            performer.setBounds(LawnGeometry.columnLeft(column)
                            + (LawnGeometry.cellWidth() - span) / 2f + tune.dx,
                    LawnGeometry.rowFeet(row) + tune.dy, span, span);
            readout.setText(caption(tune));
            return;
        }
        if (potMode) {
            performer.setBounds(POT_X, POT_Y,
                    view.gui.widgets.PotSlot.SLOT_WIDTH,
                    view.gui.widgets.PotSlot.SLOT_HEIGHT);
            readout.setText(caption(EntityTuning.of(key())));
            repour();
            return;
        }
        if (shotMode) {
            placeShot();
            return;
        }
        if (mowerMode) {
            placeMower();
            return;
        }
        if (tombMode) {
            EntityTuning.place(performer, EntityTuning.of(key()), column, row, false);
            readout.setText(caption(EntityTuning.of(key())));
            return;
        }
        EntityTuning.place(performer, EntityTuning.of(key()), column, row, zombieMode);
        readout.setText(caption(EntityTuning.of(gridMode
                ? EntityTuning.gridKey(chapter.name()) : key())));
    }

    private void placeShot() {
        if (companion != null) {
            EntityTuning.place(companion, EntityTuning.of(
                    EntityTuning.plantKey(plants()[plantIndex].name())), column, row, false);
        }
        for (int i = 0; i < ports.size() && i < muzzles.size(); i++) {
            model.entities.plants.Muzzle muzzle = muzzles.get(i);
            int lane = row;
            float at = column + muzzle.getDirection() * 0.5f;
            EntityTuning.placeShot(ports.get(i), inherit(muzzle.getName()), shotKind(),
                    at, lane);
        }
        readout.setText(caption(EntityTuning.of(key())));
    }

    private EntityTuning.Tune inherit(String port) {
        Plants plant = plants()[plantIndex];
        String own = EntityTuning.shotKey(plant, "", "");
        if (!EntityTuning.has(own)) {
            EntityTuning.edit(own).copyFrom(EntityTuning.of(EntityTuning.kindKey(plant)));
        }
        String byPort = EntityTuning.shotKey(plant, port, "");
        if (!EntityTuning.has(byPort)) {
            EntityTuning.edit(byPort).copyFrom(EntityTuning.of(own));
        }
        String variant = shotVariant();
        if (variant.isEmpty()) {
            return EntityTuning.of(byPort);
        }
        String full = EntityTuning.shotKey(plant, port, variant);
        if (!EntityTuning.has(full)) {
            EntityTuning.edit(full).copyFrom(EntityTuning.of(byPort));
        }
        return EntityTuning.of(full);
    }

    private void placeMower() {
        EntityTuning.Tune tune = EntityTuning.of(key());
        float size = LawnGeometry.cellHeight() * MOWER_SCALE * tune.scale;
        float at = running ? runColumn : (float) model.entities.Lawnmower.START_COLUMN;
        performer.setBounds(LawnGeometry.areaX() + (at + 0.5f) * LawnGeometry.cellWidth()
                        - size / 2f + tune.dx,
                LawnGeometry.rowFeet(row) + tune.dy, size, size);
        readout.setText(caption(tune));
    }

    private String caption(EntityTuning.Tune tune) {
        String name = zombieMode ? zombies()[zombieIndex].name() : plants()[plantIndex].name();
        return chapter.getDisplayName() + "   "
                + (iceMode ? "ICE" : potMode ? "POT" : gridMode ? "GRID" : shotMode ? "SHOT" : tombMode ? "TOMB"
                        : mowerMode ? "MOWER" : zombieMode ? "ZOMBIE" : "PLANT") + "  "
                + (potMode ? (potPlant() == null ? "Marigold" : potPlant().getName())
                        + "  editing " + key()
                : shotMode ? name + "  muzzle " + portName() + " of " + muzzles.size()
                        + (shotVariant().isEmpty() ? " (normal)" : " (" + shotVariant() + ")")
                        : mowerMode ? view.gui.ChapterArt.world(chapter) : name)
                + "\ndx " + tune.dx + "   dy " + tune.dy
                + "   scale " + tune.scale + "   speed " + tune.speed
                + "\n[Tab] plants/zombies  [ [ / ] ] entity  [W] world  [R/F] row  [A/D] column"
                + "\narrows move (shift x10)   [Q/E] scale   [Z/X] animation speed"
                + "\n[I] ice mode " + (iceMode ? "ON " + iceKey() : "off")
                + "  [P] piece"
                + "\n[G] pot mode " + (potMode ? "ON " + potKey() : "off")
                + "  [P] piece"
                + "\n[V] shot mode " + (shotMode ? "ON" : "off")
                + " - [ ] plant, [P] muzzle, [C] state: "
                + (shotVariant().isEmpty() ? "normal" : shotVariant())
                + "   [,/.] frame " + Math.round(frameOfSelected() * 100d) + "%"
                + "\n[M] GRID MODE " + (gridMode ? "ON" : "off")
                + " - drag to move, shift-drag to resize, Q/E width, G/T height"
                + "\n[C] animation: " + currentClip()
                + "\n[S] save   [0] reset";
    }

    private String currentClip() {
        return clips.isEmpty() ? "-" : clips.get(Math.floorMod(clipIndex, clips.size()));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (running) {
            runColumn += (float) model.entities.Lawnmower.SPEED
                    * Gdx.graphics.getDeltaTime() * model.Game.TICKS_PER_SECOND;
            if (runColumn > LawnGeometry.COLUMNS + 1) {
                running = false;
                runColumn = (float) model.entities.Lawnmower.START_COLUMN;
                if (performer instanceof PamActor) {
                    ((PamActor) performer).play("idle", true, null);
                }
            }
            place();
        }
        if (potMode && potSlot != null) {
            pourClock -= Gdx.graphics.getDeltaTime();
            if (pourClock <= 0f) {
                repour();
            }
        }
        input();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void input() {
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        float step = shift ? COARSE : STEP;
        EntityTuning.Tune tune = EntityTuning.edit(gridMode
                ? EntityTuning.gridKey(chapter.name()) : key());
        boolean moved = false;

        if (repeat(Input.Keys.LEFT)) {
            tune.dx -= step;
            moved = true;
        }
        if (repeat(Input.Keys.RIGHT)) {
            tune.dx += step;
            moved = true;
        }
        if (repeat(Input.Keys.DOWN)) {
            tune.dy -= step;
            moved = true;
        }
        if (repeat(Input.Keys.UP)) {
            tune.dy += step;
            moved = true;
        }
        moved |= sizing(tune, shift);
        if (moved) {
            if (gridMode) {
                EntityTuning.applyGrid(chapter.name());
            }
            EntityTuning.touch();
            place();
        }
        cycle();
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            EntityTuning.save();
            model.entities.plants.MuzzleTiming.save();
        }
    }

    private boolean sizing(EntityTuning.Tune tune, boolean shift) {
        float scaleStep = SCALE_STEP * (shift ? BOOST : 1f);
        float speedStep = SPEED_STEP * (shift ? BOOST : 1f);
        float sizeStep = COARSE * (shift ? BOOST : 1f);
        boolean moved = false;
        if (repeat(Input.Keys.Q)) {
            if (gridMode) {
                tune.dw -= sizeStep;
            } else {
                tune.scale = Math.max(0.1f, tune.scale - scaleStep);
            }
            moved = true;
        }
        if (repeat(Input.Keys.E)) {
            if (gridMode) {
                tune.dw += sizeStep;
            } else {
                tune.scale += scaleStep;
            }
            moved = true;
        }
        if (gridMode && repeat(Input.Keys.T)) {
            tune.dh += sizeStep;
            moved = true;
        }
        if (gridMode && repeat(Input.Keys.G)) {
            tune.dh -= sizeStep;
            moved = true;
        }
        if (repeat(Input.Keys.Z)) {
            tune.speed = Math.max(0.1f, tune.speed - speedStep);
            moved = true;
        }
        if (repeat(Input.Keys.X)) {
            tune.speed += speedStep;
            moved = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            tune.dx = 0f;
            tune.dy = 0f;
            tune.scale = 1f;
            tune.speed = 1f;
            moved = true;
        }
        return moved;
    }

    private void cycle() {
        boolean rebuild = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            zombieMode = !zombieMode;
            clipIndex = 0;
            rebuild = true;
        }
        int span = potMode ? gardenPlants().size() + 1
                : zombieMode ? zombies().length : plants().length;
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            step(1, span);
            rebuild = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            step(-1, span);
            rebuild = true;
        }
        if (mowerMode && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            running = true;
            runColumn = (float) model.entities.Lawnmower.START_COLUMN;
            if (performer instanceof PamActor) {
                ((PamActor) performer).play("attack", true, null);
            }
        }
        rebuild |= modeKeys();
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            gridMode = !gridMode;
            rebuild = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            if (shotMode) {
                variantIndex++;
            } else {
                clipIndex++;
            }
            rebuild = true;
        }
        if (shotMode && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            portIndex++;
            rebuild = true;
        }
        rebuild |= frameKeys();
        rebuild |= worldKey();
        cellKeys();
        if (rebuild) {
            rebuild();
        }
    }

    private boolean modeKeys() {
        boolean changed = false;
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            tombMode = !tombMode;
            clipIndex = 0;
            changed = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            mowerMode = !mowerMode;
            clipIndex = 0;
            changed = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            potMode = !potMode;
            changed = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            iceMode = !iceMode;
            changed = true;
        }
        if (iceMode && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            icePiece++;
            changed = true;
        }
        if (potMode && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            potPiece++;
            changed = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            shotMode = !shotMode;
            clipIndex = 0;
            variantIndex = 0;
            portIndex = 0;
            changed = true;
        }
        return changed;
    }

    private void cellKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            row = Math.floorMod(row - 1, LawnGeometry.ROWS);
            place();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            row = Math.floorMod(row + 1, LawnGeometry.ROWS);
            place();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            column = Math.floorMod(column - 1, LawnGeometry.COLUMNS);
            place();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            column = Math.floorMod(column + 1, LawnGeometry.COLUMNS);
            place();
        }
    }

    private void step(int delta, int span) {
        if (zombieMode) {
            zombieIndex = Math.floorMod(zombieIndex + delta, span);
        } else {
            plantIndex = Math.floorMod(plantIndex + delta, span);
        }
    }

    private boolean worldKey() {
        if (!Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            return false;
        }
        ChapterType[] all = ChapterType.values();
        chapter = all[(chapter.ordinal() + 1) % all.length];
        return true;
    }

    private boolean frameKeys() {
        if (!shotMode) {
            return false;
        }
        if (repeat(Input.Keys.COMMA)) {
            nudgeFrame(-frameStep());
            return true;
        }
        if (repeat(Input.Keys.PERIOD)) {
            nudgeFrame(frameStep());
            return true;
        }
        return false;
    }

    private static double frameStep() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? 0.1d : 0.02d;
    }

    @Override
    public void dispose() {
        EntityTuning.save();
        model.entities.plants.MuzzleTiming.save();
        if (stage != null) {
            stage.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}

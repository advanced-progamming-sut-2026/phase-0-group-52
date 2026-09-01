package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import controller.menu.LevelController;
import model.entities.Sun;
import model.entities.plants.Plant;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.LawnGeometry;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LawnField extends WidgetGroup {

    public interface Sink {
        void picked(int column, int row);

        void missed();
    }

    private static final float SUN_SIZE = 128f;
    private static final float SMOOTHING = 14f;
    private static final float ZOMBIE_BIAS = 0.5f;
    private static final float MOWER_CANVAS = 390f;
    private static final float TOMB_CANVAS = 390f;
    private static final float TOMB_SCALE = 2.2f;
    private static final float TORNADO_CANVAS = 320f;
    private static final float TORNADO_SCALE = 1.45f;
    private static final float FLASH_TIME = 0.16f;
    private static final float FLASH_STRENGTH = 0.55f;
    private static final String TOMB_PAM =
            "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
    private static final float MOWER_SCALE = 2.8f;
    private static final float SPAWN_GAP = 0.22f;
    private static final String MOWER_SPAWN =
            "768/INITIAL/EFFECTS/MOWER_SPAWN/MOWER_SPAWN.PAM";
    private static final String SUN_PAM = "768/INITIAL/EFFECTS/SUN/SUN.PAM";

    private final UiKit ui;
    private final Assets assets;
    private final LevelController controller;
    private final Map<Plant, Actor> growing = new HashMap<Plant, Actor>();
    private final Map<Sun, Actor> shining = new HashMap<Sun, Actor>();
    private final Map<model.entities.zombies.Zombie, Actor> horde =
            new HashMap<model.entities.zombies.Zombie, Actor>();
    private final Map<model.entities.zombies.Zombie, Float> smoothed =
            new HashMap<model.entities.zombies.Zombie, Float>();

    private Sink sink;
    private boolean showGrid;
    private Plants armed;
    private int hoverColumn = -1;
    private int hoverRow = -1;
    private Sun hovered;
    private java.util.List<PamActor> mowers;
    private final Map<model.entities.Tombstone, PamActor> stones =
            new HashMap<model.entities.Tombstone, PamActor>();
    private final Map<model.entities.zombies.Zombie, PamActor> tornadoes =
            new HashMap<model.entities.zombies.Zombie, PamActor>();
    private final Map<model.entities.zombies.Zombie, PamActor> tornadoFronts =
            new HashMap<model.entities.zombies.Zombie, PamActor>();
    private final Map<Actor, Float> hurt = new HashMap<Actor, Float>();
    private final Map<model.entities.zombies.Zombie, Double> zombieHp =
            new HashMap<model.entities.zombies.Zombie, Double>();
    private final Map<Plant, Double> plantHp = new HashMap<Plant, Double>();
    private final Map<model.entities.Tombstone, String> stoneClip =
            new HashMap<model.entities.Tombstone, String>();
    private final Map<model.entities.zombies.Zombie, String> clipShown =
            new HashMap<model.entities.zombies.Zombie, String>();
    private final Map<Integer, Actor> poofs = new HashMap<Integer, Actor>();
    private final java.util.Set<model.entities.Lawnmower> running =
            new java.util.HashSet<model.entities.Lawnmower>();
    private int spawned;
    private final Map<Sun, Float> sunFall = new HashMap<Sun, Float>();

    public LawnField(UiKit ui, Assets assets, LevelController controller) {
        this.ui = ui;
        this.assets = assets;
        this.controller = controller;
        setTouchable(Touchable.enabled);
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                int column = LawnGeometry.columnAt(x);
                int row = LawnGeometry.rowAt(y);
                if (sink == null) {
                    return;
                }
                if (column >= 0 && row >= 0) {
                    sink.picked(column, row);
                } else {
                    sink.missed();
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                hoverColumn = LawnGeometry.columnAt(x);
                hoverRow = LawnGeometry.rowAt(y);
                return false;
            }
        });
    }

    public void onPlant(Sink value) {
        sink = value;
    }

    public void setShowGrid(boolean value) {
        showGrid = value;
    }

    public void setArmed(Plants value) {
        armed = value;
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        syncArea();
        syncPlants();
        syncSun();
        syncHorde(delta);
        flashDamage(delta);
        syncTombstones();
        syncMowers();
        spawnNextMower(delta);
        placeMowers();
        sortDepth();
    }

    private void flashDamage(float delta) {
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            double hp = entry.getKey().getHp();
            Double was = zombieHp.get(entry.getKey());
            if (was != null && hp < was) {
                hurt.put(entry.getValue(), FLASH_TIME);
            }
            zombieHp.put(entry.getKey(), hp);
        }
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            double hp = entry.getKey().getHp();
            Double was = plantHp.get(entry.getKey());
            if (was != null && hp < was) {
                hurt.put(entry.getValue(), FLASH_TIME);
            }
            plantHp.put(entry.getKey(), hp);
        }
        java.util.Iterator<Map.Entry<Actor, Float>> fading = hurt.entrySet().iterator();
        while (fading.hasNext()) {
            Map.Entry<Actor, Float> entry = fading.next();
            float left = entry.getValue() - delta;
            Actor actor = entry.getKey();
            if (left <= 0f || actor.getStage() == null) {
                actor.setColor(Color.WHITE);
                fading.remove();
                continue;
            }
            entry.setValue(left);
            float mix = left / FLASH_TIME * FLASH_STRENGTH;
            actor.setColor(1f, 1f - mix, 1f - mix, 1f);
        }
    }

    private void sortDepth() {
        getChildren().sort(new java.util.Comparator<Actor>() {
            @Override
            public int compare(Actor a, Actor b) {
                float depth = depthOf(b) - depthOf(a);
                if (depth > 0f) {
                    return 1;
                }
                return depth < 0f ? -1 : 0;
            }
        });
    }

    private float depthOf(Actor actor) {
        for (Map.Entry<model.entities.Tombstone, PamActor> entry : stones.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.TOMBSTONE.depth(
                        LawnGeometry.rowFeet(entry.getKey().getRow()));
            }
        }
        for (Map.Entry<Sun, Actor> entry : shining.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.SUN.depth(actor.getY());
            }
        }
        if (mowers != null) {
            int index = mowers.indexOf(actor);
            if (index >= 0) {
                model.entities.Lawnmower mower = controller.game() == null
                        || controller.game().getField() == null ? null
                        : controller.game().getField().getLawnmower(index);
                view.gui.LawnLayer layer = mower != null && mower.isRunning()
                        ? view.gui.LawnLayer.GROUND : view.gui.LawnLayer.MOWER_PARKED;
                return layer.depth(LawnGeometry.rowFeet(index));
            }
        }
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.GROUND.depth(
                        LawnGeometry.rowFeet(entry.getKey().getRow()) - ZOMBIE_BIAS);
            }
        }
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.GROUND.depth(
                        LawnGeometry.rowFeet((int) entry.getKey().getPosition().y));
            }
        }
        return view.gui.LawnLayer.GROUND.depth(actor.getY());
    }

    private void syncTombstones() {
        if (assets == null || controller.game() == null) {
            return;
        }
        java.util.List<model.entities.Tombstone> live = controller.game().getTombstones();
        for (model.entities.Tombstone stone : live) {
            PamActor actor = stones.get(stone);
            if (actor == null) {
                actor = PlantStage.anchored(assets, TOMB_PAM, stone.clipName(),
                        TOMB_CANVAS, TOMB_CANVAS);
                if (!actor.isReady()) {
                    return;
                }
                actor.setSynced(true);
                stones.put(stone, actor);
                stoneClip.put(stone, stone.clipName());
                addActor(actor);
            }
            String clip = stone.clipName();
            if (!clip.equals(stoneClip.get(stone))) {
                stoneClip.put(stone, clip);
                actor.play(clip, true, null);
            }
            view.gui.EntityTuning.Tune tune =
                    view.gui.EntityTuning.of("tomb|" + chapterName());
            float size = LawnGeometry.cellHeight() * TOMB_SCALE * tune.scale;
            actor.setBounds(LawnGeometry.columnX(stone.getColumn()) - size / 2f + tune.dx,
                    LawnGeometry.rowFeet(stone.getRow()) + tune.dy, size, size);
        }
        java.util.Iterator<Map.Entry<model.entities.Tombstone, PamActor>> gone =
                stones.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<model.entities.Tombstone, PamActor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove();
                stoneClip.remove(entry.getKey());
                gone.remove();
            }
        }
    }

    private void syncMowers() {
        if (mowers != null || assets == null || controller.game() == null
                || controller.game().getField() == null) {
            return;
        }
        String rig = mowerRig();
        java.util.List<PamActor> built = new java.util.ArrayList<PamActor>();
        for (int row = 0; row < LawnGeometry.ROWS; row++) {
            PamActor actor = PlantStage.anchored(assets, rig, "idle",
                    MOWER_CANVAS, MOWER_CANVAS);
            if (!actor.isReady()) {
                util.Log.warn("gui", "Mower rig " + rig + " did not load");
                return;
            }
            actor.setSynced(true);
            actor.setVisible(false);
            built.add(actor);
        }
        mowers = built;
        for (PamActor actor : built) {
            addActor(actor);
        }
    }

    private String mowerKey() {
        return "mower|" + (controller.chapter() == null ? "EGYPT"
                : view.gui.ChapterArt.world(controller.chapter()));
    }

    private String mowerRig() {
        String world = controller.chapter() == null ? "EGYPT"
                : view.gui.ChapterArt.world(controller.chapter());
        if ("EGYPT".equals(world)) {
            return "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
        }
        return "768/FULL/MOWERS/MOWER_" + world + "/MOWER_" + world + ".PAM";
    }

    private void placeMowers() {
        if (mowers == null) {
            return;
        }
        for (int row = 0; row < mowers.size(); row++) {
            model.entities.Lawnmower mower = controller.game().getField().getLawnmower(row);
            PamActor actor = mowers.get(row);
            boolean alive = mower != null && mower.isIsactive();
            actor.setVisible(alive && spawned > row);
            if (!alive) {
                continue;
            }
            if (mower.isRunning() && !running.contains(mower)) {
                running.add(mower);
                actor.setSynced(false);
                actor.play("attack", true, null);
            }
            float column = mower.isRunning()
                    ? (float) mower.getColumn() : (float) model.entities.Lawnmower.START_COLUMN;
            view.gui.EntityTuning.Tune tune = view.gui.EntityTuning.of(mowerKey());
            float size = LawnGeometry.cellHeight() * MOWER_SCALE * tune.scale;
            float x = LawnGeometry.areaX() + (column + 0.5f) * LawnGeometry.cellWidth()
                    - size / 2f + tune.dx;
            float y = LawnGeometry.rowFeet(row) + tune.dy;
            actor.setBounds(x, y, size, size);
            Actor poof = poofs.get(row);
            if (poof != null) {
                poof.setBounds(x, y, size, size);
            }
        }
    }

    private void spawnNextMower(float delta) {
        if (mowers == null || spawned >= mowers.size() || controller.game() == null) {
            return;
        }
        int due = 1 + (int) (controller.game().getCurrentTick()
                / (SPAWN_GAP * model.Game.TICKS_PER_SECOND));
        if (spawned >= due) {
            return;
        }
        int row = spawned++;
        if (assets == null) {
            return;
        }
        PamActor poof = PlantStage.anchored(assets, MOWER_SPAWN, "animation",
                MOWER_CANVAS, MOWER_CANVAS);
        if (!poof.isReady()) {
            return;
        }
        poofs.put(row, poof);
        addActor(poof);
        final PamActor arriving = mowers.get(row);
        arriving.setSynced(false);
        arriving.play("transition", false, new Runnable() {
            @Override
            public void run() {
                arriving.play("idle", true, null);
                arriving.setSynced(true);
            }
        });
        final int index = row;
        poof.play("animation", false, new Runnable() {
            @Override
            public void run() {
                Actor gone = poofs.remove(index);
                if (gone != null) {
                    gone.remove();
                }
            }
        });
    }

    private void syncHorde(float delta) {
        List<model.entities.zombies.Zombie> live = controller.zombies();
        for (model.entities.zombies.Zombie zombie : live) {
            if (horde.containsKey(zombie)) {
                continue;
            }
            Actor actor = zombieActor(zombie);
            if (actor != null) {
                horde.put(zombie, actor);
                smoothed.put(zombie, (float) zombie.getPosition().x);
                addActor(actor);
            }
        }
        java.util.Iterator<Map.Entry<model.entities.zombies.Zombie, Actor>> gone =
                horde.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<model.entities.zombies.Zombie, Actor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                dropTornado(entry.getKey());
                playDeath(entry.getKey(), entry.getValue());
                smoothed.remove(entry.getKey());
                clipShown.remove(entry.getKey());
                gone.remove();
            }
        }
        float rate = Math.min(1f, delta * SMOOTHING);
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            model.entities.zombies.Zombie zombie = entry.getKey();
            Float current = smoothed.get(zombie);
            float target = (float) zombie.getPosition().x;
            float value = current == null ? target : current + (target - current) * rate;
            smoothed.put(zombie, value);
            syncZombieClip(zombie, entry.getValue());
            placeZombie(entry.getValue(), value, zombie.getRow(), zombie);
            syncTornado(zombie, entry.getValue());
        }
    }

    private void syncZombieClip(model.entities.zombies.Zombie zombie, Actor actor) {
        if (!(actor instanceof PamActor)) {
            return;
        }
        PamActor rig = (PamActor) actor;
        model.entities.zombies.ZombieRecord record =
                model.entities.zombies.ZombieData.of(zombie.getOrigin());
        String wanted;
        if (zombie instanceof model.entities.zombies.types.RaZombie
                && ((model.entities.zombies.types.RaZombie) zombie).isAbsorbing()) {
            wanted = "power";
        } else if (zombie.getState()
                == model.entities.zombies.ZombieState.ATTACKING) {
            wanted = "eat";
        } else {
            wanted = "walk";
        }
        String clip = clipOr(record, wanted, "idle");
        String showing = clipShown.get(zombie);
        if (!clip.equals(showing)) {
            clipShown.put(zombie, clip);
            rig.play(clip, true, null);
        }
    }

    private void playDeath(model.entities.zombies.Zombie zombie, Actor actor) {
        if (!(actor instanceof PamActor)) {
            actor.remove();
            return;
        }
        final PamActor rig = (PamActor) actor;
        model.entities.zombies.ZombieRecord record =
                model.entities.zombies.ZombieData.of(zombie.getOrigin());
        String clip = clipOr(record, "die", null);
        if (clip == null) {
            rig.remove();
            return;
        }
        rig.setTouchable(Touchable.disabled);
        rig.play(clip, false, new Runnable() {
            @Override
            public void run() {
                rig.remove();
            }
        });
    }

    private static String clipOr(model.entities.zombies.ZombieRecord record,
            String wanted, String fallback) {
        if (record == null || record.getClips() == null) {
            return fallback;
        }
        return record.getClips().contains(wanted) ? wanted : fallback;
    }

    private void syncTornado(model.entities.zombies.Zombie zombie, Actor rider) {
        if (!zombie.isRidingStorm()) {
            dropTornado(zombie);
            return;
        }
        PamActor rear = tornadoes.get(zombie);
        PamActor front = tornadoFronts.get(zombie);
        if (rear == null) {
            rear = spin(Sandstorm.REAR);
            front = spin(Sandstorm.TOP);
            if (rear == null || front == null) {
                return;
            }
            tornadoes.put(zombie, rear);
            tornadoFronts.put(zombie, front);
            addActor(rear);
            addActor(front);
        }
        float grow = rider.getWidth() * (TORNADO_SCALE - 1f) / 2f;
        float x = rider.getX() - grow;
        float w = rider.getWidth() * TORNADO_SCALE;
        float h = rider.getHeight() * TORNADO_SCALE;
        rear.setBounds(x, rider.getY(), w, h);
        front.setBounds(x, rider.getY(), w, h);
        rear.toBack();
        rider.toFront();
        front.toFront();
    }

    private PamActor spin(String path) {
        PamActor rig = PlantStage.anchored(assets, path, "loop",
                TORNADO_CANVAS, TORNADO_CANVAS);
        if (!rig.isReady()) {
            util.Log.warn("gui", "Sandstorm rig " + path + " did not load");
            return null;
        }
        rig.play("loop", true, null);
        return rig;
    }

    private void dropTornado(model.entities.zombies.Zombie zombie) {
        PamActor rear = tornadoes.remove(zombie);
        if (rear != null) {
            rear.remove();
        }
        PamActor front = tornadoFronts.remove(zombie);
        if (front != null) {
            front.remove();
        }
    }

    private void placeZombie(Actor actor, float column, int row,
            model.entities.zombies.Zombie zombie) {
        view.gui.EntityTuning.place(actor, view.gui.EntityTuning.of(
                view.gui.EntityTuning.zombieKey(zombie.getOrigin().name())),
                column, row, true);
    }

    private String chapterName() {
        return controller.chapter() == null ? null : controller.chapter().name();
    }

    private Actor zombieActor(model.entities.zombies.Zombie zombie) {
        model.entities.zombies.ZombieRecord record =
                model.entities.zombies.ZombieData.of(zombie.getOrigin());
        if (record == null || assets == null) {
            return null;
        }
        String clip = record.getClips() != null && record.getClips().contains("walk")
                ? "walk" : "idle";
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
        actor.setRate(walkRate(zombie, record));
        return actor.isReady() ? actor : null;
    }

    private static float walkRate(model.entities.zombies.Zombie zombie,
            model.entities.zombies.ZombieRecord record) {
        float tuned = view.gui.EntityTuning.of(
                view.gui.EntityTuning.zombieKey(zombie.getOrigin().name())).speed;
        double reference = record.getMoveSpeed();
        if (reference <= 0d) {
            return tuned;
        }
        return (float) (tuned * zombie.getSpeed() / reference);
    }

    private void syncPlants() {
        List<Plant> live = controller.planted();
        for (Plant plant : live) {
            if (growing.containsKey(plant)) {
                continue;
            }
            Actor actor = actorFor(plant);
            if (actor != null) {
                growing.put(plant, actor);
                addActor(actor);
            }
        }
        java.util.Iterator<Map.Entry<Plant, Actor>> gone = growing.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<Plant, Actor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove();
                gone.remove();
            }
        }
    }

    private Actor actorFor(Plant plant) {
        PlantRecord record = PlantData.record(plant.getType());
        if (record == null || assets == null || !record.getAnimations().hasPlant()) {
            return null;
        }
        String clip = PlantStage.clipOf(record, "idle");
        PamActor actor = PlantStage.anchored(assets, record.getAnimations().getPlant(), clip,
                record.getAnimations().getCanvasWidth(),
                record.getAnimations().getCanvasHeight());
        actor.setSynced(true);
        if (!actor.isReady()) {
            return null;
        }
        place(actor, (int) plant.getPosition().x, (int) plant.getPosition().y,
                plant.getType());
        return actor;
    }

    private void syncSun() {
        List<Sun> live = controller.loose();
        for (final Sun sun : live) {
            if (shining.containsKey(sun)) {
                continue;
            }
            Actor token = sunToken(sun);
            shining.put(sun, token);
            addActor(token);
        }
        java.util.Iterator<Map.Entry<Sun, Actor>> gone = shining.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<Sun, Actor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove();
                sunFall.remove(entry.getKey());
                gone.remove();
            }
        }
    }

    private Actor sunToken(final Sun sun) {
        Actor token = sunFace(sun);
        placeSun(token, sun);
        UiKit.onClick(token, new Runnable() {
            @Override
            public void run() {
                controller.collect(sun);
            }
        });
        token.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                if (pointer == -1) {
                    token.setColor(1f, 1f, 0.72f, 1f);
                    hovered = sun;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor to) {
                if (pointer == -1) {
                    token.setColor(Color.WHITE);
                    if (hovered == sun) {
                        hovered = null;
                    }
                }
            }
        });
        return token;
    }

    private Actor sunFace(Sun sun) {
        if (assets != null && assets.load(SUN_PAM)) {
            PamActor rig = new PamActor(assets, SUN_PAM, sunClip(sun)).setFit(true);
            if (rig.isReady()) {
                return rig;
            }
        }
        com.badlogic.gdx.graphics.g2d.TextureRegion art =
                assets == null ? null : assets.region("IMAGE_EFFECTS_SUN_SUN_110X110");
        return art != null
                ? new com.badlogic.gdx.scenes.scene2d.ui.Image(art)
                : new com.badlogic.gdx.scenes.scene2d.ui.Image(
                        ui.primitives().circle((int) SUN_SIZE, Theme.SUN,
                                Theme.darken(Theme.SUN_DEEP, 0.2f), 3));
    }

    private static String sunClip(Sun sun) {
        if (sun.getType() == model.entities.SunType.SPECIAL) {
            return "blue";
        }
        if (sun.getType() == model.entities.SunType.RADIOACTIVE) {
            return "red";
        }
        return "animation";
    }

    private float fallOf(Sun sun) {
        float target = sun.isFalling()
                ? Math.max(0f, Math.min(1f,
                        sun.getFallTicksRemaining() / (float) Sun.FALL_TICKS))
                : 0f;
        Float current = sunFall.get(sun);
        if (current == null) {
            sunFall.put(sun, target);
            return target;
        }
        float rate = Math.min(1f, com.badlogic.gdx.Gdx.graphics.getDeltaTime() * SMOOTHING);
        float value = current + (target - current) * rate;
        sunFall.put(sun, value);
        return value;
    }

    private void placeSun(Actor token, Sun sun) {
        float size = SUN_SIZE * (sun.getType() == model.entities.SunType.SPECIAL ? 1.25f
                : sun.getType() == model.entities.SunType.RADIOACTIVE ? 1.45f : 1f);
        float column = (float) sun.getPosition().x;
        float row = (float) sun.getPosition().y;
        float x = LawnGeometry.areaX() + column * LawnGeometry.cellWidth() - size / 2f;
        float ground = LawnGeometry.areaY()
                + (LawnGeometry.ROWS - 1 - row) * LawnGeometry.cellHeight()
                + LawnGeometry.cellHeight() / 2f - size / 2f;
        float y = ground;
        float left = fallOf(sun);
        if (left > 0.001f) {
            float sky = LawnGeometry.areaY() + LawnGeometry.areaHeight() + size;
            y = ground + (sky - ground) * left;
        }
        token.setBounds(x, y, size, size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (showGrid) {
            paintGrid(batch, parentAlpha);
        }
        if (armed != null && hoverColumn >= 0 && hoverRow >= 0) {
            paintHover(batch, parentAlpha);
        }
        super.draw(batch, parentAlpha);
    }

    private void paintGrid(Batch batch, float parentAlpha) {
        Drawable line = ui.primitives().flat(Theme.alpha(Color.WHITE, 0.16f));
        float width = LawnGeometry.cellWidth();
        float height = LawnGeometry.cellHeight();
        batch.setColor(1f, 1f, 1f, parentAlpha);
        for (int c = 0; c < controller.columns(); c++) {
            for (int r = 0; r < controller.rows(); r++) {
                float x = LawnGeometry.columnLeft(c);
                float y = LawnGeometry.rowFeet(r);
                line.draw(batch, x, y, width, 1f);
                line.draw(batch, x, y, 1f, height);
            }
        }
    }

    private void paintHover(Batch batch, float parentAlpha) {
        boolean free = controller.isFree(hoverColumn, hoverRow);
        Drawable tint = ui.primitives().flat(Theme.alpha(
                free ? Theme.GREEN_LIGHT : Theme.RED, 0.28f));
        batch.setColor(1f, 1f, 1f, parentAlpha);
        tint.draw(batch, LawnGeometry.columnLeft(hoverColumn),
                LawnGeometry.rowFeet(hoverRow),
                LawnGeometry.cellWidth(), LawnGeometry.cellHeight());
    }

    private void syncArea() {
        view.gui.EntityTuning.applyGrid(chapterName());
        reposition();
    }

    private void reposition() {
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            Plant plant = entry.getKey();
            place(entry.getValue(), (int) plant.getPosition().x,
                    (int) plant.getPosition().y, plant.getType());
        }
        for (Map.Entry<Sun, Actor> entry : shining.entrySet()) {
            placeSun(entry.getValue(), entry.getKey());
        }
    }

    private void place(Actor actor, int column, int row, Plants type) {
        view.gui.EntityTuning.place(actor, view.gui.EntityTuning.of(
                view.gui.EntityTuning.plantKey(type.name())),
                column, row, false);
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

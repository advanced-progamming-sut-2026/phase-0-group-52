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
    private static final float HOP_TIME = 0.7f;
    private static final float HOP_RISE = 62f;
    private static final float ORIGIN_UP = 0.45f;
    private static final float SMOOTHING = 14f;
    private static final float MOWER_CANVAS = 390f;
    private static final float TOMB_CANVAS = 390f;
    private static final float ARROW_FADE = 0.45f;
    private static final float BLOCK_SPAN = 1.35f;
    private static final float BLOCK_FOOT = 0.15f;
    private static final float SLIDE_EASE = 0.35f;
    private static final float RISE_SPAN = 1.6f;
    private static final float WATER_VEIL = 0.55f;
    private static final float HOP = 0.45f;
    private static final int FREEZE_STAGES = 3;
    private static final float ICE_MIN_ALPHA = 0.4f;
    private static final float ICE_MAX_ALPHA = 0.8f;
    private static final float WIND_SPAN = 1.6f;
    private static final double LOB_SPAN = 4d;
    private static final float LOB_HEIGHT = 1.1f;
    private static final int MAX_SWINGS = 5;
    private static final double DAMAGE_ONE = 0.66d;
    private static final double DAMAGE_TWO = 0.4d;
    private static final double DAMAGE_THREE = 0.18d;
    private static final double FED_INTRO = 0.5d;
    private static final double FED_OUTRO = 0.5d;
    private static final float SHOT_SMOOTHING = 6f;
    private static final float SHOT_LEASH = 1.2f;
    private static final float TORNADO_CANVAS = 320f;
    private static final float TORNADO_SCALE = 1.45f;
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
    private final Map<model.entities.zombies.Zombie, Float> slid =
            new HashMap<model.entities.zombies.Zombie, Float>();
    private final Map<model.entities.zombies.Zombie, Float> smoothed =
            new HashMap<model.entities.zombies.Zombie, Float>();

    private Sink sink;
    private boolean showGrid;
    private Plants armed;
    private int hoverColumn = -1;
    private int hoverRow = -1;
    private Sun hovered;
    private java.util.List<PamActor> mowers;
    private final Map<model.entities.Projectile, PamActor> shots =
            new HashMap<model.entities.Projectile, PamActor>();
    private final Map<model.entities.Projectile, Float> shotFlight =
            new HashMap<model.entities.Projectile, Float>();
    private final Map<String, Actor> icyTiles = new HashMap<String, Actor>();
    private final Map<Object, PamActor> iceBlocks = new HashMap<Object, PamActor>();
    private final java.util.List<PamActor> puffs = new java.util.ArrayList<PamActor>();
    private final Map<Actor, Integer> iceRows = new HashMap<Actor, Integer>();
    private final Map<String, Actor> water = new HashMap<String, Actor>();
    private final Map<Actor, Integer> waterRows = new HashMap<Actor, Integer>();
    private final Map<Plant, java.util.List<PamActor>> laneFire =
            new HashMap<Plant, java.util.List<PamActor>>();
    private final Map<Plant, Integer> swings = new HashMap<Plant, Integer>();
    private final java.util.Set<Plant> wasActing = new java.util.HashSet<Plant>();
    private final Map<model.entities.Tombstone, PamActor> stones =
            new HashMap<model.entities.Tombstone, PamActor>();
    private final Map<model.entities.zombies.Zombie, PamActor> tornadoes =
            new HashMap<model.entities.zombies.Zombie, PamActor>();
    private final Map<model.entities.zombies.Zombie, PamActor> tornadoFronts =
            new HashMap<model.entities.zombies.Zombie, PamActor>();
    private final Map<model.entities.zombies.Zombie, Double> zombieHp =
            new HashMap<model.entities.zombies.Zombie, Double>();
    private final Map<Plant, Double> plantHp = new HashMap<Plant, Double>();
    private final Map<Plant, String> plantClipShown = new HashMap<Plant, String>();
    private final HitFeedback feedback;
    private final Map<model.entities.Tombstone, String> stoneClip =
            new HashMap<model.entities.Tombstone, String>();
    private final Map<model.entities.zombies.Zombie, String> clipShown =
            new HashMap<model.entities.zombies.Zombie, String>();
    private final Map<Integer, Actor> poofs = new HashMap<Integer, Actor>();
    private final java.util.Set<model.entities.Lawnmower> running =
            new java.util.HashSet<model.entities.Lawnmower>();
    private int spawned;
    private final Map<Sun, Float> sunFall = new HashMap<Sun, Float>();
    private final Map<Sun, Float> sunHop = new HashMap<Sun, Float>();

    public LawnField(UiKit ui, Assets assets, LevelController controller) {
        this.ui = ui;
        this.assets = assets;
        this.controller = controller;
        this.feedback = new HitFeedback();
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
        getColor().a = 1f;
        for (com.badlogic.gdx.scenes.scene2d.Group up = getParent(); up != null;
                up = up.getParent()) {
            up.getColor().a = 1f;
        }
        syncArea();
        syncPlants();
        syncSun();
        syncHorde(delta);
        feedback.setCeiling(night());
        syncStatus();
        duskPlants();
        flashDamage(delta);
        syncShots(delta);
        syncLaneFire();
        syncTombstones();
        syncRisings();
        syncWater();
        syncIce(delta);
        syncMowers();
        spawnNextMower(delta);
        placeMowers();
        sortDepth();
    }

    private float night() {
        return controller.chapter() == model.ChapterType.DARK_AGES
                ? view.gui.widgets.NightVeil.ENTITY_BRIGHT : 1f;
    }

    private void syncStatus() {
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            if (!(entry.getValue() instanceof PamActor)) {
                continue;
            }
            PamActor rig = (PamActor) entry.getValue();
            if (feedback.isFlashing(entry.getValue())) {
                continue;
            }
            float dusk = night();
            switch (entry.getKey().status()) {
                case BURNING:
                    rig.setTint(dusk, 0.72f * dusk, 0.62f * dusk, 1f);
                    break;
                case POISONED:
                    rig.setTint(0.8f * dusk, dusk, 0.7f * dusk, 1f);
                    break;
                case FROZEN:
                case CHILLED:
                    rig.setTint(dusk, dusk, dusk, 1f);
                    break;
                default:
                    rig.setTint(dusk, dusk, dusk, 1f);
                    break;
            }
        }
    }

    private void flashDamage(float delta) {
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            double hp = entry.getKey().getHp();
            Double was = zombieHp.get(entry.getKey());
            if (was != null && hp < was) {
                feedback.hit(entry.getValue());
            }
            zombieHp.put(entry.getKey(), hp);
        }
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            double hp = entry.getKey().getHp();
            Double was = plantHp.get(entry.getKey());
            if (was != null && hp < was) {
                feedback.hit(entry.getValue());
            }
            plantHp.put(entry.getKey(), hp);
        }
        feedback.act(delta);
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
        Float mown = mowerDepth(actor);
        if (mown != null) {
            return mown.floatValue();
        }
        Integer wetRow = waterRows.get(actor);
        if (wetRow != null) {
            return view.gui.LawnLayer.TOMBSTONE.depth(
                    LawnGeometry.rowFeet(wetRow.intValue()));
        }
        Integer icy = iceRows.get(actor);
        if (icy != null) {
            return view.gui.LawnLayer.SHOT.depth(
                    LawnGeometry.rowFeet(icy.intValue()));
        }
        for (Map.Entry<model.entities.Projectile, PamActor> entry : shots.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.SHOT.depth(
                        LawnGeometry.rowFeet((float) entry.getKey().getLane()));
            }
        }
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.ZOMBIE.depth(
                        LawnGeometry.rowFeet(entry.getKey().getRow()));
            }
        }
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            if (entry.getValue() == actor) {
                return view.gui.LawnLayer.PLANT.depth(
                        LawnGeometry.rowFeet((int) entry.getKey().getPosition().y));
            }
        }
        return view.gui.LawnLayer.PLANT.depth(actor.getY());
    }

    private void syncShots(float delta) {
        if (assets == null) {
            return;
        }
        java.util.List<model.entities.Projectile> live = controller.projectiles();
        for (model.entities.Projectile shot : live) {
            PamActor actor = shots.get(shot);
            if (actor == null) {
                actor = view.gui.ShotArt.actor(assets, shot.getSource(), shot.getKind(),
                        shot.getVariant());
                if (actor == null) {
                    continue;
                }
                shots.put(shot, actor);
                addActor(actor);
            }
            placeShot(actor, shot, delta);
        }
        java.util.Iterator<Map.Entry<model.entities.Projectile, PamActor>> gone =
                shots.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<model.entities.Projectile, PamActor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                splat(entry.getKey(), entry.getValue());
                entry.getValue().remove();
                shotFlight.remove(entry.getKey());
                gone.remove();
            }
        }
    }

    private void placeShot(PamActor actor, model.entities.Projectile shot, float delta) {
        view.gui.EntityTuning.Tune tune =
                view.gui.EntityTuning.shotTune(shot.getSource(), shot.getPort(),
                        shot.getVariant());
        view.gui.EntityTuning.placeShot(actor, tune, shot.getKind(),
                flightOf(shot, delta), (float) shot.getLane(),
                1f - (float) shot.laneProgress());
        if (shot.isLobbed()) {
            float arc = (float) Math.sin(Math.min(1d,
                    shot.getTravelled() / LOB_SPAN) * Math.PI);
            actor.setY(actor.getY() + arc * LawnGeometry.cellHeight() * LOB_HEIGHT);
        }
    }

    private void syncLaneFire() {
        for (Plant plant : controller.planted()) {
            boolean burning = burns(plant);
            if (burning) {
                fadeLane(plant);
            }
            if (burning && !laneFire.containsKey(plant)) {
                laneFire.put(plant, lightLane(plant));
            } else if (!burning && laneFire.containsKey(plant)) {
                douse(plant);
            }
        }
        java.util.Iterator<Plant> lit = laneFire.keySet().iterator();
        while (lit.hasNext()) {
            Plant plant = lit.next();
            if (!controller.planted().contains(plant)) {
                for (PamActor flame : laneFire.get(plant)) {
                    flame.remove();
                }
                lit.remove();
            }
        }
    }

    private java.util.List<PamActor> lightLane(Plant plant) {
        java.util.List<PamActor> flames = new java.util.ArrayList<PamActor>();
        int row = (int) plant.getPosition().y;
        boolean pierce = plant instanceof model.entities.plants.StrikeThrough;
        int last = pierce ? (int) Math.min(LawnGeometry.COLUMNS,
                plant.getPosition().x + ((model.entities.plants.StrikeThrough) plant).reach())
                : LawnGeometry.COLUMNS;
        for (int column = (int) plant.getPosition().x; column < last; column++) {
            PamActor flame = pierce ? view.gui.ShotArt.beam(assets, plant.getType())
                    : view.gui.ShotArt.laneFire(assets);
            if (flame == null) {
                break;
            }
            view.gui.EntityTuning.placeShot(flame,
                    view.gui.EntityTuning.shotTune(plant.getType(),
                            model.entities.plants.types.FirePeashooter.FLAME,
                            model.entities.Projectile.FED),
                    model.entities.Projectile.Kind.FIRE, column, row);
            addActor(flame);
            flames.add(flame);
        }
        return flames;
    }

    private static boolean burns(Plant plant) {
        if (plant instanceof model.entities.plants.StrikeThrough) {
            return plant.isActing();
        }
        return plant.getType() == Plants.FIRE_PEASHOOTER && plant.isFed()
                && plant.fedRemaining() <= Plant.FED_SHOW - FED_INTRO;
    }

    private void fadeLane(Plant plant) {
        java.util.List<PamActor> flames = laneFire.get(plant);
        if (flames == null) {
            return;
        }
        float alpha = plant instanceof model.entities.plants.StrikeThrough
                || plant.fedRemaining() >= FED_OUTRO ? 1f
                : (float) (plant.fedRemaining() / FED_OUTRO);
        for (PamActor flame : flames) {
            flame.setTint(1f, 1f, 1f, alpha);
        }
    }

    private void douse(Plant plant) {
        for (PamActor flame : laneFire.remove(plant)) {
            flame.remove();
        }
    }

    private void splat(model.entities.Projectile shot, PamActor was) {
        if (!shot.isSpent() || shot.getColumn() < -1d
                || shot.getColumn() > LawnGeometry.COLUMNS + 1) {
            return;
        }
        final PamActor burst = view.gui.ShotArt.splat(assets, shot.getSource(),
                shot.getKind(), shot.getVariant());
        if (burst == null) {
            return;
        }
        burst.setBounds(was.getX(), was.getY(), was.getWidth(), was.getHeight());
        addActor(burst);
        burst.play(burst.clipName(), false, new Runnable() {
            @Override
            public void run() {
                burst.remove();
            }
        });
    }

    private float flightOf(model.entities.Projectile shot, float delta) {
        float target = (float) shot.getColumn();
        Float current = shotFlight.get(shot);
        if (current == null) {
            shotFlight.put(shot, target);
            return target;
        }
        float step = (float) (model.entities.Projectile.SPEED * model.Game.TICKS_PER_SECOND)
                * delta * shot.getDirection() * controller.speed();
        float value = current + step;
        if (Math.abs(target - value) > SHOT_LEASH) {
            value = target;
        } else {
            value += (target - value) * Math.min(1f, delta * SHOT_SMOOTHING);
        }
        shotFlight.put(shot, value);
        return value;
    }

    private void syncIce(float delta) {
        if (assets == null || controller.game() == null
                || controller.game().getField() == null) {
            return;
        }
        syncIcyTiles();
        syncIceBlocks();
        blowChillWind();
        agePuffs(delta);
    }

    private void syncIcyTiles() {
        model.GameField field = controller.game().getField();
        java.util.Set<String> live = new java.util.HashSet<String>();
        for (int row = 0; row < field.getRows(); row++) {
            for (int column = 0; column < field.getCols(); column++) {
                model.entities.Cell cell = field.getCell(column, row);
                if (cell == null || !isIcy(cell.getType())) {
                    continue;
                }
                String at = column + ":" + row;
                live.add(at);
                if (!icyTiles.containsKey(at)) {
                    icyTiles.put(at, addIcyTile(cell.getType(), column, row));
                }
            }
        }
        java.util.Iterator<Map.Entry<String, Actor>> thawed =
                icyTiles.entrySet().iterator();
        while (thawed.hasNext()) {
            Map.Entry<String, Actor> entry = thawed.next();
            if (!live.contains(entry.getKey())) {
                puff(entry.getValue().getX(), entry.getValue().getY());
                entry.getValue().remove();
                thawed.remove();
            }
        }
    }

    private void blowChillWind() {
        for (Integer row : controller.takeChilledRows()) {
            final PamActor gust = view.gui.FrostArt.rig(assets,
                    view.gui.FrostArt.CHILL_WIND, "animation");
            if (gust == null) {
                return;
            }
            view.gui.EntityTuning.Tune tune =
                    view.gui.EntityTuning.of(view.gui.FrostArt.WIND_KEY);
            float height = LawnGeometry.cellHeight() * WIND_SPAN * tune.scale;
            gust.setBounds(LawnGeometry.areaX() + tune.dx,
                    LawnGeometry.rowFeet(row.intValue()) + tune.dy,
                    LawnGeometry.areaWidth(), height);
            addActor(gust);
            puffs.add(gust);
            gust.play(gust.clipName(), false, new Runnable() {
                @Override
                public void run() {
                    gust.remove();
                    puffs.remove(gust);
                }
            });
        }
    }

    private static boolean isIcy(model.entities.CellType type) {
        return type == model.entities.CellType.FROZEN
                || type == model.entities.CellType.SLIPPERY_UP
                || type == model.entities.CellType.SLIPPERY_DOWN;
    }

    private Actor addIcyTile(model.entities.CellType type, int column, int row) {
        com.badlogic.gdx.scenes.scene2d.Group cell =
                new com.badlogic.gdx.scenes.scene2d.Group();
        com.badlogic.gdx.scenes.scene2d.ui.Image sheet =
                new com.badlogic.gdx.scenes.scene2d.ui.Image(assets.region(view.gui.FrostArt.TILE_ICE));
        sheet.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
        view.gui.EntityTuning.Tune tune =
                view.gui.EntityTuning.of(view.gui.FrostArt.TILE_KEY);
        float w = LawnGeometry.cellWidth() * tune.scale;
        float h = LawnGeometry.cellHeight() * tune.scale;
        sheet.setBounds(0f, 0f, w, h);
        cell.addActor(sheet);
        if (type != model.entities.CellType.FROZEN) {
            com.badlogic.gdx.scenes.scene2d.ui.Image arrow =
                    new com.badlogic.gdx.scenes.scene2d.ui.Image(ui.drawable(
                    type == model.entities.CellType.SLIPPERY_UP
                            ? "sortAscending" : "sortDescending"));
            arrow.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            arrow.setColor(1f, 1f, 1f, ARROW_FADE);
            arrow.setBounds(w * 0.34f, h * 0.3f, w * 0.32f, h * 0.4f);
            cell.addActor(arrow);
        }
        cell.setBounds(LawnGeometry.columnLeft(column) + tune.dx,
                LawnGeometry.rowFeet(row) + tune.dy, w, h);
        cell.setTouchable(Touchable.disabled);
        addActor(cell);
        return cell;
    }

    private void syncIceBlocks() {
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            block(entry.getKey(), entry.getValue(),
                    entry.getKey().getFreezeLevel() > 0,
                    view.gui.FrostArt.ICE_PLANT,
                    (int) entry.getKey().getPosition().y);
        }
        for (Map.Entry<model.entities.zombies.Zombie, Actor> entry : horde.entrySet()) {
            block(entry.getKey(), entry.getValue(), entry.getKey().isEncased(),
                    view.gui.FrostArt.ICE_ZOMBIE, entry.getKey().getRow());
        }
    }

    private void block(Object owner, Actor host, boolean iced, String rig,
            int row) {
        PamActor ice = iceBlocks.get(owner);
        if (iced && ice == null) {
            ice = view.gui.FrostArt.rig(assets, rig, "freeze_idle", "idle", "animation");
            if (ice == null) {
                return;
            }
            iceBlocks.put(owner, ice);
            addActor(ice);
        } else if (!iced && ice != null) {
            puff(ice.getX(), ice.getY());
            iceRows.remove(ice);
            ice.remove();
            iceBlocks.remove(owner);
            return;
        }
        if (ice != null) {
            ice.setTint(1f, 1f, 1f, iceAlpha(owner));
            view.gui.EntityTuning.Tune tune =
                    view.gui.EntityTuning.of(owner instanceof Plant
                            ? view.gui.FrostArt.PLANT_BLOCK_KEY
                            : view.gui.FrostArt.ZOMBIE_BLOCK_KEY);
            float span = LawnGeometry.cellWidth() * BLOCK_SPAN * tune.scale;
            float lift = LawnGeometry.cellHeight() * BLOCK_FOOT;
            float middle = host.getX() + host.getWidth() / 2f;
            float floor = host.getY();
            ice.setBounds(middle - span / 2f + tune.dx, floor - lift + tune.dy, span, span);
            iceRows.put(ice, Integer.valueOf(row));
        }
    }

    private Float mowerDepth(Actor actor) {
        if (mowers == null) {
            return null;
        }
        int index = mowers.indexOf(actor);
        if (index < 0) {
            return null;
        }
        model.entities.Lawnmower mower = controller.game() == null
                || controller.game().getField() == null ? null
                : controller.game().getField().getLawnmower(index);
        view.gui.LawnLayer layer = mower != null && mower.isRunning()
                ? view.gui.LawnLayer.ZOMBIE : view.gui.LawnLayer.MOWER_PARKED;
        return Float.valueOf(layer.depth(LawnGeometry.rowFeet(index)));
    }

    private static float iceAlpha(Object owner) {
        int stage = FREEZE_STAGES;
        if (owner instanceof Plant) {
            stage = Math.max(1, Math.min(FREEZE_STAGES,
                    ((Plant) owner).getFreezeLevel()));
        }
        float step = (ICE_MAX_ALPHA - ICE_MIN_ALPHA) / (FREEZE_STAGES - 1);
        return ICE_MIN_ALPHA + step * (stage - 1);
    }

    private static int columnOf(Object owner) {
        if (owner instanceof Plant) {
            return (int) ((Plant) owner).getPosition().x;
        }
        if (owner instanceof model.entities.zombies.Zombie) {
            return ((model.entities.zombies.Zombie) owner).getCol();
        }
        return 0;
    }


    private void puff(float x, float y) {
        final PamActor burst = view.gui.FrostArt.rig(assets,
                view.gui.FrostArt.ICE_PARTICLES, "animation");
        if (burst == null) {
            return;
        }
        burst.setBounds(x, y, LawnGeometry.cellWidth(), LawnGeometry.cellHeight());
        addActor(burst);
        puffs.add(burst);
        burst.play(burst.clipName(), false, new Runnable() {
            @Override
            public void run() {
                burst.remove();
                puffs.remove(burst);
            }
        });
    }

    private void agePuffs(float delta) {
        for (PamActor burst : new java.util.ArrayList<PamActor>(puffs)) {
            burst.toFront();
        }
    }

    private static final String RISE_RIG =
            "768/FULL/EFFECTS/TOMBSTONE_DARK_SPAWN_EFFECT/TOMBSTONE_DARK_SPAWN_EFFECT.PAM";

    private void syncWater() {
        if (controller.game() == null || controller.game().getField() == null) {
            return;
        }
        model.GameField field = controller.game().getField();
        java.util.Set<String> wet = new java.util.HashSet<String>();
        for (int column = 0; column < field.getCols(); column++) {
            for (int row = 0; row < field.getRows(); row++) {
                model.entities.Cell cell = field.getCell(column, row);
                if (cell == null || !cell.getType().isWater()) {
                    continue;
                }
                String key = column + ":" + row;
                wet.add(key);
                Actor pool = water.get(key);
                if (pool == null) {
                    pool = new com.badlogic.gdx.scenes.scene2d.ui.Image(
                            ui.primitives().rounded(10, Theme.WATER_SHALLOW,
                                    Theme.WATER_DEEP, 2));
                    pool.setColor(1f, 1f, 1f, WATER_VEIL);
                    pool.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
                    water.put(key, pool);
                    addActor(pool);
                }
                pool.setBounds(LawnGeometry.columnLeft(column),
                        LawnGeometry.rowFeet(row), LawnGeometry.cellWidth(),
                        LawnGeometry.cellHeight());
                waterRows.put(pool, Integer.valueOf(row));
            }
        }
        java.util.Iterator<Map.Entry<String, Actor>> dried = water.entrySet().iterator();
        while (dried.hasNext()) {
            Map.Entry<String, Actor> entry = dried.next();
            if (!wet.contains(entry.getKey())) {
                waterRows.remove(entry.getValue());
                entry.getValue().remove();
                dried.remove();
            }
        }
    }

    private void syncRisings() {
        if (assets == null || controller.game() == null) {
            return;
        }
        java.util.List<model.Vec2> risen = controller.game().getRisings();
        while (!risen.isEmpty()) {
            model.Vec2 at = risen.remove(0);
            final PamActor rise = view.gui.FrostArt.rig(assets, RISE_RIG, "animation");
            if (rise == null) {
                continue;
            }
            float span = LawnGeometry.cellWidth() * RISE_SPAN;
            rise.setBounds(LawnGeometry.columnLeft((int) at.x)
                            + (LawnGeometry.cellWidth() - span) / 2f,
                    LawnGeometry.rowFeet((int) at.y), span, span);
            addActor(rise);
            puffs.add(rise);
            rise.play(rise.clipName(), false, new Runnable() {
                @Override
                public void run() {
                    puffs.remove(rise);
                    rise.remove();
                }
            });
        }
    }

    private void syncTombstones() {
        if (assets == null || controller.game() == null) {
            return;
        }
        java.util.List<model.entities.Tombstone> live = controller.game().getTombstones();
        for (model.entities.Tombstone stone : live) {
            PamActor actor = stones.get(stone);
            if (actor == null) {
                String tombRig = view.gui.ChapterArt.gravestone(controller.chapter(),
                        stone.getBonus());
                actor = PlantStage.anchored(assets,
                        tombRig, view.gui.FrostArt.clipOf(assets, tombRig,
                                stone.clipName(), "undamaged", "idle"),
                        TOMB_CANVAS, TOMB_CANVAS);
                if (!actor.isReady()) {
                    return;
                }
                actor.play(stone.clipName(), false, null);
                stones.put(stone, actor);
                stoneClip.put(stone, stone.clipName());
                addActor(actor);
            }
            String clip = stone.clipName();
            if (!clip.equals(stoneClip.get(stone))) {
                stoneClip.put(stone, clip);
                actor.play(clip, false, null);
            }
            view.gui.EntityTuning.place(actor,
                    view.gui.EntityTuning.of("tomb|" + chapterName()),
                    stone.getColumn(), stone.getRow(), false);
        }
        java.util.Iterator<Map.Entry<model.entities.Tombstone, PamActor>> gone =
                stones.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<model.entities.Tombstone, PamActor> entry = gone.next();
            if (!live.contains(entry.getKey()) || entry.getKey().isDestroyed()) {
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
            Float lane = slid.get(zombie);
            float wanted = zombie.getRow();
            float lit = lane == null ? wanted : lane + (wanted - lane) * rate * SLIDE_EASE;
            slid.put(zombie, lit);
            syncZombieClip(zombie, entry.getValue());
            if (entry.getValue() instanceof PamActor) {
                ((PamActor) entry.getValue()).setRate(zombie.isEncased() ? 0f : 1f);
            }
            placeZombie(entry.getValue(), value, zombie.getRow(), zombie, lit);
            hop(entry.getValue(), zombie, value);
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

    private void hop(Actor actor, model.entities.zombies.Zombie zombie, float column) {
        if (!(zombie instanceof model.entities.zombies.types.DodoRider)
                || !((model.entities.zombies.types.DodoRider) zombie).isGliding()) {
            return;
        }
        float within = column - (float) Math.floor(column);
        float lift = (float) Math.sin(within * Math.PI) * LawnGeometry.cellHeight() * HOP;
        actor.setY(actor.getY() + lift);
    }

    private void placeZombie(Actor actor, float column, int row,
            model.entities.zombies.Zombie zombie, float lane) {
        view.gui.EntityTuning.place(actor, view.gui.EntityTuning.of(
                view.gui.EntityTuning.zombieKey(zombie.getOrigin().name())),
                column, lane, true);
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

    private String swingClip(Plant plant, PlantRecord record) {
        int most = 1;
        while (most < MAX_SWINGS && record.getAnimations().hasClip("attack" + (most + 1))) {
            most++;
        }
        if (most == 1) {
            return null;
        }
        Integer seen = swings.get(plant);
        int next = seen == null ? 0 : seen.intValue();
        if (!wasActing.contains(plant)) {
            next = (next + 1) % most;
            swings.put(plant, Integer.valueOf(next));
        }
        return next == 0 ? "attack" : "attack" + (next + 1);
    }

    private void duskPlants() {
        float dusk = night();
        if (dusk >= 1f) {
            return;
        }
        for (Actor actor : growing.values()) {
            if (actor instanceof PamActor && !feedback.isFlashing(actor)) {
                ((PamActor) actor).setTint(dusk, dusk, dusk, 1f);
            }
        }
    }

    private void syncPlantClip(Plant plant, Actor actor) {
        if (!(actor instanceof PamActor)) {
            return;
        }
        PlantRecord record = PlantData.record(plant.getType());
        if (record == null) {
            return;
        }
        String wanted = plantClip(plant, record);
        String showing = plantClipShown.get(plant);
        if (wanted != null && !wanted.equals(showing)) {
            plantClipShown.put(plant, wanted);
            PamActor rig = (PamActor) actor;
            rig.setSynced(!plant.isFed() && !plant.isActing());
            rig.play(wanted, true, null);
        }
    }

    private String plantClip(Plant plant, PlantRecord record) {
        String suffix = record.getAnimations().hasClip("idle_stage1")
                ? "_stage" + plant.growthStage() : "";
        if (record.getAnimations().hasClip("loop")) {
            if (plant.isFading() && record.getAnimations().hasClip("outro")) {
                return "outro";
            }
            return plant.isActing() && record.getAnimations().hasClip("intro")
                    ? "intro" : "loop";
        }
        if (plant.isFed()) {
            String staged = fedStage(plant, record);
            if (staged != null) {
                return staged;
            }
            String fed = firstClip(record, "plantfood" + suffix, "plantfood",
                    "plantfood_on", "special" + suffix, "special");
            if (fed != null) {
                return fed;
            }
        }
        String arming = armingStage(plant, record);
        if (arming != null) {
            return arming;
        }
        if (plant.isActing()) {
            String aimed = aimedAttack(plant, record);
            if (aimed != null) {
                return aimed;
            }
            String swing = swingClip(plant, record);
            if (swing != null) {
                return swing;
            }
            String acting = firstClip(record, "special" + suffix, "special", "attack", "bite");
            if (acting != null) {
                return acting;
            }
        }
        String hurt = damageStage(plant, record);
        if (hurt != null) {
            return hurt;
        }
        String idle = firstClip(record, "idle" + suffix, "idle");
        return idle == null ? PlantStage.clipOf(record, "idle") : idle;
    }

    private static String armingStage(Plant plant, PlantRecord record) {
        boolean sleeping = plant instanceof model.entities.plants.types.PotatoMine
                ? !((model.entities.plants.types.PotatoMine) plant).isArmed()
                : plant instanceof model.entities.plants.types.PrimalPotatoMine
                        && !((model.entities.plants.types.PrimalPotatoMine) plant).isArmed();
        if (!sleeping) {
            return null;
        }
        return firstClip(record, "plant_idle", "plant", "idle");
    }

    private static String damageStage(Plant plant, PlantRecord record) {
        if (!record.getAnimations().hasClip("damage")) {
            return null;
        }
        double full = plant.getType().getBaseHP();
        if (full <= 0d) {
            return null;
        }
        double left = plant.getHp() / full;
        if (left > DAMAGE_ONE) {
            return null;
        }
        if (left > DAMAGE_TWO) {
            return firstClip(record, "idle_damage", "damage");
        }
        if (left > DAMAGE_THREE) {
            return firstClip(record, "idle_damage2", "damage2", "damage");
        }
        return firstClip(record, "idle_damage3", "damage3", "damage2", "damage");
    }

    private static String fedStage(Plant plant, PlantRecord record) {
        if (plant instanceof model.entities.plants.types.BowlingBulb) {
            int bulb = ((model.entities.plants.types.BowlingBulb) plant).getBulb();
            return firstClip(record, "plantfood" + bulb, "plantfood");
        }
        if (!record.getAnimations().hasClip("plantfood_loop")) {
            return null;
        }
        double left = plant.fedRemaining();
        if (left > Plant.FED_SHOW - FED_INTRO) {
            return firstClip(record, "plantfood");
        }
        return left < FED_OUTRO
                ? firstClip(record, "plantfood_end", "plantfood_loop")
                : firstClip(record, "plantfood_loop");
    }

    private static String aimedAttack(Plant plant, PlantRecord record) {
        if (!(plant instanceof model.entities.plants.Shooter)) {
            return null;
        }
        java.util.Set<String> fired =
                ((model.entities.plants.Shooter) plant).firedPorts();
        if (plant instanceof model.entities.plants.types.BowlingBulb) {
            int bulb = ((model.entities.plants.types.BowlingBulb) plant).getBulb();
            return firstClip(record, bulb == 1 ? "special" : "special" + bulb, "special");
        }
        if (plant.getType() == Plants.SPLIT_PEA) {
            boolean back = fired.contains(model.entities.plants.types.SplitPea.BACK);
            boolean front = fired.contains(model.entities.plants.types.SplitPea.FRONT);
            if (back && front) {
                return firstClip(record, "attack3", "attack");
            }
            if (back) {
                return firstClip(record, "attack2", "attack");
            }
            return firstClip(record, "attack");
        }
        if (plant.getType() == Plants.PEA_POD && fired.size() > 1) {
            return firstClip(record, "attack " + fired.size(), "attack");
        }
        if (record.getAnimations().hasClip("attack2")) {
            return null;
        }
        if (record.getAnimations().hasClip("special_stage1")) {
            return firstClip(record, "special_stage" + plant.growthStage(), "special_stage1");
        }
        return null;
    }

    private static String firstClip(PlantRecord record, String... names) {
        for (String name : names) {
            if (record.getAnimations().hasClip(name)) {
                return name;
            }
        }
        return null;
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
        for (Map.Entry<Plant, Actor> entry : growing.entrySet()) {
            syncPlantClip(entry.getKey(), entry.getValue());
            if (entry.getValue() instanceof PamActor) {
                ((PamActor) entry.getValue())
                        .setRate(entry.getKey().isFrozenSolid() ? 0f : 1f);
            }
            if (entry.getKey().isActing()) {
                wasActing.add(entry.getKey());
            } else {
                wasActing.remove(entry.getKey());
                swings.remove(entry.getKey());
            }
            if (entry.getKey().isBitten()) {
                feedback.bite(entry.getValue());
            }
        }
        java.util.Iterator<Map.Entry<Plant, Actor>> gone = growing.entrySet().iterator();
        while (gone.hasNext()) {
            Map.Entry<Plant, Actor> entry = gone.next();
            if (!live.contains(entry.getKey())) {
                entry.getValue().remove();
                plantClipShown.remove(entry.getKey());
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
            hopOf(sun, true);
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
                sunHop.remove(entry.getKey());
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

    private float hopOf(Sun sun, boolean fresh) {
        if (sun.isFromSky()) {
            return 1f;
        }
        Float held = sunHop.get(sun);
        if (held == null) {
            float start = fresh ? 0f : 1f;
            sunHop.put(sun, start);
            return start;
        }
        if (fresh) {
            return held;
        }
        float next = Math.min(1f, held
                + com.badlogic.gdx.Gdx.graphics.getDeltaTime() / HOP_TIME);
        sunHop.put(sun, next);
        return next;
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
        float hop = hopOf(sun, false);
        if (hop < 1f) {
            float fromX = LawnGeometry.areaX()
                    + ((int) sun.getPosition().x + 0.5f) * LawnGeometry.cellWidth() - size / 2f;
            float fromY = LawnGeometry.areaY()
                    + (LawnGeometry.ROWS - 1 - (int) sun.getPosition().y)
                    * LawnGeometry.cellHeight()
                    + LawnGeometry.cellHeight() * ORIGIN_UP - size / 2f;
            x = fromX + (x - fromX) * hop;
            y = fromY + (y - fromY) * hop + 4f * HOP_RISE * hop * (1f - hop);
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

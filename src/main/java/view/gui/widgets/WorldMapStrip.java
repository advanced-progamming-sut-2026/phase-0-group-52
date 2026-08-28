package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import model.ChapterType;
import model.adventure.AdventureProgress;
import model.adventure.ChapterMap;
import model.adventure.MapNode;
import model.adventure.MapNodeKind;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.WorldMapArt;

import java.util.ArrayList;
import java.util.List;

public final class WorldMapStrip extends WidgetGroup {

    public interface Listener {
        void onLevel(MapNodeActor actor, boolean playable);

        void onIsland(MapNode node, PlantIsland island);
    }

    private static final float MARGIN = 40f;
    private static final float LEVEL_STEP = 380f;
    private static final float PLANT_STEP = 260f;
    private static final float BASE_Y = 0.56f;
    private static final float AMPLITUDE = 0.085f;
    private static final float WAVELENGTH = 760f;

    private static final float NODE_SIZE = 72f;
    private static final float SPECIAL_SIZE = 235f;
    private static final float SPECIAL_LIFT = 0.30f;
    private static final float ZOMBOSS_SIZE = 420f;
    private static final float TROPHY_SIZE = 92f;
    private static final float ENTRY_LIFT = -0.12f;
    private static final float ENTRY_SHIFT = -0.18f;
    private static final float TROPHY_SINK = 0.34f;
    private static final float DECOR_LIFT = 0.26f;
    private static final float DECOR_DIM = 1f;
    private static final float FOG_LEAD = 220f;
    private static final float FOG_TRAIL = 300f;

    private static final float[] DECOR_SHIFT = {0.46f, -0.48f};

    private final UiKit ui;
    private final Assets assets;
    private final ChapterType chapter;
    private final int cleared;
    private final Listener listener;

    private final List<MapNodeActor> nodes = new ArrayList<MapNodeActor>();
    private final List<Placed> placed = new ArrayList<Placed>();
    private final List<float[]> links = new ArrayList<float[]>();
    private final MapPathLayer paths;
    private MapFog fogLayer;
    private float fogFrom;
    private float fogTo;
    private float span;
    private float edgePad;
    private float centreBias;
    private float focusX;

    public WorldMapStrip(UiKit ui, Assets assets, ChapterType chapter, int cleared,
            AdventureProgress progress, Listener listener) {
        this.ui = ui;
        this.assets = assets;
        this.chapter = chapter;
        this.cleared = cleared;
        this.listener = listener;
        setTransform(false);

        paths = new MapPathLayer(ui, assets, chapter);
        addActor(paths);
        build(progress);
    }

    private void build(AdventureProgress progress) {
        float x = MARGIN;
        int special = 0;
        int plant = 0;
        MapNode previous = null;
        float previousX = 0f;
        for (MapNode node : ChapterMap.of(chapter)) {
            if (previous != null) {
                x += step(previous, node);
                links.add(new float[]{previousX, x, isCleared(previous) ? 1f : 0f});
            }
            if (node.getKind() == MapNodeKind.PLANT) {
                addIsland(node, x, plant, progress);
                plant++;
            } else if (node.getKind() == MapNodeKind.TROPHY) {
                addTrophy(x);
            } else {
                if (node.getLevelNumber() == ChapterType.LEVELS_PER_CHAPTER - 1) {
                    fogFrom = x;
                } else if (node.getKind() == MapNodeKind.ZOMBOSS) {
                    fogTo = x;
                }
                special = addLevel(node, x, special);
            }
            previous = node;
            previousX = x;
        }
        span = x + MARGIN;
        addFog();
    }

    private float step(MapNode from, MapNode to) {
        boolean small = from.getKind() == MapNodeKind.PLANT
                && to.getKind() == MapNodeKind.PLANT;
        return small ? PLANT_STEP : LEVEL_STEP;
    }

    private int addLevel(MapNode node, float x, int special) {
        float size = nodeSize(node);
        int next = special;
        if (node.getKind() == MapNodeKind.SPECIAL) {
            int slot = Math.min(special, DECOR_SHIFT.length - 1);
            addDecor(slot == 0 ? WorldMapArt.statueIsland(chapter)
                    : WorldMapArt.faceIsland(chapter), x + LEVEL_STEP * DECOR_SHIFT[slot]);
            next = special + 1;
        } else if (node.getKind() == MapNodeKind.LEVEL) {
            addEntry(x);
        }
        MapNodeActor actor = new MapNodeActor(ui, assets, chapter, node, stateOf(node));
        actor.setName("node-" + node.getLevelNumber());
        if (focusX <= 0f && stateOf(node) != MapNodeActor.State.CLEARED) {
            focusX = x;
        }
        place(actor, x, size, size, node.getKind() == MapNodeKind.SPECIAL
                ? size * SPECIAL_LIFT : 0f);
        clickable(actor, node, null);
        nodes.add(actor);
        return next;
    }

    private void addFog() {
        if (fogFrom <= 0f || fogTo <= fogFrom) {
            return;
        }
        MapFog fog = new MapFog(ui.primitives());
        addActorAfter(paths, fog);
        fogLayer = fog;
    }

    private void addDecor(int islandNumber, float x) {
        Image decor = atNativeScale(islandNumber);
        if (decor == null) {
            return;
        }
        decor.setName("landmark-" + islandNumber);
        decor.getColor().a = DECOR_DIM;
        addActorAfter(paths, decor);
        placed.add(new Placed(decor, x, decor.getWidth(), decor.getHeight(),
                decor.getHeight() * DECOR_LIFT));
    }

    private void addEntry(float x) {
        Image entry = atNativeScale(WorldMapArt.entryIsland(chapter));
        if (entry == null) {
            return;
        }
        entry.setName("entry-island");
        centreBias = -entry.getWidth() * ENTRY_SHIFT;
        place(entry, x + entry.getWidth() * ENTRY_SHIFT, entry.getWidth(),
                entry.getHeight(), entry.getHeight() * ENTRY_LIFT);
    }

    private Image atNativeScale(int islandNumber) {
        TextureRegion art = assets == null
                ? null : assets.region(WorldMapArt.island(chapter, islandNumber));
        if (art == null) {
            return null;
        }
        Image image = new Image(new TextureRegionDrawable(art));
        image.setScaling(Scaling.stretch);
        image.setSize(art.getRegionWidth() * WorldMapArt.MAP_SCALE,
                art.getRegionHeight() * WorldMapArt.MAP_SCALE);
        return image;
    }

    private float nodeSize(MapNode node) {
        if (node.getKind() == MapNodeKind.ZOMBOSS) {
            return ZOMBOSS_SIZE;
        }
        return node.getKind() == MapNodeKind.SPECIAL ? SPECIAL_SIZE : NODE_SIZE;
    }

    private void addIsland(MapNode node, float x, int plant,
            AdventureProgress progress) {
        Plants claimed = progress == null
                ? null : progress.claimedPlant(chapter, node.getSlot());
        PlantIsland.State state = islandState(node, claimed);
        int[] platforms = WorldMapArt.platforms(chapter);
        int[] trims = WorldMapArt.decor(chapter);
        final MapNode owner = node;
        PlantIsland island = new PlantIsland(ui, assets, chapter,
                platforms[plant % platforms.length],
                trims[plant % trims.length], state, claimed,
                new PlantIsland.Listener() {
                    @Override
                    public void onBurst(PlantIsland from) {
                        if (listener != null) {
                            listener.onIsland(owner, from);
                        }
                    }

                    @Override
                    public void onLocked(PlantIsland from) {
                        if (listener != null) {
                            listener.onIsland(owner, from);
                        }
                    }
                });
        island.setName("island-" + node.getSlot());
        place(island, x, island.getPrefWidth(), island.getPrefHeight(), 0f);
    }

    private void addTrophy(float x) {
        int[] platforms = WorldMapArt.platforms(chapter);
        Image seat = atNativeScale(platforms[0]);
        if (seat != null) {
            place(seat, x, seat.getWidth(), seat.getHeight(),
                    -TROPHY_SIZE * TROPHY_SINK);
        }
        TextureRegion art = assets == null
                ? null : assets.region(WorldMapArt.trophy(chapter));
        if (art == null) {
            return;
        }
        Image trophy = new Image(new TextureRegionDrawable(art));
        trophy.setScaling(Scaling.fit);
        trophy.getColor().a = cleared >= ChapterType.LEVELS_PER_CHAPTER ? 1f : 0.35f;
        place(trophy, x, TROPHY_SIZE, TROPHY_SIZE, 0f);
    }

    private PlantIsland.State islandState(MapNode node, Plants claimed) {
        if (claimed != null) {
            return PlantIsland.State.OPENED;
        }
        return ChapterMap.isSlotOpen(cleared, node.getSlot())
                ? PlantIsland.State.UNLOCKED : PlantIsland.State.LOCKED;
    }

    private MapNodeActor.State stateOf(MapNode node) {
        if (node.getLevelNumber() <= cleared) {
            return MapNodeActor.State.CLEARED;
        }
        return ChapterMap.isLevelPlayable(cleared, node.getLevelNumber())
                ? MapNodeActor.State.CURRENT : MapNodeActor.State.LOCKED;
    }

    private boolean isCleared(MapNode node) {
        if (node.getKind() == MapNodeKind.PLANT) {
            return ChapterMap.isSlotOpen(cleared, node.getSlot());
        }
        return node.getLevelNumber() <= cleared;
    }

    private void clickable(final Actor actor, final MapNode node,
            final PlantIsland.State state) {
        if (listener == null) {
            return;
        }
        actor.setTouchable(Touchable.enabled);
        final boolean playable = ChapterMap.isLevelPlayable(cleared, node.getLevelNumber());
        UiKit.onClick(actor, new Runnable() {
            @Override
            public void run() {
                listener.onLevel((MapNodeActor) actor, playable);
            }
        });
    }

    private void place(Actor actor, float x, float width, float height, float lift) {
        addActor(actor);
        placed.add(new Placed(actor, x, width, height, lift));
    }

    @Override
    public void setCullingArea(com.badlogic.gdx.math.Rectangle area) {
        super.setCullingArea(null);
    }

    @Override
    public void layout() {
        float height = getHeight();
        float base = edgePad + centreBias;
        if (fogLayer != null) {
            fogLayer.setBounds(base + fogFrom - FOG_LEAD, 0f,
                    fogTo - fogFrom + FOG_LEAD + FOG_TRAIL, height);
        }
        paths.setBounds(base, 0f, span, height);
        paths.clearLegs();
        for (float[] link : links) {
            paths.connect(link[0], centreY(link[0], height),
                    link[1], centreY(link[1], height), link[2] > 0.5f);
        }
        for (Placed item : placed) {
            float centre = centreY(item.x, height);
            view.gui.layout.UiLayout.placeAt(this, item.actor, 
                    base + item.x - item.width / 2f,
                    centre - item.height / 2f + item.lift, item.width, item.height);
        }
    }

    private float centreY(float x, float height) {
        double phase = x / WAVELENGTH * Math.PI * 2.0;
        return height * (BASE_Y + AMPLITUDE * (float) Math.sin(phase));
    }

    public float focusX() {
        return (focusX > 0f ? focusX : span) + edgePad + centreBias;
    }

    public void setViewport(float width) {
        float pad = Math.max(0f, width / 2f);
        if (Math.abs(pad - edgePad) > 0.5f) {
            edgePad = pad;
            invalidateHierarchy();
        }
    }

    @Override
    public float getPrefWidth() {
        return span + edgePad * 2f + centreBias;
    }

    @Override
    public float getPrefHeight() {
        return 0f;
    }

    private static final class Placed {
        private final Actor actor;
        private final float x;
        private final float width;
        private final float height;
        private final float lift;

        private Placed(Actor actor, float x, float width, float height, float lift) {
            this.actor = actor;
            this.x = x;
            this.width = width;
            this.height = height;
            this.lift = lift;
        }
    }
}

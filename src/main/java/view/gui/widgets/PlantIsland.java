package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.ChapterType;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.ChapterArt;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.WorldMapArt;

public final class PlantIsland extends WidgetGroup {

    public enum State { LOCKED, UNLOCKED, OPENED }

    public interface Listener {
        void onBurst(PlantIsland island);

        void onLocked(PlantIsland island);
    }

    private static final float PACKET_SCALE = 1f;
    private static final float PACKET_OVERLAP = 0.42f;
    private static final float PACKET_SIT = 0.34f;
    private static final float DECOR_SCALE = 0.42f;
    private static final float DECOR_SIDE = 0.62f;
    private static final float DECOR_SINK = 0.34f;
    private static final float PINATA_SIZE = 96f;
    private static final float PINATA_LIFT = 0.34f;
    private static final float PLANT_SIZE = 148f;
    private static final float PLANT_SIT = 0.42f;
    private static final float CONFETTI_SIZE = 300f;
    private static final float BOB_SWELL = 0.07f;
    private static final float BOB_TIME = 0.9f;
    private static final float SHAKE_STEP = 0.045f;
    private static final float SHAKE_SWING = 9f;
    private static final int BREAK_CLICKS = 4;
    private static final float BREAK_WINDOW = 1.1f;
    private static final float BURST_HOLD = 0.45f;
    private static final float BURST_FADE = 0.35f;

    private final State state;
    private final Listener listener;
    private final ChapterType world;

    private Actor platform;
    private Image decor;
    private Actor packet;
    private Bobber pinata;
    private PamActor pinataRig;
    private PamActor plant;
    private PamActor confetti;

    private float decorWidth;
    private float decorHeight;
    private float platformWidth;
    private float platformHeight;
    private float packetWidth;
    private float packetHeight;

    private float lastClick = -BREAK_WINDOW;
    private int rapidClicks;
    private boolean burst;

    public PlantIsland(UiKit ui, Assets assets, ChapterType chapter, String islandArt,
            String decorArt, State state, Plants prize, Listener listener) {
        this.state = state;
        this.listener = listener;
        this.world = chapter;
        setTransform(false);

        addPlatform(assets, islandArt);
        addDecor(assets, decorArt);
        packetWidth = SeedPacket.ART_W * PACKET_SCALE;
        packetHeight = SeedPacket.ART_H * PACKET_SCALE;

        if (state == State.OPENED && prize != null) {
            addPrize(ui, assets, prize);
        } else if (state == State.UNLOCKED) {
            addPinata(assets, chapter);
        } else {
            packet = mystery(ui, assets, chapter);
            packet.setName("packet");
            addActor(packet);
        }
        setTouchable(Touchable.enabled);
        addListener(clicks());
    }

    public State state() {
        return state;
    }

    private void addPlatform(Assets assets, String islandArt) {
        TextureRegion art = assets == null ? null : assets.region(islandArt);
        if (art == null) {
            return;
        }
        platformWidth = art.getRegionWidth() * WorldMapArt.MAP_SCALE;
        platformHeight = art.getRegionHeight() * WorldMapArt.MAP_SCALE;
        String rig = WorldMapArt.animOf(world, islandArt);
        if (rig != null) {
            PamActor live = WorldMapArt.rigged(assets, rig, "idle", "idle");
            if (live.isReady()) {
                platform = live;
                platform.setName("platform");
                addActor(platform);
                return;
            }
        }
        Image still = new Image(new TextureRegionDrawable(art));
        still.setScaling(Scaling.stretch);
        platform = still;
        platform.setName("platform");
        addActor(platform);
    }

    private void addDecor(Assets assets, String decorArt) {
        TextureRegion trim = assets == null || decorArt == null
                ? null : assets.region(decorArt);
        if (trim == null) {
            return;
        }
        decorWidth = trim.getRegionWidth() * WorldMapArt.MAP_SCALE * DECOR_SCALE;
        decorHeight = trim.getRegionHeight() * WorldMapArt.MAP_SCALE * DECOR_SCALE;
        decor = new Image(new TextureRegionDrawable(trim));
        decor.setName("trim");
        decor.setScaling(Scaling.stretch);
        addActor(decor);
    }

    private void addPinata(Assets assets, ChapterType chapter) {
        PamActor rig = WorldMapArt.rigged(assets, WorldMapArt.pinata(chapter), "idle", "idle");
        if (!rig.isReady()) {
            return;
        }
        pinataRig = rig;
        pinata = new Bobber(rig);
        pinata.setName("pinata");
        addActor(pinata);
        bob();
    }

    private void addPrize(UiKit ui, Assets assets, Plants prize) {
        if (assets != null && assets.loadPlant(prize)) {
            plant = WorldMapArt.rigged(assets, assets.plantPam(prize), "idle", "idle");
            if (plant.isReady()) {
                plant.setName("plant");
                addActor(plant);
            } else {
                plant = null;
            }
        }
        packet = new SeedPacket(ui, assets, prize, SeedPacket.Mode.ALMANAC, PACKET_SCALE);
        packet.setName("packet");
        addActor(packet);
    }

    private void bob() {
        pinata.clearActions();
        pinata.addAction(Actions.forever(Actions.sequence(
                Actions.scaleTo(1f, 1f + BOB_SWELL, BOB_TIME,
                        com.badlogic.gdx.math.Interpolation.sine),
                Actions.scaleTo(1f, 1f, BOB_TIME,
                        com.badlogic.gdx.math.Interpolation.sine),
                Actions.delay(com.badlogic.gdx.math.MathUtils.random(1.4f, 3.2f)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        shake();
                    }
                }))));
    }

    private void shake() {
        if (pinata == null) {
            return;
        }
        pinata.addAction(Actions.sequence(
                Actions.rotateBy(-SHAKE_SWING, SHAKE_STEP),
                Actions.rotateBy(SHAKE_SWING * 2f, SHAKE_STEP * 2f),
                Actions.rotateBy(-SHAKE_SWING * 2f, SHAKE_STEP * 2f),
                Actions.rotateBy(SHAKE_SWING, SHAKE_STEP)));
    }

    private static final class Bobber extends WidgetGroup {
        private final Actor inner;

        Bobber(Actor inner) {
            this.inner = inner;
            setTransform(true);
            setTouchable(Touchable.disabled);
            addActor(inner);
        }

        @Override
        public void layout() {
            inner.setBounds(0f, 0f, getWidth(), getHeight());
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

    private ClickListener clicks() {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tapped();
            }
        };
    }

    private void tapped() {
        if (listener == null) {
            return;
        }
        if (state != State.UNLOCKED || pinata == null || burst) {
            listener.onLocked(PlantIsland.this);
            return;
        }
        float now = com.badlogic.gdx.utils.TimeUtils.nanoTime() / 1.0e9f;
        rapidClicks = now - lastClick <= BREAK_WINDOW ? rapidClicks + 1 : 1;
        lastClick = now;
        if (rapidClicks < BREAK_CLICKS) {
            shake();
            return;
        }
        burst = true;
        listener.onBurst(this);
    }

    public void burst(Assets assets, final Runnable done) {
        if (pinata == null) {
            done.run();
            return;
        }
        confetti = WorldMapArt.rigged(assets, WorldMapArt.CONFETTI,
                WorldMapArt.CONFETTI_CLIP, WorldMapArt.CONFETTI_CLIP);
        if (confetti.isReady()) {
            addActor(confetti);
            invalidate();
        }
        pinata.clearActions();
        pinata.setRotation(0f);
        pinata.setScale(1f);
        String pop = WorldMapArt.clipOr(assets, WorldMapArt.pinata(world), "explode", "idle");
        pinataRig.play(pop, false, null);
        pinata.addAction(Actions.sequence(Actions.delay(BURST_HOLD),
                Actions.fadeOut(BURST_FADE), Actions.run(done)));
    }

    private Actor mystery(UiKit ui, Assets assets, ChapterType chapter) {
        WidgetGroup card = new WidgetGroup();
        TextureRegion back = assets == null
                ? null : assets.region(ChapterArt.packet(chapter));
        if (back != null) {
            Image face = new Image(new TextureRegionDrawable(back));
            face.setScaling(Scaling.stretch);
            face.setFillParent(true);
            card.addActor(face);
        }
        Label shade = new Label("?", ui.skin(), "hugeOnDark");
        shade.setAlignment(Align.center);
        shade.setColor(Theme.alpha(Theme.PORTAL_VOID, 0.75f));
        shade.setFillParent(true);
        card.addActor(shade);

        Label mark = new Label("?", ui.skin(), "hugeOnDark");
        mark.setAlignment(Align.center);
        mark.setColor(Theme.SUN);
        mark.setFillParent(true);
        card.addActor(mark);
        return card;
    }

    @Override
    public void layout() {
        float width = getWidth();
        if (platform != null) {
            placeAt(platform, (width - platformWidth) / 2f, 0f,
                    platformWidth, platformHeight);
        }
        if (decor != null) {
            placeAt(decor, (width + platformWidth) / 2f - decorWidth * DECOR_SIDE,
                    platformHeight * (1f - DECOR_SINK), decorWidth, decorHeight);
        }
        layoutPrize(width);
        if (pinata != null) {
            placeAt(pinata, (width - PINATA_SIZE) / 2f, platformHeight * PINATA_LIFT,
                    PINATA_SIZE, PINATA_SIZE);
            pinata.setOrigin(Align.center);
        }
        if (confetti != null) {
            placeAt(confetti, (width - CONFETTI_SIZE) / 2f,
                    platformHeight * PINATA_LIFT - CONFETTI_SIZE / 3f,
                    CONFETTI_SIZE, CONFETTI_SIZE);
        }
    }

    private void layoutPrize(float width) {
        if (plant != null) {
            placeAt(plant, (width - PLANT_SIZE) / 2f, platformHeight * PLANT_SIT,
                    PLANT_SIZE, PLANT_SIZE);
        }
        if (packet == null) {
            return;
        }
        float lift = plant == null
                ? platformHeight * (1f - PACKET_OVERLAP) : platformHeight * PACKET_SIT;
        placeAt(packet, (width - packetWidth) / 2f, lift, packetWidth, packetHeight);
    }

    private void placeAt(Actor child, float x, float y, float width, float height) {
        view.gui.layout.UiLayout.placeAt(this, child, x, y, width, height);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(platformWidth + decorWidth * DECOR_SIDE, packetWidth);
    }

    @Override
    public float getPrefHeight() {
        float top = platformHeight * (1f - PACKET_OVERLAP) + packetHeight;
        if (pinata != null) {
            top = Math.max(top, platformHeight * PINATA_LIFT + PINATA_SIZE);
        }
        if (plant != null) {
            top = Math.max(top, platformHeight * PLANT_SIT + PLANT_SIZE);
        }
        return top;
    }
}

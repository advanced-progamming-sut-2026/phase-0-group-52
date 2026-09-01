package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.AdventureMenuController;
import model.ChapterType;
import model.Result;
import model.adventure.ChapterMap;
import model.adventure.MapNode;
import model.adventure.MapNodeKind;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.MapBackdrop;
import view.gui.widgets.MapNodeActor;
import view.gui.widgets.PamActor;
import view.gui.widgets.PlantIsland;
import view.gui.widgets.WorldMapStrip;

public final class WorldMapScreen extends BaseScreen
        implements WorldMapStrip.Listener, view.gui.layout.UiLayout.Scoped {

    private static final float PORTAL_COVERAGE = 1f;
    private static final float PORTAL_TINT = 0.26f;
    private static final float HEADING_PAD = 10f;
    private static final String MAP_LAYER = "world-map";

    private final AdventureMenuController controller;

    private ChapterType chapter;
    private ScrollPane scroll;
    private MapBackdrop backdrop;
    private WorldMapStrip strip;
    private boolean focused;
    private float keepScrollX = -1f;

    public WorldMapScreen(GameContext context) {
        super(context, "Adventure");
        this.controller = new AdventureMenuController(context.app());
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected void build() {
        chapter = context.app().getSelectedChapter();
        if (chapter == null) {
            chapter = ChapterType.first();
        }
        int cleared = controller.clearedLevels(chapter);
        focused = false;
        strip = new WorldMapStrip(ui, context.assets(), chapter, cleared,
                context.user() == null ? null : context.user().getAdventure(), this);

        scroll = new ScrollPane(strip, barelessStyle());
        scroll.setScrollingDisabled(false, true);
        scroll.setScrollbarsVisible(false);
        scroll.setFadeScrollBars(true);
        scroll.setSmoothScrolling(true);
        scroll.setOverscroll(false, false);
        UiKit.focusOnHover(scroll);

        backdrop = new MapBackdrop(context.assets(), chapter, strip.getPrefWidth());

        Stack layers = new Stack();
        PamActor portal = new PamActor(context.assets(), Assets.PORTAL, "idle")
                .setFit(false)
                .setCoverage(PORTAL_COVERAGE);
        if (portal.isReady()) {
            portal.getColor().a = PORTAL_TINT;
            layers.add(portal);
        }
        layers.add(backdrop);
        layers.add(scroll);
        layers.add(headingLayer(cleared));

        com.badlogic.gdx.scenes.scene2d.Actor stale = stage.getRoot().findActor(MAP_LAYER);
        if (stale != null) {
            stale.remove();
        }
        layers.setName(MAP_LAYER);
        layers.setFillParent(true);
        stage.getRoot().addActorAt(1, layers);
    }

    private Table headingLayer(int cleared) {
        Table pill = new Table();
        pill.setBackground(ui.primitives().rounded(Theme.RADIUS,
                Theme.alpha(Theme.PORTAL_VOID, 0.72f), Theme.alpha(Theme.SUN, 0.5f), 2));
        Label heading = new Label(chapter.getDisplayName() + "   "
                + cleared + " / " + ChapterType.LEVELS_PER_CHAPTER + " levels   "
                + claimed() + " / " + ChapterMap.PLANT_ISLANDS + " plants",
                ui.skin(), "onDark");
        heading.setAlignment(Align.left);
        pill.add(heading).pad(HEADING_PAD / 2f, HEADING_PAD, HEADING_PAD / 2f, HEADING_PAD);

        Table box = new Table();
        box.top().left();
        box.add(pill).left().pad(HEADING_PAD);
        box.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        return box;
    }

    @Override
    protected float contentPad() {
        return 0f;
    }

    private ScrollPane.ScrollPaneStyle barelessStyle() {
        ScrollPane.ScrollPaneStyle bare =
                new ScrollPane.ScrollPaneStyle(ui.skin().get(ScrollPane.ScrollPaneStyle.class));
        bare.hScroll = null;
        bare.hScrollKnob = null;
        bare.vScroll = null;
        bare.vScrollKnob = null;
        bare.background = null;
        bare.corner = null;
        return bare;
    }

    @Override
    public String layoutScope() {
        ChapterType world = context.app().getSelectedChapter();
        return "WorldMapScreen-" + (world == null ? ChapterType.first() : world).name();
    }

    @Override
    protected String backdropImage() {
        return "assets/backgrounds/main_menu.png";
    }

    private int claimed() {
        return context.user() == null
                ? 0 : context.user().getAdventure().claimedCount(chapter);
    }

    @Override
    protected void refresh() {
        if (scroll == null) {
            return;
        }
        strip.setViewport(scroll.getWidth());
        if (!focused) {
            scroll.validate();
        }
        if (!focused && scroll.getMaxX() > 0f) {
            focused = true;
            scrollTo(keepScrollX >= 0f ? keepScrollX : 0f);
            keepScrollX = -1f;
        }
        if (backdrop != null) {
            backdrop.setOffset(scroll.getScrollX());
        }
    }

    public void scrollToFraction(float fraction) {
        if (scroll != null) {
            focused = true;
            scrollTo(scroll.getMaxX() * fraction);
        }
    }

    private void scrollTo(float x) {
        scroll.setScrollX(x);
        scroll.updateVisualScroll();
    }

    @Override
    public void onLevel(MapNodeActor actor, boolean playable) {
        MapNode node = actor.node();
        if (context.settings().isDebugMode()) {
            cheatStep(actor, node, playable);
            return;
        }
        if (!playable) {
            context.toasts().error("Clear the level before it to unlock this one.");
            return;
        }
        Result result = controller.openLevel(chapter, node.getLevelNumber(),
                node.getKind() == MapNodeKind.SPECIAL ? node.getSpecial() : null);
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
        }
    }

    private void cheatStep(MapNodeActor actor, MapNode node, boolean playable) {
        if (!playable) {
            advance(actor, controller.unlockLevel(chapter, node.getLevelNumber()),
                    node, " unlocked.", false);
            return;
        }
        int cleared = controller.clearedLevels(chapter);
        if (node.getLevelNumber() > cleared) {
            advance(actor, controller.completeLevel(chapter, node.getLevelNumber()),
                    node, " cleared.", true);
            return;
        }
        Result result = controller.openLevel(chapter, node.getLevelNumber(),
                node.getKind() == MapNodeKind.SPECIAL ? node.getSpecial() : null);
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
        }
    }

    private void advance(MapNodeActor actor, Result result, MapNode node,
            String suffix, boolean clearing) {
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().info((node.getLabel().isEmpty()
                ? "Level " + node.getLevelNumber() : node.getLabel()) + suffix);
        Runnable rebuild = new Runnable() {
            @Override
            public void run() {
                keepScrollX = scroll == null ? -1f : scroll.getScrollX();
                refreshTopBar();
                show();
            }
        };
        if (clearing) {
            actor.playClearing(rebuild);
        } else {
            actor.playOpening(rebuild);
        }
    }

    @Override
    public void onIsland(MapNode node, PlantIsland island) {
        PlantIsland.State state = island.state();
        if (state == PlantIsland.State.OPENED) {
            Plants owned = context.user() == null ? null
                    : context.user().getAdventure().claimedPlant(chapter, node.getSlot());
            context.toasts().info(owned == null
                    ? "Already opened." : owned.getName() + " came from this island.");
            return;
        }
        if (state == PlantIsland.State.LOCKED) {
            openIsland(node);
            return;
        }
        Result result = controller.claimIsland(chapter, node.getSlot());
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().info(((Plants) result.getObject()).getName() + " is yours!");
        island.burst(context.assets(), new Runnable() {
            @Override
            public void run() {
                keepScrollX = scroll == null ? -1f : scroll.getScrollX();
                refreshTopBar();
                show();
            }
        });
    }

    private void openIsland(MapNode node) {
        int needed = ChapterMap.levelRequiredFor(node.getSlot());
        if (!context.settings().isDebugMode()) {
            context.toasts().error("Clear level " + needed + " to reach this island.");
            return;
        }
        Result result = controller.completeLevel(chapter, needed);
        if (!result.isSuccess()) {
            result = controller.unlockLevel(chapter, needed);
        }
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().info("Island opened.");
        keepScrollX = scroll == null ? -1f : scroll.getScrollX();
        refreshTopBar();
        show();
    }
}

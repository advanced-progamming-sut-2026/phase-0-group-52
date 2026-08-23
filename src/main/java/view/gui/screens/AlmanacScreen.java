package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.entities.plants.PlantAnimations;
import model.entities.plants.PlantData;
import model.entities.plants.PlantProgress;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import controller.menu.CollectionMenuController;
import view.gui.Animations;
import view.gui.Assets;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.TopBar;
import view.gui.UiKit;
import view.gui.widgets.AlmanacFilterPopup;
import view.gui.widgets.PamActor;
import view.gui.widgets.SeedPacket;
import view.gui.widgets.XpBar;

import java.util.ArrayList;
import java.util.List;

public final class AlmanacScreen extends BaseScreen {

    private static final float STAGE_WIDTH = 362f;
    private static final float NAME_HEIGHT = 48f;
    private static final float STAGE_HEIGHT = 162f;
    private static final float ARROW = 40f;
    private static final float XP_HEIGHT = 12f;
    private static final float BADGE_SIZE = 64f;
    private static final float ACTION_ICON = 62f;
    private static final float PRICE_ICON = 24f;
    private static final float TILE_HEIGHT = 54f;
    private static final float TILE_GAP = 3f;
    private static final int STAGE_RADIUS = 12;
    private static final float STAGE_COVERAGE = 1.72f;
    private static final float SHADOW_OFFSET = 5f;
    private static final float COST_ROW = 26f;
    private static final int BURST_COPIES = 3;
    private static final float BURST_COVERAGE = 1.2f;
    private static final float STAGE_DROP = 0.30f;
    private static final float XP_FILL_HEIGHT = 8f;
    private static final float XP_FILL_INSET = 3f;
    private static final float XP_ROW = 36f;
    private static final float BOLT_EXTENT = 120f;
    private static final float BOLT_DROP = 0.22f;
    private static final float XP_FONT = 1f;
    private static final com.badlogic.gdx.graphics.Color STATE_GREY =
            new com.badlogic.gdx.graphics.Color(0.82f, 0.85f, 0.8f, 1f);
    private static final int TURF_CROP_W = 420;
    private static final int TURF_CROP_H = 300;
    private static final float PACKET_SCALE = 0.68f;
    private static final int PACKET_ROWS = 3;
    private static final float INNER_PAD = 9f;
    private static final int PANEL_RADIUS = 30;
    private static final int PANEL_BORDER = 3;
    private static final float TAB_WIDTH = 54f;
    private static final float TAB_HEIGHT = 54f;
    private static final float TAB_ACTIVE_HEIGHT = 72f;
    private static final float TAB_ICON = 38f;
    private static final float TAB_ICON_TOP = 5f;
    private static final float TAB_INDENT = 14f;
    private static final float TAB_GAP = 3f;
    private static final float TAB_SEAM = 14f;
    private static final int TAB_RADIUS = 14;
    private static final float OUTER_PAD = 12f;
    private static final float STRIP_HEIGHT = 160f;
    private static final Color PANEL_SHADE = new Color(0.62f, 0.6f, 0.58f, 1f);
    private static final Color DIMMED = new Color(0.55f, 0.55f, 0.58f, 1f);

    private final AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
    private final CollectionMenuController controller;
    private final view.gui.widgets.AlmanacControls controls;
    private final view.gui.widgets.StatTiles tiles;

    private int tileColumn;
    private Plants selected = Plants.SUNFLOWER;
    private int clipIndex;
    private PamActor stageActor;
    private PamActor shadowActor;
    private Table burstLayer;
    private Label stateLabel;
    private ScrollPane strip;
    private float scrollX;

    public AlmanacScreen(GameContext context) {
        super(context, "Almanac");
        this.controller = new CollectionMenuController(context.app());
        this.controls = new view.gui.widgets.AlmanacControls(context.ui(), context.assets());
        this.tiles = new view.gui.widgets.StatTiles(context.ui(), context.assets());
    }

    @Override
    protected TopBar.Section section() {
        return TopBar.Section.ALMANAC;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected String backdropImage() {
        return "assets/backgrounds/main_menu.png";
    }

    private Assets art() {
        return context.assets();
    }

    private PlantRecord record() {
        return PlantData.record(selected);
    }

    private PlantProgress progress() {
        if (context.user() == null) {
            return new PlantProgress(selected);
        }
        return context.user().getPlants().progress(selected);
    }

    private boolean unlocked() {
        return progress().isUnlocked();
    }

    @Override
    protected void build() {
        content.clearChildren();
        content.top();

        Table panel = ui.panel();
        panel.pad(OUTER_PAD);
        panel.setColor(PANEL_SHADE);
        panel.top();

        Table body = new Table();
        body.top();
        body.add(inner(leftColumn())).width(STAGE_WIDTH).growY();
        body.add(inner(statsPane())).grow();
        panel.add(body).grow().row();

        panel.add(packetStrip()).growX().height(STRIP_HEIGHT)
                .padTop(Theme.PAD).padLeft(Theme.PAD_SMALL).padRight(Theme.PAD_SMALL)
                .padBottom(Theme.PAD);

        Table below = new Table();
        below.top();
        below.add(panel).grow().padTop(TAB_HEIGHT - TAB_SEAM);

        Table above = new Table();
        above.top().left();
        above.add(tabRow()).left().padLeft(TAB_INDENT).growX();

        Stack stack = new Stack();
        stack.add(below);
        stack.add(above);
        content.add(stack).grow();
    }

    private Table inner(Table body) {
        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(PANEL_RADIUS, Theme.PANEL,
                Theme.OUTLINE_SOFT, PANEL_BORDER));
        frame.pad(INNER_PAD);
        frame.add(body).grow();
        return frame;
    }

    private void rebuild() {
        if (strip != null) {
            scrollX = strip.getScrollX();
        }
        content.clear();
        build();
    }

    private Table tabRow() {
        Table row = new Table();
        row.top().left();
        row.add(artTab("PLANTS", true, null)).size(TAB_WIDTH, TAB_ACTIVE_HEIGHT).top();
        row.add(artTab("ZOMBIES", false, new Runnable() {
            @Override
            public void run() {
                context.toasts().info("The zombie almanac is not built yet.");
            }
        })).size(TAB_WIDTH, TAB_HEIGHT).top().padLeft(TAB_GAP);
        row.add(new Table()).growX();
        row.add(filterTab()).height(TAB_HEIGHT).top().padRight(TAB_INDENT);
        return row;
    }

    private Table artTab(String kind, boolean active, final Runnable onClick) {
        Table cell = new Table();
        Drawable face = regionOf("IMAGE_UI_ALMANAC_TABS_" + kind
                + (active ? "_ACTIVE" : "_DOWN"));
        if (face != null) {
            cell.setBackground(face);
        } else {
            cell.setBackground(ui.primitives().rounded(TAB_RADIUS,
                    active ? Theme.GREEN : Theme.PANEL_SUNKEN, Theme.OUTLINE, 2));
        }
        Drawable icon = regionOf("IMAGE_UI_STORE_TABICONS_" + kind);
        if (icon != null) {
            Image mark = new Image(icon);
            mark.setScaling(Scaling.fit);
            cell.add(mark).size(TAB_ICON).top().padTop(TAB_ICON_TOP).expandY();
        }
        if (onClick != null) {
            UiKit.onClick(cell, onClick);
        }
        return cell;
    }

    private Table filterTab() {
        Table cell = new Table();
        cell.setBackground(ui.primitives().rounded(TAB_RADIUS,
                Theme.PANEL, Theme.OUTLINE_SOFT, 2));
        Label label = new Label("Filter", ui.skin(), "rowHeader");
        cell.add(label).pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD)
                .padTop(UiKit.opticalPad(label));
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                openFilter();
            }
        });
        return cell;
    }

    private void openFilter() {
        new AlmanacFilterPopup(context, rules, new Runnable() {
            @Override
            public void run() {
                rebuild();
            }
        }).showOn(stage);
    }

    private Table leftColumn() {
        Table column = new Table();
        column.top();
        column.add(nameHeader()).growX().minHeight(NAME_HEIGHT).row();
        column.add(stagePanel()).growX().height(STAGE_HEIGHT).row();
        column.add(levelingPanel()).growX().padTop(Theme.PAD_SMALL);
        return column;
    }

    private com.badlogic.gdx.graphics.Color levelColour() {
        switch (progress().getLevel()) {
            case 2:  return Theme.SUN;
            case 3:  return Theme.plantFamily("EXPLOSIVE");
            case 4:  return Theme.plantFamily("MAGIC");
            default: return Theme.GREEN;
        }
    }

    private Table nameHeader() {
        Color face = levelColour();
        Table header = new Table();
        header.setBackground(ui.primitives().rounded(Theme.RADIUS, face,
                Theme.darken(face, 0.45f), 4));
        header.center();

        Table gloss = new Table();
        gloss.setBackground(ui.primitives().rounded(Theme.RADIUS,
                Theme.alpha(Color.WHITE, 0.16f), null, 0));

        Table text = new Table();
        text.center();
        Label name = new Label(selected.getName(), ui.skin(), "titleOnDark");
        name.setEllipsis(true);
        name.setAlignment(Align.center);
        text.add(name).growX().minWidth(0f).height(NAME_HEIGHT * 0.56f).center()
                .padLeft(Theme.PAD).padRight(Theme.PAD).row();

        Label.LabelStyle base = ui.skin().get("smallOnDark", Label.LabelStyle.class);
        stateLabel = new Label(currentClip(), new Label.LabelStyle(base.font, STATE_GREY));
        stateLabel.setAlignment(Align.center);
        text.add(stateLabel).center().height(NAME_HEIGHT * 0.34f).padTop(-3f).padBottom(2f);

        Stack stack = new Stack();
        Table glossHolder = new Table();
        glossHolder.top();
        glossHolder.add(gloss).growX().height(NAME_HEIGHT * 0.42f).padTop(3f)
                .padLeft(5f).padRight(5f);
        stack.add(glossHolder);
        stack.add(text);

        header.add(stack).grow();
        return header;
    }

    private List<String> clips() {
        List<String> names = new ArrayList<String>();
        PlantRecord r = record();
        if (r != null) {
            names.addAll(r.getAnimations().getClips().keySet());
        }
        if (names.isEmpty()) {
            names.add("idle");
        }
        return names;
    }

    private String currentClip() {
        List<String> all = clips();
        return all.get(Math.floorMod(clipIndex, all.size()));
    }

    private Drawable turf() {
        TextureRegion base = art() == null ? null : art().region(view.gui.ChapterArt.turf(record()));
        if (base == null) {
            return ui.primitives().flat(Theme.PANEL_SUNKEN);
        }
        int w = Math.min(base.getRegionWidth(), TURF_CROP_W);
        int h = Math.min(base.getRegionHeight(), TURF_CROP_H);
        int x = base.getRegionX() + (base.getRegionWidth() - w) / 2;
        int y = base.getRegionY() + (base.getRegionHeight() - h) / 2;
        return new TextureRegionDrawable(new TextureRegion(base.getTexture(), x, y, w, h));
    }

    private PamActor plantActor(PlantRecord r, boolean shadow) {
        PamActor actor = new PamActor(art(), r.getAnimations().getPlant(), currentClip())
                .setFit(true)
                .setCoverage(STAGE_COVERAGE)
                .setClipped(true);
        float half = canvasSize(r) / 2f;
        actor.setExtent(-half, -half + half * STAGE_DROP, half * 2f, half * 2f);
        if (shadow) {
            actor.setColor(0f, 0f, 0f, 0.32f);
        }
        return actor;
    }

    private Table stagePanel() {
        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(STAGE_RADIUS, Theme.PANEL_SUNKEN,
                Theme.OUTLINE_SOFT, PANEL_BORDER));
        frame.pad(PANEL_BORDER + 1f);

        Stack stack = new Stack();
        Table turfLayer = new Table();
        turfLayer.setBackground(turf());
        stack.add(turfLayer);

        PlantRecord r = record();
        if (r != null && r.getAnimations().hasPlant() && art() != null) {
            PamActor shade = plantActor(r, true);
            if (shade.isReady()) {
                shadowActor = shade;
                Table shadow = new Table();
                shadow.add(shade).grow().padLeft(SHADOW_OFFSET).padTop(SHADOW_OFFSET);
                stack.add(shadow);
            }
            PamActor actor = plantActor(r, false);
            if (actor.isReady()) {
                stageActor = actor;
                Table holder = new Table();
                holder.add(actor).grow();
                stack.add(holder);
            }
            burstLayer = new Table();
            stack.add(burstLayer);
        }
        frame.add(stack).grow();
        if (!unlocked()) {
            frame.setColor(DIMMED);
        }

        Stack shell = new Stack();
        Table frameHolder = new Table();
        frameHolder.add(frame).grow().padLeft(ARROW / 2f).padRight(ARROW / 2f);
        shell.add(frameHolder);

        Table arrows = new Table();
        arrows.add(arrow(false)).size(ARROW).left().expandX();
        arrows.add(arrow(true)).size(ARROW).right().expandX();
        shell.add(arrows);

        Table wrap = new Table();
        wrap.add(shell).grow();
        return wrap;
    }

    private float canvasSize(PlantRecord r) {
        int widest = Math.max(r.getAnimations().getCanvasWidth(),
                r.getAnimations().getCanvasHeight());
        return widest <= 0 ? 390f : widest;
    }

    private Table arrow(final boolean forward) {
        Table cell = new Table();
        Drawable art = regionOf(forward
                ? "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_NEXT"
                : "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_PREVIOUS");
        if (art != null) {
            final Image mark = new Image(art);
            mark.setScaling(Scaling.fit);
            cell.add(mark).grow();
            controls.hoverTint(cell, mark);
        } else {
            cell.add(new Label(forward ? ">" : "<", ui.skin(), "rowHeader"));
        }
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                clipIndex += forward ? 1 : -1;
                showClip();
            }
        });
        return cell;
    }

    private void showClip() {
        String clip = currentClip();
        if (stateLabel != null) {
            stateLabel.setText(clip);
        }
        if (stageActor != null) {
            stageActor.play(clip, true, null);
        }
        if (shadowActor != null) {
            shadowActor.play(clip, true, null);
        }
    }

    private Drawable regionOf(String id) {
        if (art() == null) {
            return null;
        }
        TextureRegion found = art().region(id);
        return found == null ? null : new TextureRegionDrawable(found);
    }

    private Table levelingPanel() {
        Table panel = new Table();
        PlantRecord r = record();
        PlantProgress state = progress();
        if (r == null || !unlocked()) {
            return panel;
        }
        boolean maxed = state.isMaxLevel();
        if (!maxed) {
            panel.add(xpBar(state)).growX().height(XP_ROW)
                    .padLeft(ARROW / 2f).padRight(ARROW / 2f).row();
        }
        panel.add(actionRow(r, state, maxed)).growX().padTop(maxed ? 0f : Theme.PAD_SMALL);
        return panel;
    }

    private Table actionRow(PlantRecord r, PlantProgress state, boolean maxed) {
        Table row = new Table();
        row.left();
        if (r.isBoostable()) {
            row.add(controls.iconButton("IMAGE_UI_PERKS_RIFT_ICON_SUNBOOST", canBoost(), true,
                    new Runnable() {
                @Override
                public void run() {
                    doBoost();
                }
            })).size(ACTION_ICON).bottom().padRight(4f);
            Table gems = new Table();
            gems.left();
            gems.add(controls.priceCell(String.valueOf(r.getGemCost()), "gemIcon", PRICE_ICON))
                    .left().height(COST_ROW).row();
            gems.add(new Table()).height(COST_ROW);
            row.add(gems).left();
        }
        row.add(new Table()).growX();
        if (maxed) {
            Label done = new Label("Max level", ui.skin(), "muted");
            row.add(done).right().padRight(4f).padTop(UiKit.opticalPad(done));
            return row;
        }

        int next = state.getLevel() + 1;
        Table costs = new Table();
        costs.right();
        costs.add(controls.priceCell(state.getPackets() + "/"
                + r.getLeveling().packetsToLevel(next),
                "IMAGE_UI_STOREMULTI_SEEDPACKETMINIICON", PRICE_ICON))
                .right().height(COST_ROW).row();
        costs.add(controls.priceCell(String.valueOf(r.getLeveling().coinsToLevel(next)),
                "coinIcon", PRICE_ICON)).right().height(COST_ROW);
        row.add(costs).right().padRight(6f);

        row.add(controls.iconButton("IMAGE_UI_ALMANAC_ALMANAC_BOOST_LARGE", canUpgrade(), new Runnable() {
            @Override
            public void run() {
                doUpgrade();
            }
        })).size(ACTION_ICON).bottom();
        return row;
    }

    private Table xpBar(PlantProgress state) {
        Drawable trackArt = regionOf("IMAGE_UI_LEVELING_PROGRESS_BG");
        if (trackArt == null) {
            trackArt = ui.primitives().rounded((int) (XP_HEIGHT / 2f),
                    Theme.darken(Theme.PANEL_SUNKEN, 0.4f), Theme.OUTLINE, 1);
        }
        Drawable fillArt = ui.primitives().rounded((int) (XP_FILL_HEIGHT / 2f),
                state.isXpFull() ? Theme.GREEN : Theme.SUN,
                Theme.darken(state.isXpFull() ? Theme.GREEN : Theme.SUN, 0.35f), 1);

        XpBar bar = new XpBar(trackArt, fillArt, state.xpRatio(), XP_FILL_INSET);

        Label count = new Label(state.getXp() + " / " + state.xpNeeded(),
                ui.skin(), "packetCost");
        count.setAlignment(Align.center);
        count.setFontScale(XP_FONT);

        Table barHolder = new Table();
        barHolder.add(bar).growX().height(XP_HEIGHT);

        Stack stack = new Stack();
        stack.add(barHolder);

        Table text = new Table();
        text.add(count).center();
        stack.add(text);

        Table over = new Table();
        over.right();
        over.add(bolt(state)).size(BADGE_SIZE);
        stack.add(over);

        Table holder = new Table();
        holder.add(stack).grow();
        return holder;
    }

    private Table bolt(PlantProgress state) {
        Table holder = new Table();
        if (art() == null) {
            return holder;
        }
        PamActor badge = new PamActor(art(), Assets.UPGRADE_BADGE,
                state.isXpFull() ? "idle" : "no_charge").setFit(true);
        badge.setExtent(-BOLT_EXTENT, -BOLT_EXTENT + BOLT_EXTENT * BOLT_DROP,
                BOLT_EXTENT * 2f, BOLT_EXTENT * 2f);
        if (badge.isReady()) {
            holder.add(badge).grow();
        }
        return holder;
    }

    private boolean canBoost() {
        return context.user() != null && PlantData.canBoost(context.user(), selected);
    }

    private boolean canUpgrade() {
        if (context.user() == null) {
            return false;
        }
        return context.settings().isDebugMode()
                || PlantData.canUpgrade(context.user(), selected);
    }

    private void doBoost() {
        if (!canBoost()) {
            context.toasts().error("Not enough diamonds to boost.");
            return;
        }
        controller.handleCommand("menu collection boost-plant -p " + selected.getName());
        rebuild();
    }

    private void doUpgrade() {
        String blocker = context.user() == null ? "No user is signed in."
                : PlantData.upgradeBlocker(context.user(), selected);
        if (blocker != null) {
            context.toasts().error(blocker);
            return;
        }
        controller.handleCommand("menu collection upgrade-plant -p " + selected.getName());
        celebrate();
        rebuild();
    }


    private void celebrate() {
        if (art() == null || burstLayer == null) {
            return;
        }
        burstLayer.clearChildren();
        for (int i = 0; i < BURST_COPIES; i++) {
            PamActor burst = new PamActor(art(), Assets.BOOST_EFFECT, "animation")
                    .setFit(false)
                    .setCoverage(BURST_COVERAGE + i * 0.25f);
            if (!burst.isReady()) {
                return;
            }
            Table holder = new Table();
            holder.setFillParent(true);
            holder.add(burst).grow();
            burstLayer.addActor(holder);
            holder.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(1.1f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.7f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor()));
        }
    }

    private Table statsPane() {
        Table box = new Table() {
            @Override
            public float getPrefWidth() {
                return 0f;
            }
        };
        box.top().left();
        PlantRecord r = record();
        if (r == null) {
            return box;
        }

        tileColumn = 0;
        addCombatStats(box);
        java.util.Set<String> seen = new java.util.LinkedHashSet<String>();
        for (String taken : new String[]{"Sun Cost", "Recharge", "Toughness", "Damage",
            "Attack Speed", "Attack Rate", "Damage per Second", "Attack Damage",
            "Sun Production", "Firing Rate", "Growth Speed", "Damage Details"}) {
            seen.add(key(taken));
        }
        List<PlantRecord.Stat> rows = new ArrayList<PlantRecord.Stat>(r.getStats());
        rows.addAll(r.getDetails());
        for (PlantRecord.Stat stat : rows) {
            if (seen.add(key(stat.getLabel()))) {
                addTile(box, stat.getLabel(), stat.getValue(), false);
            }
        }

        closeTiles(box, r);

        if (!r.getDescription().isEmpty()) {
            Label desc = new Label(r.getDescription(), ui.skin(), "rowSub");
            desc.setWrap(true);
            box.add(desc).colspan(2).growX().minWidth(0f).left()
                    .padTop(Theme.PAD).padLeft(TILE_GAP).row();
        }
        if (!r.getFlavorText().isEmpty()) {
            Label flavor = new Label(r.getFlavorText(), ui.skin(), "story");
            flavor.setWrap(true);
            box.add(flavor).colspan(2).growX().minWidth(0f).left()
                    .padTop(Theme.PAD).padLeft(TILE_GAP).row();
        }

        return statsScroller(box);
    }

    private Table statsScroller(Table box) {
        ScrollPane scroll = new ScrollPane(box, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, true);
        UiKit.focusOnHover(scroll);

        Table holder = new Table();
        holder.add(scroll).grow();
        if (!unlocked()) {
            holder.setColor(DIMMED);
        }
        return holder;
    }

    private static String key(String label) {
        return label == null ? "" : label.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private void addCombatStats(Table box) {
        int level = progress().getLevel();
        int cost = PlantData.effectiveCost(selected, level);
        int recharge = PlantData.effectiveRecharge(selected, level);
        int hp = PlantData.effectiveHp(selected, level);
        int damage = PlantData.effectiveDamage(selected, level);
        double interval = PlantData.effectiveInterval(selected, level);

        addTile(box, "Sun cost", String.valueOf(cost), cost != selected.getCost());
        addTile(box, "Recharge", recharge + "s", recharge != (int) selected.getRecharge());
        addTile(box, "Toughness", String.valueOf(hp), hp != selected.getBaseHP());
        if (selected.getDamage() > 0) {
            addTile(box, "Damage", String.valueOf(damage), damage != selected.getDamage());
        }
        boolean attacks = selected.getDamage() > 0 && selected.getActionInterval() > 0;
        if (attacks) {
            boolean moved = Math.abs(interval - selected.getActionInterval()) > 0.01;
            addTile(box, "Attack rate", String.format("%.1fs", interval), moved);
            addTile(box, "Damage per second", String.format("%.1f", damage / interval),
                    moved || damage != selected.getDamage());
        } else if (selected.getActionInterval() > 0) {
            addTile(box, "Production time", String.format("%.0fs", interval),
                    Math.abs(interval - selected.getActionInterval()) > 0.01);
        }
    }

    private void closeTiles(Table box, PlantRecord r) {
        if (tileColumn % 2 == 1) {
            box.add(new Table()).growX().uniformX().pad(TILE_GAP).height(TILE_HEIGHT);
        }
        box.row();
        box.add(tiles.categoryTile(r)).colspan(2).growX()
                .pad(TILE_GAP).height(TILE_HEIGHT).row();
    }

    private void addTile(Table grid, String label, String value, boolean upgraded) {
        grid.add(tiles.tile(label, value, upgraded)).growX().uniformX()
                .pad(TILE_GAP).height(TILE_HEIGHT);
        tileColumn++;
        if (tileColumn % 2 == 0) {
            grid.row();
        }
    }

    private Table packetStrip() {
        List<Plants> plants = rules.apply();
        final float restore = scrollX;
        Table grid = new Table();
        grid.bottom().left();
        int rows = PACKET_ROWS;
        int columns = (plants.size() + rows - 1) / rows;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int at = column * rows + row;
                if (at < plants.size()) {
                    SeedPacket card = packet(plants.get(at));
                    grid.add(card).size(card.width(), card.height());
                } else {
                    grid.add().size(1f, 1f);
                }
            }
            grid.row();
        }

        ScrollPane scroll = new ScrollPane(grid, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(false, true);
        scroll.setOverscroll(true, false);
        UiKit.focusOnHover(scroll);
        scroll.validate();
        scroll.setScrollX(restore);
        scroll.updateVisualScroll();
        strip = scroll;

        Table holder = new Table();
        holder.bottom();
        holder.add(scroll).grow();
        return holder;
    }

    private SeedPacket packet(final Plants plant) {
        final boolean isLocked = context.user() != null
                && !context.user().getPlants().isUnlocked(plant);
        SeedPacket card = new SeedPacket(ui, art(), plant, SeedPacket.Mode.ALMANAC, PACKET_SCALE);
        card.setLocked(isLocked);
        card.setLevel(context.user() == null ? 1 : context.user().getPlantLevel(plant));
        card.setSelected(plant == selected);
        card.setBoosted(context.user() != null && context.user().getStoredBoosts().contains(plant));
        card.onClick(new Runnable() {
            @Override
            public void run() {
                if (isLocked && context.settings().isDebugMode()) {
                    context.user().getPlants().grant(plant, 1);
                    context.toasts().success(plant.getName() + " unlocked.");
                }
                selected = plant;
                clipIndex = 0;
                rebuild();
            }
        });
        return card;
    }

    @Override
    public void show() {
        super.show();
        if (strip != null) {
            uiStage().setScrollFocus(strip);
        }
    }
}

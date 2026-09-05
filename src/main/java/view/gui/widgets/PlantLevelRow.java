package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import controller.menu.CollectionMenuController;
import model.entities.plants.PlantData;
import model.entities.plants.PlantProgress;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

public final class PlantLevelRow extends Table {

    public static final float ROW_HEIGHT = 108f;

    private static final float XP_HEIGHT = 12f;
    private static final float XP_FILL_HEIGHT = 8f;
    private static final float XP_FILL_INSET = 3f;
    private static final float XP_ROW = 36f;
    private static final float XP_FONT = 1f;
    private static final float BADGE_SIZE = 64f;
    private static final float ACTION_ICON = 62f;
    private static final float PRICE_ICON = 24f;
    private static final float COST_ROW = 26f;
    private static final float BOLT_EXTENT = 120f;
    private static final float BOLT_DROP = 0.22f;
    private static final float SIDE_PAD = 20f;
    private static final int BURST_COPIES = 2;
    private static final float BURST_COVERAGE = 1.4f;

    private final GameContext context;
    private final UiKit ui;
    private final AlmanacControls controls;
    private final CollectionMenuController controller;
    private final Runnable onChange;

    private Plants plant;
    private Table burstHost;

    public PlantLevelRow(GameContext context, Runnable onChange) {
        this.context = context;
        this.ui = context.ui();
        this.controls = new AlmanacControls(context.ui(), context.assets());
        this.controller = new CollectionMenuController(context.app());
        this.onChange = onChange;
    }

    public PlantLevelRow setBurstHost(Table host) {
        this.burstHost = host;
        return this;
    }

    public void show(Plants value) {
        this.plant = value;
        clearChildren();
        PlantRecord record = record();
        if (record == null || !unlocked()) {
            return;
        }
        PlantProgress state = progress();
        boolean maxed = state.isMaxLevel();
        if (!maxed) {
            add(xpBar(state)).growX().height(XP_ROW)
                    .padLeft(SIDE_PAD / 2f).padRight(SIDE_PAD / 2f).row();
        }
        add(actionRow(record, state, maxed)).growX().padTop(maxed ? 0f : Theme.PAD_SMALL);
    }

    private Assets art() {
        return context.assets();
    }

    private PlantRecord record() {
        return plant == null ? null : PlantData.record(plant);
    }

    private PlantProgress progress() {
        return context.user() == null ? new PlantProgress(plant)
                : context.user().getPlants().progress(plant);
    }

    private boolean unlocked() {
        return context.user() == null || progress().isUnlocked();
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
        row.add(costs(r, state)).right().padRight(6f);
        row.add(controls.iconButton("IMAGE_UI_ALMANAC_ALMANAC_BOOST_LARGE", canUpgrade(),
                new Runnable() {
                    @Override
                    public void run() {
                        doUpgrade();
                    }
                })).size(ACTION_ICON).bottom();
        return row;
    }

    private Table costs(PlantRecord r, PlantProgress state) {
        int next = state.getLevel() + 1;
        Table box = new Table();
        box.right();
        box.add(controls.priceCell(state.getPackets() + "/"
                + r.getLeveling().packetsToLevel(next),
                "IMAGE_UI_STOREMULTI_SEEDPACKETMINIICON", PRICE_ICON))
                .right().height(COST_ROW).row();
        box.add(controls.priceCell(String.valueOf(r.getLeveling().coinsToLevel(next)),
                "coinIcon", PRICE_ICON)).right().height(COST_ROW);
        return box;
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
        Table text = new Table();
        text.add(count).center();
        Table over = new Table();
        over.right();
        over.add(bolt(state)).size(BADGE_SIZE);

        Stack stack = new Stack();
        stack.add(barHolder);
        stack.add(text);
        stack.add(over);
        Table holder = new Table();
        holder.add(stack).grow();
        return holder;
    }

    private Drawable regionOf(String id) {
        com.badlogic.gdx.graphics.g2d.TextureRegion found =
                art() == null ? null : art().region(id);
        return found == null ? null : new TextureRegionDrawable(found);
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
        return context.user() != null && PlantData.canBoost(context.user(), plant);
    }

    private boolean canUpgrade() {
        if (context.user() == null) {
            return false;
        }
        return context.settings().isDebugMode()
                || PlantData.canUpgrade(context.user(), plant);
    }

    private void doBoost() {
        if (!canBoost()) {
            context.toasts().error("Not enough diamonds to boost.");
            return;
        }
        controller.boostPlant(plant);
        changed();
    }

    private void doUpgrade() {
        String blocker = context.user() == null ? "No user is signed in."
                : PlantData.upgradeBlocker(context.user(), plant);
        if (blocker != null) {
            context.toasts().error(blocker);
            return;
        }
        controller.upgradePlant(plant);
        celebrate();
        changed();
    }

    private void changed() {
        if (onChange != null) {
            onChange.run();
        }
    }

    private void celebrate() {
        if (art() == null || burstHost == null) {
            return;
        }
        burstHost.clearChildren();
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
            burstHost.addActor(holder);
            holder.addAction(Actions.sequence(Actions.delay(1.1f),
                    Actions.fadeOut(0.7f), Actions.removeActor()));
        }
    }
}

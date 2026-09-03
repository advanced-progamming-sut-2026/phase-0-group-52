package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.GreenhouseController;
import model.greenhouse.Pot;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.widgets.PotSlot;

import java.util.ArrayList;
import java.util.List;

public final class GreenhouseScreen extends BaseScreen {

    public static final String BACKDROP = "IMAGE_BACKGROUNDS_ZEN_GARDEN";
    public static final String CAN_ICON =
            "IMAGE_ZEN_GARDEN_ZENGARDEN_WATER_POURING_ZENGARDEN_WATER_POURING_317X281";
    public static final String SPROUT_ICON = "IMAGE_UI_SPROUTS_STACK_1";
    public static final String SHOVEL_ICON = "IMAGE_UI_HUD_INGAME_SHOVEL_ICON";
    public static final String DOOBER_RIG =
            "768/INITIAL/ZEN_GARDEN/SPROUTDOOBER/SPROUTDOOBER.PAM";
    public static final String REVEAL_RIG =
            "768/INITIAL/ZEN_GARDEN/ZENGARDEN_SPROUT_REVEAL/ZENGARDEN_SPROUT_REVEAL.PAM";
    public static final String POUR_RIG =
            "768/INITIAL/ZEN_GARDEN/ZENGARDEN_WATER_POURING/ZENGARDEN_WATER_POURING.PAM";

    private static final int COLUMNS = 4;
    private static final float TOOL = 72f;
    private static final float GEM_ICON = 26f;
    private static final float GEM_TEXT = 1.25f;
    private static final float COIN = 40f;
    private static final float COIN_RISE = 0.32f;
    private static final float COIN_FALL = 0.38f;
    private static final float PLUS_ICON = 34f;

    private final GreenhouseController garden;
    private final List<PotSlot> slots = new ArrayList<PotSlot>();
    private final java.util.Set<Pot> ripened = new java.util.HashSet<Pot>();
    private final java.util.Map<Pot, Float> watering =
            new java.util.HashMap<Pot, Float>();
    private final java.util.Map<Pot, Long> soaked =
            new java.util.HashMap<Pot, Long>();
    private boolean digging;
    private Image held;
    private boolean carryingSprout;
    private view.gui.widgets.PamActor doober;

    private Label sproutCount;
    private Table board;

    public GreenhouseScreen(GameContext context) {
        super(context, "Greenhouse");
        this.garden = new GreenhouseController(context.app());
    }

    @Override
    protected view.gui.TopBar.Section section() {
        return view.gui.TopBar.Section.GREENHOUSE;
    }

    @Override
    protected String backdropRegion() {
        return BACKDROP;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected void build() {
        slots.clear();
        content.top().left();
        content.add(tools()).top().left().pad(Theme.PAD_LARGE).row();
        content.add(view.gui.layout.UiLayout.positioned(content, plots()))
                .expand().center();
    }

    private Table tools() {
        Table bar = new Table();
        Table can = new Table();
        can.add(toolButton(CAN_ICON, new Runnable() {
            @Override
            public void run() {
                pickUp();
            }
        })).size(TOOL).row();
        can.add(price()).padTop(2f).row();
        can.add(ui.iconButton(view.gui.Icons.SHOVEL, "Dig", Theme.GREEN_DARK, new Runnable() {
            @Override
            public void run() {
                takeShovel();
            }
        })).size(TOOL).padTop(Theme.PAD_SMALL).row();
        bar.add(can).top();

        sproutCount = new Label("", ui.skin(), "onDark");
        Table stock = pill(SPROUT_ICON, sproutCount);
        stock.setTouchable(Touchable.enabled);
        stock.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (x < event.getListenerActor().getWidth() - PLUS_ICON) {
                    takeSprout();
                }
            }
        });
        bar.add(stock).top().padLeft(Theme.PAD_LARGE * 2f);
        refreshCounts();
        return bar;
    }

    private Actor toolButton(String icon, Runnable action) {
        Table face = ui.faceButton("", "secondary", action);
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack =
                new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(face);
        Table art = new Table();
        art.add(fitted(icon)).size(TOOL * 0.72f);
        stack.add(art);
        return stack;
    }

    private Table price() {
        Table cost = new Table();
        Image gem = new Image(ui.drawable("gemIcon"));
        gem.setScaling(Scaling.fit);
        cost.add(gem).size(GEM_ICON).padRight(2f);
        Label amount = new Label(String.valueOf(GreenhouseController.CAN_PRICE),
                ui.skin(), "titleOnDark");
        amount.setFontScale(GEM_TEXT);
        cost.add(amount).padLeft(2f);
        return cost;
    }

    private Table pill(String icon, Label amount) {
        Table holder = new Table();
        holder.setBackground(ui.drawable("counter"));
        holder.add(amount).expandX().center().padLeft(TOOL * 0.5f).padRight(PLUS_ICON);
        Table front = new Table();
        front.left();
        front.add(fitted(icon)).size(TOOL * 0.52f).padLeft(-TOOL * 0.1f);
        com.badlogic.gdx.scenes.scene2d.ui.Stack stack =
                new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(holder);
        stack.add(front);
        stack.add(plusSlot());
        Table wrap = new Table();
        wrap.add(stack).height(TOOL * 0.62f).minWidth(TOOL * 2.1f);
        return wrap;
    }

    private Table plusSlot() {
        Table slot = new Table();
        slot.right();
        Image plus = new Image(ui.drawable("plusIcon"));
        plus.setScaling(Scaling.fit);
        plus.setTouchable(Touchable.enabled);
        plus.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                event.stop();
                garden.addSprout();
                refreshCounts();
            }
        });
        slot.add(plus).size(PLUS_ICON).padRight(-PLUS_ICON * 0.12f);
        return slot;
    }

    private Image fitted(String icon) {
        Image art = context.assets() == null || context.assets().region(icon) == null
                ? new Image() : new Image(context.assets().region(icon));
        art.setScaling(Scaling.fit);
        art.setTouchable(Touchable.disabled);
        return art;
    }

    private Table plots() {
        board = new Table();
        board.setName("pot-board");
        int column = 0;
        for (final Pot pot : garden.pots()) {
            PotSlot slot = new PotSlot(context.assets(), pot, ui.skin());
            slot.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    tap(pot);
                }
            });
            slots.add(slot);
            board.add(slot).size(PotSlot.SLOT_WIDTH, PotSlot.SLOT_HEIGHT);
            if (++column % COLUMNS == 0) {
                board.row();
            }
        }
        return board;
    }

    private void takeSprout() {
        if (garden.sprouts() <= 0) {
            toasts.info("You have no sprouts. Zombies drop them.");
            return;
        }
        garden.dropCan();
        digging = false;
        carryingSprout = !carryingSprout;
        syncShimmer();
        syncHeld();
    }

    private void takeShovel() {
        garden.dropCan();
        carryingSprout = false;
        digging = !digging;
        syncShimmer();
        syncHeld();
    }

    private void pickUp() {
        digging = false;
        carryingSprout = false;
        if (!garden.canAffordWatering()) {
            toasts.info("You need " + GreenhouseController.CAN_PRICE + " gems to water a plant.");
            return;
        }
        if (garden.holdingCan()) {
            garden.dropCan();
        } else {
            garden.pickUpCan();
        }
        syncShimmer();
        syncHeld();
    }

    private void tap(Pot pot) {
        if (digging) {
            if (garden.dig(pot)) {
                toasts.info("Dug up the pot.");
            }
            digging = false;
            syncHeld();
            return;
        }
        if (garden.holdingCan()) {
            if (garden.water(pot)) {
                pour(pot);
            }
            syncShimmer();
            syncHeld();
            refreshCounts();
            return;
        }
        if (pot.isMarigold() && pot.isReady()) {
            dropCoins(pot, garden.harvest(pot));
            return;
        }
        if (carryingSprout && !pot.isOccupied() && garden.sow(pot)) {
            carryingSprout = false;
            syncHeld();
            PotSlot slot = slotFor(pot);
            if (slot != null) {
                slot.splash(REVEAL_RIG);
            }
            refreshCounts();
        }
    }

    private void pour(Pot pot) {
        watering.put(pot, Float.valueOf(GreenhouseController.WATER_SECONDS));
        soaked.put(pot, Long.valueOf(pot.remainingMillis()));
        for (PotSlot slot : slots) {
            if (slot.pot() == pot) {
                slot.splash(POUR_RIG);
                slot.setWatering(true);
            }
        }
    }

    private void dropCoins(Pot pot, int coins) {
        PotSlot slot = slotFor(pot);
        if (slot == null || coins <= 0) {
            garden.collect(coins);
            return;
        }
        com.badlogic.gdx.math.Vector2 at = slot.localToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(slot.getWidth() / 2f,
                        slot.getHeight() * 0.4f));
        int batches = coins / GreenhouseController.COIN_BATCH;
        for (int i = 0; i < batches; i++) {
            tossCoin(at.x, at.y, i, batches);
        }
    }

    private void tossCoin(float x, float y, int index, int total) {
        final Image token = new Image(ui.drawable("coinIcon"));
        token.setScaling(Scaling.fit);
        token.setBounds(x - COIN / 2f, y, COIN, COIN);
        token.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x2, float y2) {
                garden.collect(GreenhouseController.COIN_BATCH);
                toasts.success("You got " + GreenhouseController.COIN_BATCH + " coins.");
                token.remove();
            }
        });
        stage.addActor(token);
        float spread = (index - (total - 1) / 2f) * COIN * 1.15f;
        token.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(
                                spread, COIN * 1.1f, COIN_RISE,
                                com.badlogic.gdx.math.Interpolation.circleOut),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                                1.1f, 1.1f, COIN_RISE)),
                com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy(
                                0f, -COIN * 0.75f, COIN_FALL,
                                com.badlogic.gdx.math.Interpolation.bounceOut),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(
                                1f, 1f, COIN_FALL))));
    }

    private PotSlot slotFor(Pot pot) {
        for (PotSlot slot : slots) {
            if (slot.pot() == pot) {
                return slot;
            }
        }
        return null;
    }

    private void advanceWatering(float delta) {
        java.util.Iterator<java.util.Map.Entry<Pot, Float>> pouring =
                watering.entrySet().iterator();
        while (pouring.hasNext()) {
            java.util.Map.Entry<Pot, Float> entry = pouring.next();
            float left = entry.getValue().floatValue() - delta;
            Pot pot = entry.getKey();
            Long span = soaked.get(pot);
            if (span != null) {
                pot.hasten((long) (span.longValue() * delta
                        / GreenhouseController.WATER_SECONDS));
            }
            if (left <= 0f) {
                pot.finishGrowth();
                soaked.remove(pot);
                pouring.remove();
                PotSlot done = slotFor(pot);
                if (done != null) {
                    done.setWatering(false);
                }
            } else {
                entry.setValue(Float.valueOf(left));
            }
        }
    }

    private void syncHeld() {
        String icon = garden.holdingCan() ? CAN_ICON : digging ? SHOVEL_ICON : null;
        if (carryingSprout) {
            carrySprout();
            view.gui.UiKit.hideCursor();
        } else if (doober != null) {
            doober.remove();
            doober = null;
        }
        if (icon == null) {
            if (held != null) {
                held.remove();
                held = null;
            }
            if (!carryingSprout) {
                view.gui.UiKit.useGameCursor();
            }
            return;
        }
        if (held == null) {
            held = fitted(icon);
            held.setSize(TOOL, TOOL);
            stage.addActor(held);
            view.gui.UiKit.hideCursor();
        } else {
            held.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                    context.assets().region(icon)));
        }
        held.toFront();
    }

    private void carrySprout() {
        if (doober == null) {
            doober = new view.gui.widgets.PamActor(context.assets(), DOOBER_RIG, "animation")
                    .setFit(true);
            if (!doober.isReady()) {
                doober = null;
                return;
            }
            stage.addActor(doober);
        }
        doober.toFront();
    }

    private void followPointer() {
        if (held == null && doober == null) {
            return;
        }
        com.badlogic.gdx.math.Vector2 at = stage.screenToStageCoordinates(
                new com.badlogic.gdx.math.Vector2(com.badlogic.gdx.Gdx.input.getX(),
                        com.badlogic.gdx.Gdx.input.getY()));
        if (held != null) {
            held.setPosition(at.x - TOOL / 2f, at.y - TOOL / 2f);
            held.toFront();
        }
        if (doober != null) {
            doober.setBounds(at.x - TOOL / 2f, at.y - TOOL / 2f, TOOL, TOOL);
            doober.setVisible(carryingSprout);
            doober.toFront();
        }
    }

    private void syncShimmer() {
        boolean holding = garden.holdingCan();
        for (PotSlot slot : slots) {
            slot.setShimmering(holding && slot.pot().isOccupied() && !slot.pot().isReady());
        }
    }

    private void refreshCounts() {
        if (sproutCount != null) {
            sproutCount.setText(String.valueOf(garden.sprouts()));
        }
    }

    @Override
    public void render(float delta) {
        followPointer();
        advanceWatering(delta);
        super.render(delta);
    }

    @Override
    protected void refresh() {
        refreshCounts();
        syncShimmer();
        awardRipened();
        restartSpentPlants();
    }

    private void restartSpentPlants() {
        for (PotSlot slot : slots) {
            Pot pot = slot.pot();
            if (ripened.contains(pot) && garden.boostSpent(pot)) {
                garden.regrow(pot);
                ripened.remove(pot);
            }
        }
    }

    private void awardRipened() {
        for (PotSlot slot : slots) {
            Pot pot = slot.pot();
            if (pot.isOccupied() && !pot.isMarigold() && pot.isReady()
                    && !ripened.contains(pot)) {
                ripened.add(pot);
                garden.ripen(pot);
                toasts.success(pot.getPlantType().getName() + " is fully grown and boosted.");
            }
        }
    }
}

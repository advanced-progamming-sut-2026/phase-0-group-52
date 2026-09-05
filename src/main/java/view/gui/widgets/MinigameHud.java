package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.MinigameRunController;
import model.entities.plants.Plants;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;
import model.entities.zombies.Zombies;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.List;
import java.util.Map;

public final class MinigameHud extends Table {

    public interface Sink {
        void armedPlant(Plants plant);

        void armedZombie(Zombies zombie);

        void quit();
    }

    private static final float PAD = 10f;
    private static final float PACKET_SCALE = 0.72f;
    private static final float COUNTER_HEIGHT = 42f;
    private static final float COUNTER_WIDTH = 132f;
    private static final float SHOP_ICON = 54f;
    private static final int PACKET_COLUMNS = 2;

    private final GameContext context;
    private final UiKit ui;
    private final Assets assets;
    private final MinigameRunController controller;
    private final Sink sink;

    private final Table slots = new Table();
    private final Label sunLabel;
    private final Label goal;

    private Plants armedPlant;
    private Zombies armedZombie;
    private int builtSignature = -1;

    public MinigameHud(GameContext context, MinigameRunController controller, Sink sink) {
        this.context = context;
        this.ui = context.ui();
        this.assets = context.assets();
        this.controller = controller;
        this.sink = sink;
        this.sunLabel = new Label("0", ui.skin(), "titleOnDark");
        this.goal = new Label("", ui.skin(), "titleOnDark");
        this.goal.setColor(Theme.SUN);
        this.goal.setAlignment(Align.center);
        setFillParent(true);
        top();
        add(topRow()).growX().pad(PAD).row();
        add(slots).left().top().pad(PAD).row();
        add().expandY().row();
        add(bottomRow()).growX().pad(PAD);
        rebuild();
    }

    public void clearArmed() {
        armedPlant = null;
        armedZombie = null;
        rebuild();
    }

    private Table topRow() {
        Table row = new Table();
        row.left();
        row.add(counter("IMAGE_UI_HUD_INGAME_SUN", sunLabel)).left();
        row.add().expandX();
        row.add(ui.faceButton("Quit", "secondary", new Runnable() {
            @Override
            public void run() {
                sink.quit();
            }
        })).right();
        return row;
    }

    private Table bottomRow() {
        Table row = new Table();
        row.bottom();
        row.add(goal).expandX().center().bottom();
        return row;
    }

    private Table counter(String iconId, Label label) {
        Table cell = new Table();
        cell.setBackground(ui.drawable("counter"));
        cell.left().pad(4f);
        if (assets != null && assets.region(iconId) != null) {
            Image mark = new Image(assets.region(iconId));
            mark.setScaling(Scaling.fit);
            cell.add(mark).size(COUNTER_HEIGHT * 0.8f).padRight(4f);
        }
        cell.add(label).growX().left();
        Table holder = new Table();
        holder.add(cell).size(COUNTER_WIDTH, COUNTER_HEIGHT);
        return holder;
    }

    public void refresh() {
        sunLabel.setText(String.valueOf(controller.sun()));
        goal.setText(controller.objectiveTag());
        int signature = signature();
        if (signature != builtSignature) {
            builtSignature = signature;
            rebuild();
        }
        for (Actor actor : slots.getChildren()) {
            if (actor instanceof SeedPacket) {
                SeedPacket packet = (SeedPacket) actor;
                Plants plant = packet.getPlant();
                packet.setAffordable(controller.canAfford(plant));
                packet.setRecharge(controller.isOnCooldown(plant)
                        ? (float) rechargeFraction(plant) : 0f);
            }
        }
    }

    private int signature() {
        if (controller.isBowling()) {
            return 1000 + controller.bowlingBelt().size();
        }
        if (controller.isIZombie()) {
            int cooling = 0;
            for (Zombies type : controller.zombieShop().keySet()) {
                cooling = cooling * 3 + (int) Math.ceil(
                        controller.zombieRechargeFraction(type) * 4f);
            }
            return 2000 + cooling;
        }
        if (controller.isVasebreaker()) {
            return 3000 + (controller.held() == null ? 0 : controller.held().ordinal() + 1);
        }
        return 4000 + controller.bank().size();
    }

    private double rechargeFraction(Plants plant) {
        double left = controller.cooldownLeft(plant);
        double total = Math.max(0.001, plant.getRecharge());
        return Math.max(0d, Math.min(1d, left / total));
    }

    private void rebuild() {
        slots.clearChildren();
        slots.top().left();
        if (controller.isIZombie()) {
            buildZombieShop();
            return;
        }
        if (controller.isBowling()) {
            buildBelt();
            return;
        }
        if (controller.isVasebreaker()) {
            buildHeld();
            return;
        }
        buildSeedBank();
    }

    private void buildSeedBank() {
        int column = 0;
        for (final Plants plant : controller.bank()) {
            SeedPacket packet = new SeedPacket(ui, assets, plant,
                    SeedPacket.Mode.GAME, PACKET_SCALE);
            packet.setSelected(plant == armedPlant);
            packet.setAffordable(controller.canAfford(plant));
            packet.onClick(new Runnable() {
                @Override
                public void run() {
                    if (controller.isOnCooldown(plant) || !controller.canAfford(plant)) {
                        return;
                    }
                    armedPlant = plant == armedPlant ? null : plant;
                    sink.armedPlant(armedPlant);
                    rebuild();
                }
            });
            slots.add(packet).size(SeedPacket.ART_W * PACKET_SCALE,
                    SeedPacket.ART_H * PACKET_SCALE).pad(3f);
            if (++column % PACKET_COLUMNS == 0) {
                slots.row();
            }
        }
    }

    private void buildBelt() {
        List<Plants> belt = controller.bowlingBelt();
        int column = 0;
        for (Plants nut : belt) {
            SeedPacket packet = new SeedPacket(ui, assets, nut,
                    SeedPacket.Mode.GAME, PACKET_SCALE);
            packet.setAffordable(true);
            packet.setFree(true);
            packet.setSelected(column == 0);
            slots.add(packet).size(SeedPacket.ART_W * PACKET_SCALE,
                    SeedPacket.ART_H * PACKET_SCALE).pad(3f);
            if (++column % PACKET_COLUMNS == 0) {
                slots.row();
            }
        }
        if (belt.isEmpty()) {
            slots.add(ui.muted("Waiting for a nut...")).pad(Theme.PAD_SMALL);
        }
    }

    private void buildHeld() {
        Plants held = controller.held();
        if (held == null) {
            slots.add(ui.muted("Click a vase to break it.")).pad(Theme.PAD_SMALL);
            return;
        }
        SeedPacket packet = new SeedPacket(ui, assets, held,
                SeedPacket.Mode.GAME, PACKET_SCALE);
        packet.setAffordable(true);
        packet.setFree(true);
        packet.setSelected(true);
        slots.add(packet).size(SeedPacket.ART_W * PACKET_SCALE,
                SeedPacket.ART_H * PACKET_SCALE).pad(3f);
    }

    private void buildZombieShop() {
        int column = 0;
        for (Map.Entry<Zombies, Integer> entry : controller.zombieShop().entrySet()) {
            final Zombies type = entry.getKey();
            final int price = entry.getValue().intValue();
            slots.add(shopCell(type, price)).pad(3f);
            if (++column % PACKET_COLUMNS == 0) {
                slots.row();
            }
        }
    }

    private Table shopCell(final Zombies type, final int price) {
        Table cell = new Table();
        boolean affordable = controller.sun() >= price
                && controller.zombieRecharge(type) <= 0d;
        cell.setBackground(ui.primitives().rounded(8,
                Theme.alpha(type == armedZombie ? Theme.GREEN_LIGHT : Theme.PANEL_SUNKEN,
                        affordable ? 0.92f : 0.5f),
                type == armedZombie ? Theme.SUN : Theme.OUTLINE, 3));
        Stack art = new Stack();
        ZombieRecord record = ZombieData.of(type);
        if (record != null && assets != null
                && assets.region(record.getPacketIcon()) != null) {
            Image icon = new Image(assets.region(record.getPacketIcon()));
            icon.setScaling(Scaling.fit);
            Table holder = new Table();
            holder.add(icon).size(SHOP_ICON);
            art.add(holder);
        }
        float cooling = controller.zombieRechargeFraction(type);
        if (cooling > 0f) {
            Table shade = new Table();
            shade.bottom();
            Table fill = new Table();
            fill.setBackground(ui.primitives().flat(
                    Theme.alpha(com.badlogic.gdx.graphics.Color.BLACK, 0.55f)));
            shade.add(fill).growX().height(SHOP_ICON * cooling);
            art.add(shade);
        }
        cell.add(art).size(SHOP_ICON).row();
        Label cost = new Label(String.valueOf(price), ui.skin(), "packetCost");
        cost.setAlignment(Align.center);
        cost.setColor(affordable ? Theme.SUN : Theme.RED_LIGHT);
        cell.add(cost).growX().padBottom(2f);
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                if (controller.sun() < price) {
                    context.toasts().error("Not enough sun.");
                    return;
                }
                armedZombie = type == armedZombie ? null : type;
                sink.armedZombie(armedZombie);
                rebuild();
            }
        });
        return cell;
    }
}

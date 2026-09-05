package view.gui.widgets;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.LevelController;
import model.User;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.List;

public final class LevelHud extends Table {

    public interface Sink {
        void armed(Plants plant);

        void shovel(boolean on);

        void feeding(boolean on);

        void paused();

        void nuked();
    }

    private static final float PACKET_SCALE = 1.05f;
    private static final int PACKET_COLUMNS = 2;
    private static final float PAD = 8f;
    private static final float ICON = 74f;
    private static final float COUNTER_WIDTH = 176f;
    private static final float COUNTER_HEIGHT = 58f;
    private static final float NUKE = 104f;
    private static final float METER_WIDTH = 360f;
    private static final float SUN_GRANT = 100f;
    private static final float TITLE_SCALE = 1.5f;
    private static final float GOAL_SCALE = 0.72f;
    private static final int BELT_SLOTS = 10;
    private static final double LOW_TIME = 10d;
    private static final String NUKE_ICON =
            "IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBIEHEAD";
    private static final float NUKE_CANVAS = 390f;
    private static final float NUKE_COVERAGE = 1.15f;
    private static final float NUKE_ICON_FIT = 0.6f;
    private static final float NUKE_ICON_PAD = 0.16f;
    private static final String NUKE_PAM =
            "768/FULL/EFFECTS/ZOMBIE_BIGHEAD_SHOCK/ZOMBIE_BIGHEAD_SHOCK.PAM";

    private final GameContext context;
    private final UiKit ui;
    private final Assets assets;
    private final LevelController controller;
    private final Sink sink;

    private final Table packets = new Table();
    private final PlantFoodBank foodBank;
    private final WaveMeter meter;
    private final Label sunLabel;
    private final Label coinLabel;
    private final Label gemLabel;
    private final Label title;
    private final Label goal;
    private final Label extra;
    private int armedSlot = -1;
    private int beltSize = -1;

    private Plants armed;
    private boolean shovelling;
    private boolean feeding;
    private Image headIcon;
    private boolean builtCheating;

    public LevelHud(GameContext context, LevelController controller, Sink sink) {
        this.context = context;
        this.ui = context.ui();
        this.assets = context.assets();
        this.controller = controller;
        this.sink = sink;
        this.foodBank = new PlantFoodBank(assets, controller.plantFoodSlots());
        this.meter = new WaveMeter(assets);
        this.sunLabel = new Label("0", ui.skin(), "packetCost");
        this.coinLabel = new Label("0", ui.skin(), "onDark");
        this.gemLabel = new Label("0", ui.skin(), "onDark");
        this.extra = new Label("", ui.skin(), "titleOnDark");
        this.extra.setAlignment(Align.center);
        this.extra.setColor(Theme.SUN);
        this.goal = new Label("", ui.skin(), "titleOnDark");
        this.goal.setAlignment(Align.center);
        this.goal.setFontScale(GOAL_SCALE);
        this.goal.setColor(Theme.SUN);
        this.title = new Label("", ui.skin(), "titleOnDark");
        this.title.setFontScale(TITLE_SCALE);
        setFillParent(true);
        builtCheating = cheating();
        build();
    }

    private void build() {
        top().left();
        add(topRow()).growX().pad(PAD).row();
        add(leftColumn()).left().top().pad(PAD).row();
        add().expandY().row();
        add(bottomRow()).growX().pad(PAD);
    }

    private Table leftColumn() {
        Table column = new Table();
        column.top().left();
        column.add(packets).left().top();
        return column;
    }

    private Table topRow() {
        Table row = new Table();
        row.left();
        row.add(counter("IMAGE_UI_HUD_INGAME_SUN", sunLabel, new Runnable() {
            @Override
            public void run() {
                controller.grantSun((int) SUN_GRANT);
            }
        })).left();
        row.add(foodCell()).left().padLeft(PAD);
        row.add().expandX();
        row.add(walletCell("coinIcon", coinLabel, true)).right().padRight(PAD);
        row.add(walletCell("gemIcon", gemLabel, false)).right().padRight(PAD);
        row.add(iconButton("IMAGE_UI_HUD_INGAME_PAUSE_BUTTON", new Runnable() {
            @Override
            public void run() {
                sink.paused();
            }
        })).size(ICON).right();
        return row;
    }

    private Table foodCell() {
        Table cell = new Table();
        UiKit.onClick(foodBank, new Runnable() {
            @Override
            public void run() {
                feeding = !feeding;
                sink.feeding(feeding);
            }
        });
        cell.add(foodBank).size(COUNTER_WIDTH * 0.85f, COUNTER_HEIGHT);
        if (cheating()) {
            cell.add(plus(new Runnable() {
                @Override
                public void run() {
                    controller.grantPlantFood(1);
                }
            })).size(ICON * 0.5f).padLeft(2f);
        }
        return cell;
    }

    private Table counter(String iconId, Label label, final Runnable grant) {
        Table cell = new Table();
        TextureRegion bar = assets == null ? null
                : assets.region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
        if (bar != null) {
            cell.setBackground(new TextureRegionDrawable(bar));
        }
        TextureRegion icon = assets == null ? null : assets.region(iconId);
        if (icon != null) {
            Image mark = new Image(new TextureRegionDrawable(icon));
            mark.setScaling(Scaling.fit);
            cell.add(mark).size(COUNTER_HEIGHT * 0.8f).padRight(4f);
        }
        label.setAlignment(Align.left);
        cell.add(label).growX().left();
        if (cheating()) {
            cell.add(plus(grant)).size(ICON * 0.5f).padLeft(2f);
        }
        cell.pad(4f);
        return cell;
    }

    private Table walletCell(String iconName, Label amount, final boolean coins) {
        Table plusSlot = new Table();
        Table face = view.gui.TopBar.walletFace(context, plusSlot, iconName, amount,
                cheating());
        if (cheating()) {
            UiKit.onClick(plusSlot, new Runnable() {
                @Override
                public void run() {
                    wallet(coins);
                }
            });
        }
        return face;
    }

    private Table plus(Runnable action) {
        return iconButton("IMAGE_UI_HUD_INGAME_COIN_BUY", action);
    }

    private Table iconButton(String id, Runnable action) {
        Table cell = new Table();
        TextureRegion art = assets == null ? null : assets.region(id);
        if (art != null) {
            Image mark = new Image(new TextureRegionDrawable(art));
            mark.setScaling(Scaling.fit);
            cell.add(mark).grow();
            new AlmanacControls(ui, assets).hoverTint(cell, mark);
        }
        UiKit.onClick(cell, action);
        view.gui.Animations.attachPress(cell);
        return cell;
    }

    private Table shovelCell() {
        Table cell = iconButton("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON", new Runnable() {
            @Override
            public void run() {
                shovelling = !shovelling;
                sink.shovel(shovelling);
            }
        });
        cell.setName("shovel");
        Table holder = new Table();
        holder.left();
        holder.add(cell).size(ICON * 1.4f);
        return holder;
    }

    private Table bottomRow() {
        Table row = new Table();
        row.bottom();
        Table corner = new Table();
        corner.add(shovelCell()).left().row();
        if (cheating()) {
            corner.add(nukeCell()).left().padTop(PAD);
        }
        row.add(corner).left().bottom();
        title.setAlignment(Align.center);
        Table centre = new Table();
        centre.add(extra).center().padBottom(2f).row();
        centre.add(goal).center().padBottom(2f).row();
        centre.add(title).center();
        row.add(centre).expandX().center().bottom();
        row.add(meter).size(METER_WIDTH, COUNTER_HEIGHT).right().bottom()
                .padRight(PAD * 2f);
        return row;
    }

    private com.badlogic.gdx.scenes.scene2d.ui.Stack nukeArt(PamActor shock) {
        com.badlogic.gdx.scenes.scene2d.ui.Stack art =
                new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        TextureRegion still = assets == null ? null : assets.region(NUKE_ICON);
        if (still != null) {
            headIcon = new Image(new TextureRegionDrawable(still));
            headIcon.setScaling(Scaling.fit);
            Table holder = new Table();
            holder.add(headIcon).grow();
            art.add(holder);
        }
        if (shock.isReady()) {
            Table burst = new Table();
            burst.add(shock).grow();
            art.add(burst);
        }
        return art;
    }

    private Table nukeCell() {
        final PamActor shock = PlantStage.anchored(assets, NUKE_PAM, "animation",
                NUKE_CANVAS, NUKE_CANVAS);
        shock.setClipped(true).setCoverage(NUKE_COVERAGE);
        if (shock.isReady()) {
            shock.freeze();
            shock.setVisible(false);
        }
        com.badlogic.gdx.scenes.scene2d.ui.Button face =
                new com.badlogic.gdx.scenes.scene2d.ui.Button(
                        ui.skin().get("epic",
                                com.badlogic.gdx.scenes.scene2d.ui.TextButton
                                        .TextButtonStyle.class));
        final com.badlogic.gdx.scenes.scene2d.ui.Stack flash = nukeArt(shock);
        com.badlogic.gdx.scenes.scene2d.ui.Stack art = flash;
        face.add(art).size(NUKE * NUKE_ICON_FIT).pad(NUKE * NUKE_ICON_PAD);
        UiKit.onClick(face, new Runnable() {
            @Override
            public void run() {
                if (shock.isReady()) {
                    shock.setVisible(true);
                    if (headIcon != null) {
                        headIcon.setVisible(false);
                    }
                    shock.play("animation", false, new Runnable() {
                        @Override
                        public void run() {
                            shock.setVisible(false);
                            shock.freeze();
                            if (headIcon != null) {
                                headIcon.setVisible(true);
                            }
                        }
                    });
                    flash.toFront();
                }
                controller.nuke();
                sink.nuked();
            }
        });
        view.gui.Animations.attachPress(face);
        Table holder = new Table();
        holder.add(face).size(NUKE, NUKE);
        return holder;
    }

    public void setArmed(Plants plant) {
        armed = plant;
        rebuildPackets();
    }

    public void setShovelling(boolean value) {
        shovelling = value;
    }

    public void setFeeding(boolean value) {
        feeding = value;
    }

    public void rebuildPackets() {
        packets.clearChildren();
        if (controller.isConveyor() || controller.isBossFight()) {
            rebuildBelt();
            return;
        }
        List<Plants> bank = controller.bank();
        int column = 0;
        for (final Plants plant : bank) {
            SeedPacket packet = new SeedPacket(ui, assets, plant,
                    SeedPacket.Mode.GAME, PACKET_SCALE);
            packet.setSelected(plant == armed);
            packet.setBoosted(controller.isBoosted(plant));
            packet.setAffordable(controller.canAfford(plant));
            packet.setRecharge(controller.isOnCooldown(plant)
                    ? (float) rechargeFraction(plant) : 0f);
            packet.setCooldown((float) controller.cooldownLeft(plant));
            packet.onClick(new Runnable() {
                @Override
                public void run() {
                    if (controller.isOnCooldown(plant) || !controller.canAfford(plant)) {
                        return;
                    }
                    sink.armed(plant == armed ? null : plant);
                }
            });
            packets.add(packet).size(SeedPacket.ART_W * PACKET_SCALE,
                    SeedPacket.ART_H * PACKET_SCALE).pad(3f);
            if (++column % PACKET_COLUMNS == 0) {
                packets.row();
            }
        }
    }

    private void rebuildBelt() {
        List<Plants> belt = controller.belt();
        int slot = 0;
        for (final Plants plant : belt) {
            if (slot >= BELT_SLOTS) {
                break;
            }
            final int index = slot;
            SeedPacket packet = new SeedPacket(ui, assets, plant,
                    SeedPacket.Mode.GAME, PACKET_SCALE);
            packet.setSelected(plant == armed && index == armedSlot);
            packet.setAffordable(true);
            packet.setFree(true);
            packet.onClick(new Runnable() {
                @Override
                public void run() {
                    boolean same = plant == armed && index == armedSlot;
                    armedSlot = same ? -1 : index;
                    sink.armed(same ? null : plant);
                }
            });
            packets.add(packet).size(SeedPacket.ART_W * PACKET_SCALE,
                    SeedPacket.ART_H * PACKET_SCALE).pad(3f);
            if (++slot % PACKET_COLUMNS == 0) {
                packets.row();
            }
        }
        if (belt.isEmpty()) {
            packets.add(ui.muted("The belt is empty...")).pad(PAD);
        }
    }

    private double rechargeFraction(Plants plant) {
        double left = controller.cooldownLeft(plant);
        double total = Math.max(0.001, plant.getRecharge());
        return Math.max(0d, Math.min(1d, left / total));
    }

    public void refresh() {
        if (cheating() != builtCheating) {
            builtCheating = cheating();
            clearChildren();
            build();
            rebuildPackets();
        }
        sunLabel.setText(String.valueOf(controller.sun()));
        foodBank.setFilled(controller.plantFood());
        if (controller.isBossFight()) {
            meter.setBoss(view.gui.widgets.WaveMeter.BOSS_SEGMENTS,
                    controller.bossHealth(), controller.bossStunned());
        } else {
            meter.set(controller.waveCount(), controller.threatProgress());
        }
        User user = context.user();
        coinLabel.setText(user == null ? "0" : String.valueOf(user.getCoins()));
        gemLabel.setText(user == null ? "0" : String.valueOf(user.getGems()));
        title.setText(caption());
        goal.setText(controller.objectiveTag());
        extra.setText(sideNote());
        if ((controller.isConveyor() || controller.isBossFight())
                && controller.belt().size() != beltSize) {
            beltSize = controller.belt().size();
            rebuildPackets();
        }
        for (Actor actor : packets.getChildren()) {
            if (actor instanceof SeedPacket) {
                SeedPacket packet = (SeedPacket) actor;
                Plants plant = packet.getPlant();
                packet.setAffordable(controller.canAfford(plant));
                packet.setRecharge(controller.isOnCooldown(plant)
                        ? (float) rechargeFraction(plant) : 0f);
                packet.setCooldown((float) controller.cooldownLeft(plant));
            }
        }
    }

    private String caption() {
        String chapter = controller.chapter() == null ? ""
                : controller.chapter().getDisplayName();
        String special = context.app().getPendingSpecial();
        String base = chapter + " - Day " + controller.levelNumber();
        return special == null ? base : base + ": " + AlmanacControls.pretty(special);
    }

    private String sideNote() {
        double left = controller.secondsLeft();
        if (left >= 0d) {
            extra.setColor(left <= LOW_TIME ? Theme.RED_LIGHT : Theme.SUN);
            return String.format("%d:%02d", (int) left / 60, (int) left % 60);
        }
        int lost = controller.plantsLost();
        if (lost >= 0) {
            int cap = controller.plantsAllowedToLose();
            extra.setColor(lost >= cap - 1 ? Theme.RED_LIGHT : Theme.SUN);
            return "Plants lost  " + lost + " / " + cap;
        }
        return "";
    }

    private boolean cheating() {
        return context.settings() != null && context.settings().isDebugMode();
    }

    private void wallet(boolean coins) {
        new controller.menu.ChapterMenuController(context.app()).handleCommand(
                new String[]{"menu", "cheat", "add", coins ? "500" : "50",
                        coins ? "coin" : "diamond"});
    }

    @Override
    public float getPrefWidth() {
        return Theme.WORLD_WIDTH;
    }
}

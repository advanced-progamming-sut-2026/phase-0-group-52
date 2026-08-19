package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.GreenhouseMenuController;
import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.PvzGame;
import view.gui.Theme;
import view.gui.UiKit;

public final class GreenhouseScreen extends BaseScreen {
    private final GreenhouseMenuController controller;
    private Table grid;

    public GreenhouseScreen(GameContext context) {
        super(context, "Greenhouse");
        this.controller = new GreenhouseMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Your garden", ui.skin(), "title")).left().expandX();
        header.add(ui.styledButton("Shop", "info", new Runnable() {
            @Override
            public void run() {
                ((PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener()).showShop();
            }
        })).padRight(Theme.PAD_SMALL);
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand("menu enter main");
            }
        })).right();
        panel.add(header).growX().padBottom(Theme.PAD_SMALL).row();

        Label legend = new Label(
                "Marigolds pay coins when harvested. Other plants bank a boost for your next level.",
                ui.skin(), "muted");
        panel.add(legend).left().padBottom(Theme.PAD).row();

        grid = new Table();
        grid.defaults().pad(Theme.PAD_SMALL);
        panel.add(grid).grow();

        rebuildGrid();
        content.add(panel).center();
    }

    private void rebuildGrid() {
        grid.clear();
        Greenhouse greenhouse = context.app().getGreenhouse();

        for (int y = 1; y <= Greenhouse.ROWS; y++) {
            for (int x = 1; x <= Greenhouse.COLS; x++) {
                grid.add(potCard(greenhouse.getPot(x, y), x, y)).width(150f).height(120f);
            }
            grid.row();
        }
    }

    private Table potCard(final Pot pot, final int x, final int y) {
        Table card = new Table();
        card.top();

        if (pot == null) {
            card.setBackground(ui.primitives().rounded(8, Theme.PANEL_SUNKEN, Theme.OUTLINE, 2));
            return card;
        }

        final boolean unlocked = pot.isUnlocked();
        final boolean occupied = pot.isOccupied();
        final boolean ready = occupied && pot.isReady();

        card.setBackground(ui.primitives().rounded(8,
                unlocked ? Theme.PANEL : Theme.alpha(Theme.LOCKED, 0.55f),
                ready ? Theme.SUN : Theme.OUTLINE, ready ? 4 : 2));
        card.pad(Theme.PAD_SMALL);

        if (!unlocked) {
            fillLocked(card);
            return card;
        }
        if (!occupied) {
            fillEmpty(card, x, y);
            return card;
        }
        fillGrowing(card, pot, ready, x, y);
        return card;
    }

    private void fillLocked(Table card) {
        Label locked = new Label("Locked", ui.skin(), "smallOnDark");
        locked.setAlignment(Align.center);
        card.add(locked).expand().center().row();
        Label price = new Label(model.shop.Shop.POT_PRICE + " coins", ui.skin(), "smallOnDark");
        price.setAlignment(Align.center);
        card.add(price).padBottom(Theme.PAD_SMALL);

        Animations.attachPress(card);
        UiKit.onClick(card, new Runnable() {
            @Override
            public void run() {
                context.toasts().info("Buy pots from the shop.");
            }
        });
    }

    private void fillEmpty(Table card, final int x, final int y) {
        Label empty = new Label("Empty", ui.skin(), "muted");
        empty.setAlignment(Align.center);
        card.add(empty).expand().center().row();
        card.add(ui.button("Plant", new Runnable() {
            @Override
            public void run() {
                send("plant pot at (" + x + ", " + y + ")");
            }
        })).padBottom(Theme.PAD_SMALL);
    }

    private void fillGrowing(Table card, Pot pot, boolean ready, final int x, final int y) {
        String name = (pot.getPlantType() == null) ? "Sprout" : pot.getPlantType().getName();
        card.add(ui.token(38, pot.isMarigold() ? Theme.COIN
                : Theme.plantFamily(pot.getPlantType() == null
                        ? "SUN_PRODUCER" : pot.getPlantType().getCategory().name())))
                .size(38f).padTop(4f).row();

        Label label = new Label(name, ui.skin(), "small");
        label.setAlignment(Align.center);
        label.setWrap(true);
        card.add(label).width(130f).row();

        if (ready) {
            card.add(ui.button("Harvest", new Runnable() {
                @Override
                public void run() {
                    send("collect (" + x + ", " + y + ")");
                }
            })).padBottom(Theme.PAD_SMALL);
        } else {
            Label growing = new Label("Growing", ui.skin(), "muted");
            growing.setAlignment(Align.center);
            card.add(growing).row();
            card.add(ui.styledButton("Speed up", "info", new Runnable() {
                @Override
                public void run() {
                    send("grow (" + x + ", " + y + ")");
                }
            })).padBottom(Theme.PAD_SMALL);
        }
    }

    private void send(String command) {
        controller.handleCommand(command);
        rebuildGrid();
    }

    @Override
    public void show() {
        super.show();
        if (grid != null) {
            rebuildGrid();
        }
    }
}

package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import controller.menu.GreenhouseMenuController;
import model.entities.plants.Plants;
import model.shop.Shop;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.PvzGame;
import view.gui.Theme;

/**
 * The shop, reached from the greenhouse as in the console build.
 *
 * <p>Only one daily offer exists, so the specification allows a single list rather
 * than separate permanent and daily sections; the daily item is simply marked. Each
 * purchase asks for confirmation first, then issues the documented
 * {@code shop buy} command so pricing and limits stay in {@link Shop}.
 */
public final class ShopScreen extends BaseScreen {

    /** Item identifiers accepted by {@code shop buy -i}. */
    private static final int ITEM_POT = 1;
    private static final int ITEM_PLANT_FOOD = 2;
    private static final int ITEM_RANDOM_SEEDS = 3;
    private static final int ITEM_CHOICE_SEEDS = 4;
    private static final int ITEM_EXCHANGE = 5;
    private static final int ITEM_DAILY = 6;

    private final GreenhouseMenuController controller;
    private SelectBox<String> plantPicker;

    public ShopScreen(GameContext context) {
        super(context, "Shop");
        this.controller = new GreenhouseMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Store", ui.skin(), "title")).left().expandX();
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                ((PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener()).resumeRouting();
            }
        })).right();
        panel.add(header).growX().padBottom(Theme.PAD).row();

        Table list = new Table();
        list.top();
        list.defaults().growX().pad(Theme.PAD_SMALL);

        list.add(item("Flower pot", "Unlocks one greenhouse slot.",
                Shop.POT_PRICE, "coins", Theme.plantFamily("WALL_NUT"), ITEM_POT, false)).row();
        list.add(item("Plant food", "Instantly supercharges one plant. Max "
                        + Shop.PLANT_FOOD_MAX + " held.",
                Shop.PLANT_FOOD_PRICE, "gems", Theme.PLANT_FOOD, ITEM_PLANT_FOOD, false)).row();
        list.add(item("Random seed packets", Shop.RANDOM_SEEDS_COUNT + " packets, chosen at random.",
                Shop.RANDOM_SEEDS_PRICE, "coins", Theme.SUN_DEEP, ITEM_RANDOM_SEEDS, false)).row();
        list.add(item("Chosen seed packets", Shop.CHOICE_SEEDS_COUNT + " packets for a plant you pick.",
                Shop.CHOICE_SEEDS_PRICE, "gems", Theme.GREEN, ITEM_CHOICE_SEEDS, true)).row();
        list.add(item("Currency exchange", Shop.EXCHANGE_DIAMONDS + " gems become "
                        + Shop.EXCHANGE_COINS + " coins.",
                Shop.EXCHANGE_DIAMONDS, "gems", Theme.GEM, ITEM_EXCHANGE, false)).row();
        list.add(item("Daily offer", Shop.DAILY_COUNT + " packets, discounted from "
                        + Shop.DAILY_BASE_PRICE + ". Once per day.",
                Shop.DAILY_PRICE, "coins", Theme.COIN, ITEM_DAILY, false)).row();

        ScrollPane scroll = new ScrollPane(list, ui.skin());
        scroll.setFadeScrollBars(false);
        panel.add(scroll).grow();

        content.add(panel).width(820f).height(480f).center();
    }

    /**
     * One shop row. {@code needsPlant} adds the plant picker the chosen-seed packet
     * requires.
     */
    private Table item(final String name, String description, final int price,
            final String currency, Color accent, final int itemId, final boolean needsPlant) {

        Table row = ui.sunken();
        row.left();
        row.add(ui.token(40, accent)).size(40f).padRight(Theme.PAD);

        Table text = new Table();
        text.left();
        text.add(new Label(name, ui.skin(), "default")).left().row();
        Label detail = new Label(description, ui.skin(), "muted");
        detail.setWrap(true);
        detail.setAlignment(Align.left);
        text.add(detail).width(360f).left();
        row.add(text).growX();

        if (needsPlant) {
            plantPicker = new SelectBox<String>(ui.skin());
            String[] names = new String[Plants.values().length];
            for (int i = 0; i < Plants.values().length; i++) {
                names[i] = Plants.values()[i].getName();
            }
            plantPicker.setItems(names);
            row.add(plantPicker).width(170f).padRight(Theme.PAD);
        }

        Label cost = new Label(price + " " + currency, ui.skin(), "value");
        row.add(cost).right().padRight(Theme.PAD).width(110f);

        row.add(ui.button("Buy", new Runnable() {
            @Override
            public void run() {
                confirm(name, price, currency, itemId, needsPlant);
            }
        })).right();
        return row;
    }

    /** The specification asks for confirmation before a purchase completes. */
    private void confirm(String name, int price, String currency,
            final int itemId, final boolean needsPlant) {

        final String plantName = (needsPlant && plantPicker != null)
                ? plantPicker.getSelected() : null;

        Window.WindowStyle style = new Window.WindowStyle();
        style.titleFont = ui.skin().getFont("title");
        style.titleFontColor = Theme.TEXT;
        style.background = ui.primitives().rounded(Theme.RADIUS, Theme.PANEL,
                Theme.OUTLINE, Theme.BORDER);

        Dialog dialog = new Dialog("", style) {
            @Override
            protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {
                    purchase(itemId, plantName);
                }
            }
        };
        dialog.pad(Theme.PAD_LARGE);

        String question = "Buy " + name + " for " + price + " " + currency + "?";
        if (plantName != null) {
            question += "\nPlant: " + plantName;
        }
        Label message = new Label(question, ui.skin(), "default");
        message.setAlignment(Align.center);
        dialog.getContentTable().add(message).pad(Theme.PAD).row();

        dialog.getButtonTable().add(ui.button("Confirm", null)).padRight(Theme.PAD);
        dialog.getButtonTable().add(ui.secondaryButton("Cancel", null));
        dialog.setObject(dialog.getButtonTable().getChildren().get(0), Boolean.TRUE);
        dialog.setObject(dialog.getButtonTable().getChildren().get(1), Boolean.FALSE);

        dialog.show(stage);
    }

    /**
     * Issues the purchase. {@link Shop} decides whether it is allowed and prints
     * the outcome, which surfaces as a toast.
     */
    private void purchase(int itemId, String plantName) {
        StringBuilder command = new StringBuilder("shop buy -i ")
                .append(itemId).append(" -n 1");
        if (plantName != null) {
            command.append(" -t ").append(plantName.replace(' ', '_'));
        }
        controller.handleCommand(command.toString());
    }
}

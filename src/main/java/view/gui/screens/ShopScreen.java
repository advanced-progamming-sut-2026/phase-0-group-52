package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import model.App;
import model.User;
import model.entities.plants.Plants;
import model.shop.Shop;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.widgets.ShopCard;

public final class ShopScreen extends BaseScreen {

    private final controller.menu.GreenhouseController garden;

    private static final int COLUMNS = 3;
    private static final String PACKET_ICON = "IMAGE_UI_STOREMULTI_SEEDPACKETICON";
    private static final String POT_ICON =
            "IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161";
    private static final String FOOD_ICON = "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON";

    private final Shop shop;

    private Table shelf;

    public ShopScreen(GameContext context) {
        super(context, "Shop");
        this.garden = new controller.menu.GreenhouseController(context.app());
        this.shop = context.app().getShop();
    }

    @Override
    protected view.gui.TopBar.Section section() {
        return view.gui.TopBar.Section.SHOP;
    }

    @Override
    protected String backdropRegion() {
        return "IMAGE_UI_THYMED_EVENTS_COINS_SPREE_EVENT_BG";
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected void build() {
        shelf = new Table();
        shelf.defaults().pad(Theme.PAD_SMALL);
        stock();
        content.add(shelf).expand().center();
    }

    private void stock() {
        shelf.clear();
        int column = 0;
        if (!garden.gardenIsFull()) {
            column = shelfItem(column, "Flower Pot", "Adds a pot to your Zen Garden.",
                    "coinIcon", Shop.POT_PRICE, POT_ICON, false, 1, null);
        }
        column = shelfItem(column, "Plant Food",
                "Up to " + Shop.PLANT_FOOD_MAX + " stored.",
                "gemIcon", Shop.PLANT_FOOD_PRICE, FOOD_ICON, false, 2, null);
        column = shelfItem(column, "Random Seeds",
                Shop.RANDOM_SEEDS_COUNT + " packets for a plant you own.",
                "coinIcon", Shop.RANDOM_SEEDS_PRICE, PACKET_ICON, false, 3, null);
        ShopCard chosen = new ShopCard(ui, context.assets(), "Chosen Seeds",
                Shop.CHOICE_SEEDS_COUNT + " packets for a plant you pick.", PACKET_ICON,
                "gemIcon", Shop.CHOICE_SEEDS_PRICE, null, 0, false, new Runnable() {
                    @Override
                    public void run() {
                        pickPlant();
                    }
                });
        shelf.add(chosen).size(ShopCard.WIDTH, ShopCard.HEIGHT);
        column = column + 1;
        if (column % COLUMNS == 0) {
            shelf.row();
        }
        column = trade(column);
        final Plants offer = shop.dailyPlant(context.app().getLoggedInUser());
        if (offer != null) {
            ShopCard card = new ShopCard(ui, context.assets(), offer.getName(),
                    "Unlocks this premium plant.", null,
                    new view.gui.widgets.SeedPacket(ui, context.assets(), offer),
                    "coinIcon", Shop.DAILY_PRICE, null, 0, true, new Runnable() {
                        @Override
                        public void run() {
                            confirm(offer.getName(), Shop.DAILY_PRICE, 6, null);
                        }
                    });
            shelf.add(card).size(ShopCard.WIDTH, ShopCard.HEIGHT);
        }
    }

    private int trade(int column) {
        ShopCard card = new ShopCard(ui, context.assets(), "Coin Exchange",
                "Turn gems into coins.", null, "gemIcon", Shop.EXCHANGE_DIAMONDS,
                "coinIcon", Shop.EXCHANGE_COINS, false, new Runnable() {
                    @Override
                    public void run() {
                        confirm("Coin Exchange", Shop.EXCHANGE_DIAMONDS, 5, null);
                    }
                });
        shelf.add(card).size(ShopCard.WIDTH, ShopCard.HEIGHT);
        int next = column + 1;
        if (next % COLUMNS == 0) {
            shelf.row();
        }
        return next;
    }

    private int shelfItem(int column, final String title, String blurb,
            String currency, final int price, String icon, boolean sale,
            final int itemId, final Plants choice) {
        ShopCard card = new ShopCard(ui, context.assets(), title, blurb, icon,
                currency, price, sale, new Runnable() {
                    @Override
                    public void run() {
                        confirm(title, price, itemId, choice);
                    }
                });
        shelf.add(card).size(ShopCard.WIDTH, ShopCard.HEIGHT);
        int next = column + 1;
        if (next % COLUMNS == 0) {
            shelf.row();
        }
        return next;
    }

    private void pickPlant() {
        new view.gui.widgets.PlantPickPopup(ui, context.assets(),
                "Which plant?", shop.ownedPlants(context.app().getLoggedInUser()),
                new view.gui.widgets.PlantPickPopup.Choice() {
                    @Override
                    public void picked(Plants plant) {
                        confirm(plant.getName() + " packets", Shop.CHOICE_SEEDS_PRICE,
                                4, plant);
                    }
                }).showOn(stage);
    }

    private void confirm(String title, int price, final int itemId, final Plants choice) {
        new view.gui.ConfirmPopup(ui, "Buy " + title + "?",
                "This costs " + price + ".", "Buy", new Runnable() {
                    @Override
                    public void run() {
                        buy(itemId, choice);
                    }
                }).showOn(stage);
    }

    private void buy(int itemId, Plants choice) {
        App app = context.app();
        User user = app.getLoggedInUser();
        if (user == null) {
            toasts.error("Sign in first.");
            return;
        }
        String result = shop.buy(user, app.getGreenhouse(), itemId, 1, choice);
        if (result != null && result.startsWith("Error")) {
            toasts.error(result.substring("Error: ".length()));
        } else {
            toasts.success(result);
            new controller.SaveService().persist(user);
            stock();
        }
    }
}

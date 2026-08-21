package view.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.ChapterMenuController;
import model.User;

public final class TopBar extends Table {
    public enum Section { MAIN, ALMANAC, NEWS, LEADERBOARD, SHOP, GREENHOUSE, QUESTS, OTHER }

    public static final float BUTTON = 68f;
    private static final float WALLET_WIDTH = 196f;
    private static final float WALLET_HEIGHT = 40f;
    private static final float WALLET_ICON = 69f;
    private static final float PLUS_ICON = 40f;
    private static final int COIN_COARSE = 500;
    private static final int COIN_FINE = 25;
    private static final int GEM_COARSE = 50;
    private static final int GEM_FINE = 5;

    private final GameContext context;
    private final Section section;
    private final Label coinLabel;
    private final Label gemLabel;
    private final Table leftSlots = new Table();
    private final Table rightSlots = new Table();
    private final Table walletSlots = new Table();
    private final Table coinWallet;
    private final Table gemWallet;
    private final Table coinPlus = new Table();
    private final Table gemPlus = new Table();

    private Boolean lastSignedIn;
    private int lastCoins = Integer.MIN_VALUE;
    private int lastGems = Integer.MIN_VALUE;

    public TopBar(GameContext context, String screenTitle, Section section) {
        this.context = context;
        this.section = section;
        UiKit ui = context.ui();

        setBackground(ui.drawable("topBar"));
        pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        add(leftSlots).left();

        Label title = new Label(screenTitle == null ? "" : screenTitle, ui.skin(), "titleOnDark");
        title.setAlignment(Align.left);
        add(title).left().expandX().padLeft(Theme.PAD);

        coinLabel = new Label("0", ui.skin(), "onDark");
        gemLabel = new Label("0", ui.skin(), "onDark");
        coinWallet = wallet(coinPlus, "coinIcon", coinLabel, COIN_COARSE, COIN_FINE, "coin");
        gemWallet = wallet(gemPlus, "gemIcon", gemLabel, GEM_COARSE, GEM_FINE, "diamond");

        add(walletSlots).right().padRight(Theme.PAD_LARGE);
        add(rightSlots).right();

        refresh();
    }

    private void layoutSlots(boolean signedIn) {
        leftSlots.clearChildren();
        rightSlots.clearChildren();
        walletSlots.clearChildren();
        buildButtons(signedIn);
        if (signedIn) {
            walletSlots.add(coinWallet).padRight(WALLET_ICON * 0.45f);
            walletSlots.add(gemWallet);
        }
    }

    private void buildButtons(boolean signedIn) {
        addLeftButton(Icons.BACK, "Back", Theme.GREEN, new Runnable() {
            @Override
            public void run() {
                goBack();
            }
        });
        addLeftButton(Icons.SETTINGS, "Settings", Theme.plantFamily("WALL_NUT"), new Runnable() {
            @Override
            public void run() {
                openPopup(new SettingsPopup(context));
            }
        });
        if (signedIn && section != Section.ALMANAC) {
            addLeftButton(Icons.ALMANAC, "Almanac", Theme.BLUE, new Runnable() {
                @Override
                public void run() {
                    game().navigator().goMenu(view.MenuType.COLLECTION_MENU);
                }
            });
        }
        if (signedIn && section != Section.NEWS) {
            addRightButton(Icons.NEWS, "News", Theme.SUN_DEEP, new Runnable() {
                @Override
                public void run() {
                    game().navigator().goMenu(view.MenuType.NEWS_MENU);
                }
            });
        }
        addRightButton(Icons.PLAYERS, "Players", Theme.plantFamily("MODIFIER"), new Runnable() {
            @Override
            public void run() {
                openPopup(new PlayerListPopup(context, section != Section.LEADERBOARD));
            }
        });
    }

    private void goBack() {
        game().navigator().back();
    }

    private PvzGame game() {
        return (PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener();
    }

    private void openPopup(Popup popup) {
        if (getStage() != null) {
            popup.showOn(getStage());
        }
    }

    public TopBar addLeftButton(Icons.Icon icon, String text, Color face, Runnable action) {
        leftSlots.add(context.ui().iconButton(icon, text, face, action))
                .size(BUTTON).padRight(Theme.PAD_SMALL);
        return this;
    }

    public TopBar addRightButton(Icons.Icon icon, String text, Color face, Runnable action) {
        rightSlots.add(context.ui().iconButton(icon, text, face, action))
                .size(BUTTON).padLeft(Theme.PAD_SMALL);
        return this;
    }

    private Table wallet(Table plusSlot, String iconName, Label amount, final int coarse,
            final int fine, final String currency) {
        UiKit ui = context.ui();

        Table pill = new Table();
        pill.setBackground(ui.drawable("counter"));
        pill.add(amount).expandX().center()
                .padLeft(WALLET_ICON * 0.7f).padRight(PLUS_ICON * 0.7f);

        Table icon = new Table();
        icon.left();
        Image art = new Image(ui.drawable(iconName));
        art.setScaling(Scaling.fit);
        icon.add(art).size(WALLET_ICON).padLeft(-WALLET_ICON * 0.18f);

        plusSlot.right();
        Image plus = new Image(ui.drawable("plusIcon"));
        plus.setScaling(Scaling.fit);
        plusSlot.add(plus).size(PLUS_ICON).padRight(-PLUS_ICON * 0.12f);

        Stack stack = new Stack();
        stack.add(pill);
        stack.add(icon);
        stack.add(plusSlot);

        Table holder = new Table();
        holder.add(stack).width(WALLET_WIDTH).height(WALLET_HEIGHT);
        Animations.attachPress(holder);
        UiKit.onClick(holder, new Runnable() {
            @Override
            public void run() {
                cheat(shiftHeld() ? coarse : fine, currency);
            }
        });
        return holder;
    }

    private boolean shiftHeld() {
        return com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_LEFT)
                || com.badlogic.gdx.Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.SHIFT_RIGHT);
    }

    private void cheat(int amount, String currency) {
        if (!context.settings().isDebugMode() || context.user() == null) {
            return;
        }
        new ChapterMenuController(context.app()).handleCommand(
                new String[]{"menu", "cheat", "add", String.valueOf(amount), currency});
        refresh();
    }

    private void applyCheatFace() {
        boolean cheating = context.settings().isDebugMode();
        coinPlus.setVisible(cheating);
        gemPlus.setVisible(cheating);
    }

    public void refresh() {
        applyCheatFace();

        User user = context.user();
        boolean signedIn = user != null;
        if (lastSignedIn == null || lastSignedIn != signedIn) {
            lastSignedIn = signedIn;
            layoutSlots(signedIn);
        }
        int coins = (user == null) ? 0 : user.getCoins();
        int gems = (user == null) ? 0 : user.getGems();

        if (coins != lastCoins) {
            coinLabel.setText(String.valueOf(coins));
            if (lastCoins != Integer.MIN_VALUE) {
                Animations.pulse(coinLabel);
            }
            lastCoins = coins;
        }
        if (gems != lastGems) {
            gemLabel.setText(String.valueOf(gems));
            if (lastGems != Integer.MIN_VALUE) {
                Animations.pulse(gemLabel);
            }
            lastGems = gems;
        }
    }
}

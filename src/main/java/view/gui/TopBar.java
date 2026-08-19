package view.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.ChapterMenuController;
import model.User;

public final class TopBar extends Table {
    private final GameContext context;
    private final Label coinLabel;
    private final Label gemLabel;
    private final Table cheatGroup;

    private int lastCoins = Integer.MIN_VALUE;
    private int lastGems = Integer.MIN_VALUE;

    public TopBar(GameContext context, String screenTitle) {
        this.context = context;
        UiKit ui = context.ui();

        setBackground(ui.primitives().flat(Theme.darken(Theme.OUTLINE, 0.25f)));
        pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        Label title = new Label(screenTitle, ui.skin(), "titleOnDark");
        title.setAlignment(Align.left);
        add(title).left().expandX().padLeft(Theme.PAD);

        coinLabel = new Label("0", ui.skin(), "onDark");
        gemLabel = new Label("0", ui.skin(), "onDark");

        add(wallet(ui, Theme.COIN, coinLabel)).right().padRight(Theme.PAD);
        add(wallet(ui, Theme.GEM, gemLabel)).right();

        cheatGroup = new Table();
        cheatGroup.add(ui.styledButton("+500", "secondary", new Runnable() {
            @Override
            public void run() {
                cheat(500, "coin");
            }
        })).padLeft(Theme.PAD_SMALL);
        cheatGroup.add(ui.styledButton("+50", "info", new Runnable() {
            @Override
            public void run() {
                cheat(50, "diamond");
            }
        })).padLeft(Theme.PAD_SMALL);
        add(cheatGroup).right();

        refresh();
    }

    private Table wallet(UiKit ui, com.badlogic.gdx.graphics.Color color, Label amount) {
        Table group = new Table();
        group.add(ui.token(20, color)).size(20f).padRight(Theme.PAD_SMALL);
        group.add(amount);
        return group;
    }

    private void cheat(int amount, String currency) {
        if (context.user() == null) {
            return;
        }
        new ChapterMenuController(context.app()).handleCommand(
                new String[]{"menu", "cheat", "add", String.valueOf(amount), currency});
        refresh();
    }

    public void refresh() {
        cheatGroup.setVisible(context.settings().isDebugMode());

        User user = context.user();
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

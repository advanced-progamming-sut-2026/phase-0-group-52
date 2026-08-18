package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.MainMenuController;
import model.User;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.PvzGame;
import view.gui.Theme;

/**
 * The hub every other menu is reached from.
 *
 * <p>The specification's one hard requirement here is the news entry with an
 * indicator when unread items exist; the rest of the layout is free. Tiles are
 * used so each destination has room for a short description.
 */
public final class MainMenuScreen extends BaseScreen {

    private final MainMenuController controller;
    private Label newsBadge;

    public MainMenuScreen(GameContext context) {
        super(context, "Main menu");
        this.controller = new MainMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();

        User user = context.user();
        String greeting = (user == null) ? "Welcome" : "Welcome, " + user.getNickname();
        panel.add(new Label(greeting, ui.skin(), "title")).left().padBottom(Theme.PAD).row();

        panel.add(buildGrid()).row();

        panel.add(ui.dangerButton("Sign out", new Runnable() {
            @Override
            public void run() {
                signOut();
            }
        })).padTop(Theme.PAD).right();

        content.add(panel).center();
    }

    /** The nine destination tiles. */
    private Table buildGrid() {
        Table grid = new Table();
        grid.defaults().pad(Theme.PAD_SMALL).width(230f).height(84f);

        grid.add(menuTile("Adventure", "Chapters and levels", Theme.GREEN, "chapter_menu"));
        grid.add(menuTile("Collection", "Plants and zombies", Theme.BLUE, "collection_menu"));
        grid.add(newsTile());
        grid.row();

        grid.add(menuTile("Greenhouse", "Grow and harvest",
                Theme.plantFamily("SUN_PRODUCER"), "greenhouse_menu"));
        grid.add(menuTile("Quests", "Missions and minigames",
                Theme.plantFamily("LOBBER"), "travel_log_menu"));
        grid.add(tile("Shop", "Spend coins and gems", Theme.COIN, new Runnable() {
            @Override
            public void run() {
                openShop();
            }
        }));
        grid.row();

        grid.add(menuTile("Profile", "Your details", Theme.plantFamily("MODIFIER"), "profile"));
        grid.add(tile("Leaderboard", "Compare scores", Theme.plantFamily("MELEE"), new Runnable() {
            @Override
            public void run() {
                openLeaderboard();
            }
        }));
        grid.add(menuTile("Settings", "Difficulty and speed", Theme.TEXT_MUTED, "settings"));
        grid.row();
        return grid;
    }

    /** A tile whose only job is to enter another menu. */
    private Table menuTile(String title, String subtitle, Color accent, final String menuName) {
        return tile(title, subtitle, accent, new Runnable() {
            @Override
            public void run() {
                navigate(menuName);
            }
        });
    }

    /** A destination tile: coloured token, title, and one line of description. */
    private Table tile(String title, String subtitle, Color accent, Runnable action) {
        Table tile = new Table();
        tile.setBackground(ui.primitives().rounded(
                Theme.RADIUS, Theme.PANEL_SUNKEN, Theme.OUTLINE, 2));
        tile.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        tile.add(ui.token(34, accent)).size(34f).padRight(Theme.PAD);

        Table text = new Table();
        Label name = new Label(title, ui.skin(), "default");
        Label description = new Label(subtitle, ui.skin(), "muted");
        text.add(name).left().row();
        text.add(description).left();
        tile.add(text).left().expandX();

        Animations.attachPress(tile);
        view.gui.UiKit.onClick(tile, action);
        return tile;
    }

    /** The news tile carries a count badge when unread items exist. */
    private Stack newsTile() {
        Table tile = tile("News", "Announcements", Theme.SUN_DEEP, new Runnable() {
            @Override
            public void run() {
                navigate("news");
            }
        });

        newsBadge = new Label("", ui.skin(), "smallOnDark");
        newsBadge.setAlignment(Align.center);

        Table badgeHolder = new Table();
        badgeHolder.setBackground(ui.primitives().rounded(10, Theme.RED,
                Theme.darken(Theme.RED, 0.3f), 2));
        badgeHolder.add(newsBadge).pad(1f, 7f, 1f, 7f);

        Table anchor = new Table();
        anchor.top().right();
        anchor.add(badgeHolder).pad(Theme.PAD_SMALL);

        Stack stack = new Stack();
        stack.add(tile);
        stack.add(anchor);
        return stack;
    }

    @Override
    protected void refresh() {
        if (newsBadge == null) {
            return;
        }
        User user = context.user();
        int unread = (user == null || user.getNewsList() == null)
                ? 0 : user.getNewsList().getUnread().size();
        newsBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        newsBadge.getParent().setVisible(unread > 0);
    }

    // ------------------------------------------------------------- actions

    /** All navigation is issued as the same command the console accepts. */
    private void navigate(String menuName) {
        controller.handleCommand(new String[]{"menu", "enter", menuName});
    }

    private void signOut() {
        controller.handleCommand(new String[]{"menu", "logout"});
    }

    private void openLeaderboard() {
        game().showLeaderboard();
    }

    private void openShop() {
        game().showShop();
    }

    private PvzGame game() {
        return (PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener();
    }
}

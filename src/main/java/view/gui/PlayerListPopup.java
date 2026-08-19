package view.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.PlayerListController;
import model.Result;
import model.User;

import java.util.List;

public final class PlayerListPopup extends Popup {
    private final GameContext context;
    private final PlayerListController controller;

    private Table list;
    private User selected;

    public PlayerListPopup(GameContext context) {
        super(context.ui(), "Player List", 640f, 560f);
        this.context = context;
        this.controller = new PlayerListController(context.app());

        list = new Table();
        ScrollPane scroll = new ScrollPane(list, ui.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        body().add(scroll).grow().row();
        body().add(actions()).growX().padTop(Theme.PAD_SMALL);

        selected = controller.signedIn();
        rebuild();
    }

    private void rebuild() {
        list.clear();
        list.top();
        list.defaults().growX().pad(3f);

        List<User> players = controller.allPlayers();
        if (players.isEmpty()) {
            list.add(new Label("No accounts yet. Use Register to make one.",
                    ui.skin(), "muted")).left().pad(Theme.PAD).row();
            return;
        }
        for (User player : players) {
            list.add(playerRow(player)).row();
        }
    }

    private Table playerRow(final User player) {
        boolean isSelected = selected != null && selected.getId() == player.getId();
        boolean isCurrent = controller.isSignedIn(player);

        Table row = new Table();
        row.setBackground(ui.primitives().rounded(6,
                isSelected ? Theme.lighten(Theme.PANEL, 0.4f) : Theme.PANEL,
                isSelected ? Theme.GREEN_DARK : Theme.OUTLINE_SOFT,
                isSelected ? 3 : 2));
        row.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);
        row.left();

        Table text = new Table();
        text.left();

        Label nickname = new Label(player.getNickname(), ui.skin(), "title");
        nickname.setAlignment(Align.left);
        text.add(nickname).left().row();

        Label username = new Label(player.getUsername(), ui.skin(), "muted");
        text.add(username).left().row();

        text.add(statLines(player)).left().padTop(3f);

        row.add(text).growX();

        if (isCurrent) {
            Label badge = new Label("SIGNED IN", ui.skin(), "small");
            badge.setColor(Theme.GREEN_DARK);
            row.add(badge).right().top();
        }

        Animations.attachPress(row);
        UiKit.onClick(row, new Runnable() {
            @Override
            public void run() {
                selected = player;
                rebuild();
            }
        });
        return row;
    }

    private Table statLines(User player) {
        Table info = new Table();
        info.left();
        info.defaults().left().padRight(Theme.PAD_LARGE);
        info.add(new Label("Coins " + player.getCoins(), ui.skin(), "small"));
        info.add(new Label("Gems " + player.getGems(), ui.skin(), "small"));
        info.add(new Label("Games " + player.getGamesPlayed(), ui.skin(), "small")).row();
        info.add(new Label("Best " + player.getMaxPoint(), ui.skin(), "small"));
        info.add(new Label("Levels " + controller.completedLevels(player), ui.skin(), "small"));
        info.add(new Label("Meow " + player.getMostMeowPoint(), ui.skin(), "small"));
        return info;
    }

    private Table actions() {
        Table bar = new Table();
        bar.defaults().growX().uniformX().pad(2f).height(44f);

        bar.add(ui.styledButton("Register", "primary", new Runnable() {
            @Override
            public void run() {
                openForm(AccountFormPopup.Mode.REGISTER);
            }
        }));
        bar.add(ui.styledButton("Sign in", "info", new Runnable() {
            @Override
            public void run() {
                signIn();
            }
        }));
        bar.add(ui.styledButton("Profile", "secondary", new Runnable() {
            @Override
            public void run() {
                openProfile();
            }
        }));
        bar.add(ui.styledButton("Delete", "danger", new Runnable() {
            @Override
            public void run() {
                deleteSelected();
            }
        }));
        return bar;
    }

    private void signIn() {
        if (selected == null) {
            context.toasts().error("Select a player first.");
            return;
        }
        if (controller.isSignedIn(selected)) {
            context.toasts().info("You are already signed in as " + selected.getUsername() + ".");
            return;
        }
        openForm(AccountFormPopup.Mode.SIGN_IN);
    }

    private void openProfile() {
        if (selected == null) {
            context.toasts().error("Select a player first.");
            return;
        }
        openForm(AccountFormPopup.Mode.PROFILE);
    }

    private void deleteSelected() {
        Result result = controller.delete(selected);
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().success(result.getMessage());
        selected = null;
        rebuild();
    }

    private void openForm(AccountFormPopup.Mode mode) {
        AccountFormPopup form = new AccountFormPopup(context, mode, selected, new Runnable() {
            @Override
            public void run() {
                selected = controller.signedIn();
                rebuild();
            }
        });
        if (getStage() != null) {
            form.showOn(getStage());
        }
    }
}

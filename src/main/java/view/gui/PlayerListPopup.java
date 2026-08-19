package view.gui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
    private CheckBox stayBox;

    public PlayerListPopup(GameContext context) {
        super(context.ui(), "Player List", 640f, 560f);
        this.context = context;
        this.controller = new PlayerListController(context.app());

        addHeaderButton("Leaderboard", Theme.plantFamily("MELEE"), new Runnable() {
            @Override
            public void run() {
                close();
                ((view.gui.PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener())
                        .showLeaderboard();
            }
        });

        list = new Table();
        body().add(list).growX().row();
        footer().add(actions()).growX().row();
        footer().add(staySignedInBox()).left().padTop(Theme.PAD_SMALL);

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
        row.setBackground(ui.drawable(isSelected ? "listRowSelected" : "listRow"));
        row.pad(Theme.PAD, Theme.PAD_LARGE, Theme.PAD, Theme.PAD_LARGE);
        row.left();

        Table text = new Table();
        text.left();

        Label nickname = new Label(player.getNickname(), ui.skin(),
                isSelected ? "titleOnDark" : "title");
        nickname.setAlignment(Align.left);
        text.add(nickname).left().row();

        Label username = new Label(player.getUsername(), ui.skin(),
                isSelected ? "smallOnDark" : "muted");
        text.add(username).left().row();

        text.add(statLines(player, isSelected)).left().padTop(3f);

        row.add(text).growX();

        if (isCurrent) {
            Label badge = new Label("SIGNED IN", ui.skin(), "smallOnDark");
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

    private Table statLines(User player, boolean light) {
        Table info = new Table();
        info.left();
        info.defaults().left().padRight(Theme.PAD_LARGE);
        info.add(stat("Coins", player.getCoins(), Theme.COIN, light));
        info.add(stat("Gems", player.getGems(), Theme.GEM, light));
        info.add(stat("Games", player.getGamesPlayed(), Theme.BLUE, light)).row();
        info.add(stat("Best", player.getMaxPoint(), Theme.SUN, light));
        info.add(stat("Levels", controller.completedLevels(player), Theme.GREEN, light));
        info.add(stat("Meow", player.getMostMeowPoint(), Theme.plantFamily("MELEE"), light));
        return info;
    }

    private Table stat(String label, int value, com.badlogic.gdx.graphics.Color accent,
            boolean light) {
        Table cell = new Table();
        cell.add(ui.token(14, accent)).size(14f).padRight(4f);
        cell.add(new Label(label + " " + value, ui.skin(),
                light ? "smallOnDark" : "small")).left();
        return cell;
    }

    private Table actions() {
        Table bar = new Table();
        bar.defaults().growX().uniformX().pad(2f).height(44f);

        bar.add(ui.styledButton("Register", "primary", new Runnable() {
            @Override
            public void run() {
                openForm(AccountFormPopup.Mode.REGISTER);
            }
        })).height(46f);
        bar.add(ui.styledButton("Sign in", "primary", new Runnable() {
            @Override
            public void run() {
                signIn();
            }
        })).height(46f);
        bar.add(ui.styledButton("Sign out", "secondary", new Runnable() {
            @Override
            public void run() {
                signOut();
            }
        })).height(46f);
        bar.add(ui.styledButton("Profile", "secondary", new Runnable() {
            @Override
            public void run() {
                openProfile();
            }
        })).height(46f);
        bar.add(ui.styledButton("Delete", "danger", new Runnable() {
            @Override
            public void run() {
                deleteSelected();
            }
        })).height(46f);
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

    private CheckBox staySignedInBox() {
        stayBox = new CheckBox(" Stay signed in after closing the game", ui.skin());
        stayBox.setProgrammaticChangeEvents(false);
        stayBox.setChecked(controller.isStaySignedIn());
        stayBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                Result result = controller.setStaySignedIn(stayBox.isChecked());
                if (!result.isSuccess()) {
                    stayBox.setChecked(false);
                    context.toasts().error(result.getMessage());
                    return;
                }
                context.toasts().info(result.getMessage());
            }
        });
        return stayBox;
    }

    private void signOut() {
        Result result = controller.signOut();
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().success(result.getMessage());
        selected = null;
        refreshStayBox();
        rebuild();
    }

    private void refreshStayBox() {
        if (stayBox != null) {
            stayBox.setChecked(controller.isStaySignedIn());
        }
    }

    private void deleteSelected() {
        if (selected == null) {
            context.toasts().error("Select a player first.");
            return;
        }
        if (!controller.isSignedIn(selected)) {
            context.toasts().error("You can only delete the account you are signed in to.");
            return;
        }
        final User doomed = selected;
        ConfirmPopup confirm = new ConfirmPopup(ui, "Delete account",
                "Delete " + doomed.getUsername() + " for good? This cannot be undone.",
                "Delete", new Runnable() {
                    @Override
                    public void run() {
                        applyDelete(doomed);
                    }
                });
        if (getStage() != null) {
            confirm.showOn(getStage());
        }
    }

    private void applyDelete(User doomed) {
        Result result = controller.delete(doomed);
        if (!result.isSuccess()) {
            context.toasts().error(result.getMessage());
            return;
        }
        context.toasts().success(result.getMessage());
        selected = null;
        refreshStayBox();
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

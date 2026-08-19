package view.gui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import controller.menu.PlayerListController;
import model.Result;
import model.User;

import java.util.List;

public final class PlayerListPopup extends Popup {
    private static final float ROW_HEIGHT = 90f;

    private final GameContext context;
    private final PlayerListController controller;

    private Table list;
    private User selected;
    private CheckBox stayBox;

    public PlayerListPopup(GameContext context) {
        super(context.ui(), "Player List", 680f, 600f);
        this.context = context;
        this.controller = new PlayerListController(context.app());

        addHeaderButton("Leaderboard", new Runnable() {
            @Override
            public void run() {
                close();
                ((view.gui.PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener())
                        .showLeaderboard();
            }
        });

        body().setBackground(ui.drawable("questPanel"));
        body().pad(Theme.PAD_LARGE, Theme.PAD, Theme.PAD_LARGE, Theme.PAD);

        list = new Table();
        body().add(list).grow().top().row();

        footer().add(staySignedInBox()).left().padLeft(Theme.PAD_SMALL)
                .padTop(Theme.PAD_SMALL).padBottom(Theme.PAD_SMALL).row();
        footer().add(actions()).growX();

        selected = controller.signedIn();
        rebuild();
    }

    private void rebuild() {
        list.clear();
        list.top();
        list.defaults().growX().padBottom(4f);

        List<User> players = controller.allPlayers();
        if (players.isEmpty()) {
            list.add(new Label("No accounts yet. Use Register to make one.",
                    ui.skin(), "onDark")).left().pad(Theme.PAD).row();
            return;
        }
        for (User player : players) {
            list.add(playerRow(player)).height(ROW_HEIGHT).row();
        }
        list.add(new Table()).grow();
    }

    private Stack playerRow(final User player) {
        boolean isSelected = selected != null && selected.getId() == player.getId();

        Table row = new Table();
        row.setBackground(ui.drawable("listRow"));
        row.pad(Theme.PAD_SMALL, Theme.PAD_LARGE, Theme.PAD_SMALL, Theme.PAD_LARGE + 10f);

        Table names = new Table();
        names.left();
        Label nickname = new Label(player.getNickname(), ui.skin(),
                isSelected ? "rowNameSelected" : "rowName");
        names.add(nickname).left().row();
        names.add(new Label(player.getUsername(), ui.skin(), "rowSub")).left();
        row.add(names).growX().left();

        row.add(values(player)).right();

        Stack stack = new Stack();
        stack.add(row);
        if (isSelected) {
            Table ring = new Table();
            ring.setBackground(ui.drawable("listHighlight"));
            Table holder = new Table();
            holder.add(ring).grow().pad(3f);
            stack.add(holder);
        }

        Animations.attachPress(stack);
        UiKit.onClick(stack, new Runnable() {
            @Override
            public void run() {
                selected = player;
                rebuild();
            }
        });
        return stack;
    }

    private Table values(User player) {
        Table info = new Table();
        info.right();

        Label top = new Label(player.getCoins() + " coins    " + player.getGems() + " gems",
                ui.skin(), "rowValue");
        top.setAlignment(Align.right);
        info.add(top).right().row();

        Label bottom = new Label(controller.completedLevels(player) + " levels    "
                + player.getMaxPoint() + " best", ui.skin(), "rowValue");
        bottom.setAlignment(Align.right);
        info.add(bottom).right();
        return info;
    }

    private Table actions() {
        Table bar = new Table();
        bar.defaults().growX().uniformX().pad(3f).height(58f);

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
        bar.add(ui.styledButton("Sign out", "secondary", new Runnable() {
            @Override
            public void run() {
                signOut();
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

    private CheckBox staySignedInBox() {
        stayBox = ui.checkBox(" Stay signed in after closing the game");
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

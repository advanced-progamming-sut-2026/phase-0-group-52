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
        this(context, true);
    }

    public PlayerListPopup(GameContext context, boolean withLeaderboard) {
        super(context.ui(), "Player List", 760f, 600f);
        this.context = context;
        this.controller = new PlayerListController(context.app());

        if (withLeaderboard) {
            addHeaderIcon(Icons.LEADERBOARD, "Leaderboard", new Runnable() {
                @Override
                public void run() {
                    close();
                    ((view.gui.PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener())
                            .showLeaderboard();
                }
            });
        }

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
        boolean isSelected = isSame(selected, player);
        boolean isCurrent = controller.isSignedIn(player);

        Table row = new Table();
        row.setBackground(ui.drawable(isSelected ? "listRowSelected" : "listRow"));
        row.pad(Theme.PAD_SMALL, Theme.PAD_LARGE, Theme.PAD_SMALL, Theme.PAD_LARGE + 10f);

        Table names = new Table();
        names.left();

        Table nameCell = new Table();
        nameCell.add(new Label(player.getNickname(), ui.skin(),
                isSelected ? "rowNameSelected" : "rowName")).left().row();
        if (isCurrent) {
            Table rule = new Table();
            rule.setBackground(ui.primitives().flat(
                    isSelected ? Theme.INK_SELECTED : Theme.INK));
            nameCell.add(rule).height(3f).growX().padTop(2f);
        }
        names.add(nameCell).left().row();
        names.add(new Label(player.getUsername(), ui.skin(), "rowSub")).left().padTop(2f);
        row.add(names).growX().left();

        row.add(values(player)).right();

        Stack stack = new Stack();
        stack.add(row);

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

        Label score = new Label("High Score: " + player.getMaxPoint(), ui.skin(), "rowValue");
        score.setAlignment(Align.right);
        info.add(score).right().row();

        Label place = new Label("Chapter " + player.getLastChapter()
                + ", Level " + player.getLastLevel(), ui.skin(), "rowValue");
        place.setAlignment(Align.right);
        info.add(place).right();
        return info;
    }

    private boolean isSame(User left, User right) {
        return left != null && right != null && left.getUsername() != null
                && left.getUsername().equals(right.getUsername());
    }

    private Table actions() {
        Table bar = new Table();
        bar.defaults().growX().uniformX().pad(3f);

        action(bar, "Register", "primary", new Runnable() {
            @Override
            public void run() {
                openForm(AccountFormPopup.Mode.REGISTER);
            }
        });
        action(bar, "Sign in", "info", new Runnable() {
            @Override
            public void run() {
                signIn();
            }
        });
        action(bar, "Sign out", "secondary", new Runnable() {
            @Override
            public void run() {
                signOut();
            }
        });
        action(bar, "Profile", "secondary", new Runnable() {
            @Override
            public void run() {
                openProfile();
            }
        });
        action(bar, "Delete", "danger", new Runnable() {
            @Override
            public void run() {
                deleteSelected();
            }
        });
        return bar;
    }

    private void action(Table bar, String text, String style, Runnable onClick) {
        bar.add(ui.faceButton(text, style, onClick)).height(Theme.BUTTON_HEIGHT);
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

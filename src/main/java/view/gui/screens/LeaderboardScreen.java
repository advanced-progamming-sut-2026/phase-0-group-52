package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import model.Leaderboard;
import model.User;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.PvzGame;
import view.gui.Theme;

import java.util.List;

public final class LeaderboardScreen extends BaseScreen {
    private static final String[] COLUMNS = {"score", "level", "minigames", "daily", "quests"};
    private static final String[] HEADINGS = {"High score", "Progress", "Minigames", "Daily", "Quests"};

    private String sortColumn = "score";
    private boolean ascending;

    public LeaderboardScreen(GameContext context) {
        super(context, "Leaderboard");
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Rankings", ui.skin(), "title")).left().expandX();
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                ((PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener()).resumeRouting();
            }
        })).right();
        panel.add(header).growX().padBottom(Theme.PAD).row();

        Table table = new Table();
        table.top();
        table.defaults().pad(Theme.PAD_SMALL);

        table.add(new Label("#", ui.skin(), "muted")).width(40f).left();
        table.add(new Label("Player", ui.skin(), "muted")).width(180f).left();
        for (int i = 0; i < COLUMNS.length; i++) {
            table.add(sortHeader(HEADINGS[i], COLUMNS[i])).width(120f);
        }
        table.row();

        List<Leaderboard.Entry> entries = new Leaderboard().getEntries(sortColumn, ascending);
        if (entries.isEmpty()) {
            table.add(new Label("No players registered yet.", ui.skin(), "muted"))
                    .colspan(7).left().padTop(Theme.PAD).row();
        } else {
            int rank = 1;
            for (Leaderboard.Entry entry : entries) {
                addRow(table, rank++, entry);
            }
        }

        ScrollPane scroll = new ScrollPane(table, ui.skin());
        scroll.setFadeScrollBars(false);
        view.gui.UiKit.focusOnHover(scroll);
        panel.add(scroll).grow();

        content.add(panel).width(900f).height(470f).center();
    }

    private TextButton sortHeader(String label, final String column) {
        String suffix = "";
        if (column.equals(sortColumn)) {
            suffix = ascending ? "  ^" : "  v";
        }
        return ui.styledButton(label + suffix, "secondary", new Runnable() {
            @Override
            public void run() {
                if (column.equals(sortColumn)) {
                    ascending = !ascending;
                } else {
                    sortColumn = column;
                    ascending = false;
                }
                content.clear();
                build();
            }
        });
    }

    private void addRow(Table table, int rank, Leaderboard.Entry entry) {
        User user = entry.getUser();
        boolean isCurrentPlayer = context.user() != null
                && context.user().getId() == user.getId();
        String style = isCurrentPlayer ? "value" : "default";

        table.add(new Label(String.valueOf(rank), ui.skin(), style)).left();
        table.add(new Label(user.getUsername(), ui.skin(), style)).left();
        table.add(new Label(String.valueOf(user.getMaxPoint()), ui.skin(), style));
        table.add(new Label(entry.getProgressText(), ui.skin(), style));
        table.add(new Label(String.valueOf(user.getMiniGamesPlayed()), ui.skin(), style));
        table.add(new Label(String.valueOf(user.getQuestDailyNum()), ui.skin(), style));
        table.add(new Label(String.valueOf(user.getQuestNonDailyNum()), ui.skin(), style));
        table.row();
    }
}

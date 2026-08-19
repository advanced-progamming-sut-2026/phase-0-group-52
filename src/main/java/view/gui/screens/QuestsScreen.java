package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import controller.menu.ChapterMenuController;
import database.QuestRepository;
import model.quest.QuestProgress;
import model.quest.QuestState;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class QuestsScreen extends BaseScreen {
    private static final String[] PAGES = {"daily", "main", "epic"};
    private static final String[] MINIGAMES = {
            "vase_breaker", "wallnut_bowling", "izombie", "beghouled", "zombotany"};

    private final ChapterMenuController navigation;
    private final QuestRepository quests = new QuestRepository();

    private String page = "daily";
    private Table listArea;
    private final List<TextButton> pageTabs = new ArrayList<TextButton>();

    public QuestsScreen(GameContext context) {
        super(context, "Quests");
        this.navigation = new ChapterMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Missions", ui.skin(), "title")).left().padRight(Theme.PAD_LARGE);

        pageTabs.clear();
        for (final String name : PAGES) {
            TextButton tab = ui.styledButton(pretty(name), "tab", new Runnable() {
                @Override
                public void run() {
                    page = name;
                    rebuildList();
                    markTabs();
                }
            });
            pageTabs.add(tab);
            header.add(tab).width(100f).padRight(Theme.PAD_SMALL);
        }

        header.add(new Table()).expandX();
        header.add(ui.secondaryButton("Back", new Runnable() {
            @Override
            public void run() {
                navigation.handleCommand(new String[]{"menu", "enter", "chapter_menu"});
            }
        })).right();
        panel.add(header).growX().padBottom(Theme.PAD).row();

        Table body = new Table();

        listArea = new Table();
        ScrollPane scroll = new ScrollPane(listArea, ui.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        body.add(scroll).grow().padRight(Theme.PAD);
        body.add(buildMinigames()).width(280f).growY().top();
        panel.add(body).grow();

        markTabs();
        rebuildList();

        content.add(panel).grow();
    }

    private void markTabs() {
        for (int i = 0; i < pageTabs.size(); i++) {
            pageTabs.get(i).setColor(PAGES[i].equals(page) ? Theme.PANEL : Theme.PANEL_SUNKEN);
        }
    }

    private void rebuildList() {
        listArea.clear();
        listArea.top();
        listArea.defaults().growX().pad(Theme.PAD_SMALL);

        List<QuestProgress> onPage = questsForPage();
        if (onPage.isEmpty()) {
            listArea.add(new Label("No quests on this page.", ui.skin(), "muted")).left().row();
            return;
        }
        for (QuestProgress quest : onPage) {
            listArea.add(questCard(quest)).row();
        }
    }

    private List<QuestProgress> questsForPage() {
        List<QuestProgress> result = new ArrayList<QuestProgress>();
        if (context.user() == null) {
            return result;
        }
        QuestState state = quests.load(context.user().getUsername());
        if (state == null || state.getQuests() == null) {
            return result;
        }
        for (QuestProgress quest : state.getQuests()) {
            if (quest.getDef().getCategory().name().equalsIgnoreCase(page)) {
                result.add(quest);
            }
        }
        Collections.sort(result, new Comparator<QuestProgress>() {
            @Override
            public int compare(QuestProgress a, QuestProgress b) {
                return Integer.compare(
                        b.getDef().getPriority().getPrioritynum(),
                        a.getDef().getPriority().getPrioritynum());
            }
        });
        return result;
    }

    private Table questCard(QuestProgress quest) {
        Table card = ui.sunken();
        card.left();

        Color accent = quest.isCompleted() ? Theme.GREEN : Theme.BLUE;
        card.add(ui.token(30, accent)).size(30f).padRight(Theme.PAD).top();

        Table text = new Table();
        text.left();
        text.add(new Label(quest.getDef().getDisplayName(), ui.skin(), "default")).left().row();

        String reward = quest.getDef().getRewardAmount() + " "
                + quest.getDef().getRewardType().name().toLowerCase();
        text.add(new Label(reward + "   ·   priority "
                + quest.getDef().getPriority().name().toLowerCase(), ui.skin(), "muted")).left().row();

        text.add(progressBar(quest)).growX().height(10f).padTop(4f).row();

        int target = Math.max(1, quest.getTarget());
        int done = (int) Math.min(quest.getProgress(), target);
        text.add(new Label(done + " / " + target, ui.skin(), "muted")).left();
        card.add(text).growX();

        Label status = new Label(
                quest.isClaimed() ? "Claimed" : (quest.isCompleted() ? "Complete" : "In progress"),
                ui.skin(), quest.isCompleted() ? "value" : "muted");
        card.add(status).right().padLeft(Theme.PAD).top();
        return card;
    }

    private Table progressBar(QuestProgress quest) {
        float ratio = (quest.getTarget() <= 0) ? 0f
                : Math.min(1f, (float) (quest.getProgress() / quest.getTarget()));

        Table track = new Table();
        track.setBackground(ui.primitives().flat(Theme.alpha(Theme.OUTLINE, 0.25f)));
        track.left();

        Table fill = new Table();
        fill.setBackground(ui.primitives().flat(quest.isCompleted() ? Theme.GREEN : Theme.SUN_DEEP));
        track.add(fill).growY().width(Math.max(2f, 360f * ratio)).left();
        return track;
    }

    private Table buildMinigames() {
        Table box = ui.sunken();
        box.top();
        box.add(new Label("Minigames", ui.skin(), "title")).left().padBottom(Theme.PAD_SMALL).row();

        for (final String name : MINIGAMES) {
            Table row = new Table();
            row.setBackground(ui.primitives().rounded(6, Theme.PANEL, Theme.OUTLINE_SOFT, 2));
            row.pad(Theme.PAD_SMALL);

            row.add(ui.token(24, Theme.plantFamily("EXPLOSIVE"))).size(24f).padRight(Theme.PAD_SMALL);
            Label label = new Label(pretty(name), ui.skin(), "default");
            label.setAlignment(Align.left);
            row.add(label).left().expandX();

            Animations.attachPress(row);
            UiKit.onClick(row, new Runnable() {
                @Override
                public void run() {
                    context.toasts().info(pretty(name)
                            + " becomes playable when the lawn screen lands.");
                }
            });
            box.add(row).growX().padBottom(Theme.PAD_SMALL).row();
        }
        return box;
    }

    private String pretty(String raw) {
        String[] words = raw.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    @Override
    public void show() {
        super.show();
        if (listArea != null) {
            rebuildList();
        }
    }
}

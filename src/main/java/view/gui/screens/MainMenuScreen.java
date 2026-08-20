package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.ChapterMenuController;
import controller.menu.MainMenuController;
import database.QuestRepository;
import model.ChapterType;
import model.User;
import model.quest.QuestProgress;
import model.quest.QuestState;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.PvzGame;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.Carousel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MainMenuScreen extends BaseScreen {
    private static final int LEVELS_PER_CHAPTER = 4;
    private static final String[] MINIGAMES = {
            "Vasebreaker", "Wallnut Bowling", "I, Zombie", "Beghouled"};

    private final MainMenuController controller;
    private final ChapterMenuController chapters;
    private final QuestRepository questRepository = new QuestRepository();

    private Carousel chapterCarousel;
    private Carousel minigameCarousel;
    private Table questList;

    public MainMenuScreen(GameContext context) {
        super(context, "");
        this.controller = new MainMenuController(context.app());
        this.chapters = new ChapterMenuController(context.app());
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected view.gui.TopBar.Section section() {
        return view.gui.TopBar.Section.MAIN;
    }

    @Override
    protected void build() {
        Table columns = new Table();
        columns.defaults().pad(Theme.PAD_SMALL).space(Theme.PAD_SMALL);

        columns.add(adventurePanel()).grow().prefWidth(560f).minWidth(360f);

        Table right = new Table();
        right.defaults().growX().space(Theme.PAD_SMALL);
        right.add(minigamePanel()).height(262f).row();
        right.add(bottomRight()).grow();

        columns.add(right).grow();
        content.add(columns).grow();
    }

    private Table adventurePanel() {
        Table panel = ui.panel();
        panel.top();

        chapterCarousel = new Carousel(ui)
                .setCardSize(232f, 430f)
                .setSpacing(218f)
                .setFalloff(0.55f)
                .setListener(new Carousel.Listener() {
                    @Override
                    public void onSelected(int index) {
                    }

                    @Override
                    public void onActivated(int index) {
                        enterChapter(index);
                    }
                });
        chapterCarousel.setItems(chapterItems());

        panel.add(chapterCarousel).grow();
        return panel;
    }

    private List<Carousel.Item> chapterItems() {
        List<Carousel.Item> result = new ArrayList<Carousel.Item>();
        for (ChapterType chapter : ChapterType.values()) {
            boolean unlocked = isChapterUnlocked(chapter);
            boolean[] special = new boolean[LEVELS_PER_CHAPTER];
            special[LEVELS_PER_CHAPTER - 1] = true;
            special[1] = true;
            result.add(new Carousel.Item(pretty(chapter.name()),
                    Theme.chapter(chapter.name()), !unlocked,
                    LEVELS_PER_CHAPTER, clearedInChapter(chapter), special));
        }
        return result;
    }

    private Table minigamePanel() {
        Table panel = ui.panel();
        panel.top();

        minigameCarousel = new Carousel(ui)
                .setCardSize(112f, 208f)
                .setSpacing(124f)
                .setFalloff(0.34f)
                .setCentreAll(true)
                .setListener(new Carousel.Listener() {
                    @Override
                    public void onSelected(int index) {
                    }

                    @Override
                    public void onActivated(int index) {
                        context.toasts().info(MINIGAMES[index]
                                + " opens when the lawn screen lands.");
                    }
                });

        List<Carousel.Item> items = new ArrayList<Carousel.Item>();
        for (String name : MINIGAMES) {
            items.add(new Carousel.Item(name, Theme.plantFamily("EXPLOSIVE"), false));
        }
        minigameCarousel.setItems(items);

        panel.add(minigameCarousel).grow();
        return panel;
    }

    private Table bottomRight() {
        Table row = new Table();
        row.defaults().space(Theme.PAD_SMALL);

        row.add(questPanel()).grow();

        Table side = new Table();
        side.defaults().growX().grow().space(Theme.PAD_SMALL);
        side.add(bigButton("Greenhouse", Theme.plantFamily("SUN_PRODUCER"), new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{"menu", "enter", "greenhouse_menu"});
            }
        })).row();
        side.add(bigButton("Shop", Theme.COIN, new Runnable() {
            @Override
            public void run() {
                game().showShop();
            }
        }));
        row.add(side).width(210f).growY();
        return row;
    }

    private Table questPanel() {
        Table panel = ui.panel();
        panel.top();

        Label heading = new Label("Quests", ui.skin(), "title");
        panel.add(heading).left().padBottom(Theme.PAD_SMALL).row();

        questList = new Table();
        questList.top();
        ScrollPane scroll = new ScrollPane(questList, ui.skin());
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        UiKit.focusOnHover(scroll);
        panel.add(scroll).grow();

        rebuildQuests();
        return panel;
    }

    private void rebuildQuests() {
        questList.clear();
        questList.top();
        questList.defaults().growX().padBottom(Theme.PAD_SMALL);

        List<QuestProgress> quests = activeQuests();
        if (quests.isEmpty()) {
            questList.add(new Label("No quests right now.", ui.skin(), "muted")).left();
            return;
        }
        for (QuestProgress quest : quests) {
            questList.add(questRow(quest)).row();
        }
    }

    private List<QuestProgress> activeQuests() {
        List<QuestProgress> result = new ArrayList<QuestProgress>();
        if (context.user() == null) {
            return result;
        }
        QuestState state = questRepository.load(context.user().getUsername());
        if (state == null || state.getQuests() == null) {
            return result;
        }
        for (QuestProgress quest : state.getQuests()) {
            if (!quest.isClaimed()) {
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
        return result.subList(0, Math.min(result.size(), 6));
    }

    private Table questRow(final QuestProgress quest) {
        Table row = ui.sunken();
        row.left();

        Table text = new Table();
        text.left();
        text.add(new Label(quest.getDef().getDisplayName(), ui.skin(), "default")).left().row();
        text.add(new Label(quest.getDef().getRewardAmount() + " "
                + quest.getDef().getRewardType().name().toLowerCase(),
                ui.skin(), "muted")).left().row();
        text.add(progressBar(quest)).growX().height(8f).padTop(3f);
        row.add(text).growX();

        if (quest.isCompleted()) {
            row.add(ui.button("Claim", new Runnable() {
                @Override
                public void run() {
                    context.toasts().info("Rewards are granted automatically when a level ends.");
                }
            })).padLeft(Theme.PAD);
        }
        return row;
    }

    private Table progressBar(QuestProgress quest) {
        float ratio = (quest.getTarget() <= 0) ? 0f
                : Math.min(1f, (float) (quest.getProgress() / quest.getTarget()));
        Table track = new Table();
        track.setBackground(ui.primitives().flat(Theme.alpha(Theme.OUTLINE, 0.25f)));
        track.left();
        Table fill = new Table();
        fill.setBackground(ui.primitives().flat(quest.isCompleted() ? Theme.GREEN : Theme.SUN_DEEP));
        track.add(fill).growY().width(Math.max(2f, 240f * ratio)).left();
        return track;
    }

    private Table bigButton(String text, Color face, Runnable action) {
        Table button = new Table();
        button.setBackground(ui.buttonFace("green", face));
        Label label = new Label(text, ui.skin(), "onDark");
        label.setAlignment(Align.center);
        label.setWrap(true);
        button.add(label).width(150f).expand().center();
        Animations.attachPress(button);
        UiKit.onClick(button, action);
        return button;
    }

    private void enterChapter(int index) {
        ChapterType chapter = ChapterType.values()[index];
        if (!isChapterUnlocked(chapter)) {
            context.toasts().error("Finish the previous chapter first.");
            return;
        }
        chapters.handleCommand(new String[]{"menu", "enter", "chapter", "-c", chapter.name()});
        chapters.handleCommand(new String[]{"menu", "enter", "chapter_menu"});
    }

    private boolean isChapterUnlocked(ChapterType chapter) {
        User user = context.user();
        if (user == null) {
            return chapter == ChapterType.values()[0];
        }
        return chapter.ordinal() < Math.max(1, user.getLastChapter());
    }

    private int clearedInChapter(ChapterType chapter) {
        User user = context.user();
        if (user == null) {
            return 0;
        }
        int reachedChapter = Math.max(1, user.getLastChapter());
        int reachedLevel = Math.max(1, user.getLastLevel());
        int index = chapter.ordinal() + 1;
        if (index < reachedChapter) {
            return LEVELS_PER_CHAPTER;
        }
        if (index > reachedChapter) {
            return 0;
        }
        return Math.min(LEVELS_PER_CHAPTER, reachedLevel - 1);
    }

    private String pretty(String enumName) {
        String[] words = enumName.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private PvzGame game() {
        return (PvzGame) com.badlogic.gdx.Gdx.app.getApplicationListener();
    }

    @Override
    public void show() {
        super.show();
        if (chapterCarousel != null) {
            chapterCarousel.setItems(chapterItems());
        }
        if (questList != null) {
            rebuildQuests();
        }
        stage.setKeyboardFocus(chapterCarousel);
        stage.setScrollFocus(chapterCarousel);
    }
}

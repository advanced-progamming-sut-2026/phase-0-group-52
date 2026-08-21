package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import controller.menu.ChapterMenuController;
import model.ChapterType;
import model.LevelBuilder;
import model.User;
import view.gui.Animations;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

public final class AdventureScreen extends BaseScreen {
    private static final int LEVELS_PER_CHAPTER = 4;

    private final ChapterMenuController controller;

    private ChapterType openChapter;
    private Table chapterArea;
    private Table levelArea;

    public AdventureScreen(GameContext context) {
        super(context, "Adventure");
        this.controller = new ChapterMenuController(context.app());
    }

    @Override
    protected void build() {
        Table panel = ui.panel();
        panel.top();

        Table header = new Table();
        header.add(new Label("Chapters", ui.skin(), "title")).left().expandX();
        panel.add(header).growX().padBottom(Theme.PAD).row();

        chapterArea = new Table();
        chapterArea.defaults().pad(Theme.PAD_SMALL);
        panel.add(chapterArea).growX().row();

        levelArea = new Table();
        panel.add(levelArea).grow().padTop(Theme.PAD).row();

        rebuildChapters();
        rebuildLevels();

        content.add(panel).grow();
    }

    private void rebuildChapters() {
        chapterArea.clear();
        for (final ChapterType chapter : ChapterType.values()) {
            chapterArea.add(chapterCard(chapter)).width(270f).height(96f);
        }
    }

    private Table chapterCard(final ChapterType chapter) {
        final boolean unlocked = isChapterUnlocked(chapter);
        final boolean open = chapter == openChapter;

        Color accent = Theme.chapter(chapter.name());
        Table card = new Table();
        card.setBackground(ui.primitives().rounded(Theme.RADIUS,
                open ? Theme.PANEL : Theme.PANEL_SUNKEN,
                open ? accent : Theme.OUTLINE, open ? 4 : 2));
        card.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);

        card.add(ui.token(38, unlocked ? accent : Theme.LOCKED)).size(38f).padRight(Theme.PAD);

        Table text = new Table();
        Label name = new Label(chapter.getDisplayName(), ui.skin(), "default");
        text.add(name).left().row();
        text.add(new Label(unlocked
                ? clearedInChapter(chapter) + " / " + LEVELS_PER_CHAPTER + " levels"
                : "Locked", ui.skin(), "muted")).left();
        card.add(text).left().expandX();

        Animations.attachPress(card);
        UiKit.onClick(card, new Runnable() {
            @Override
            public void run() {
                if (!unlocked) {
                    context.toasts().error("Finish the previous chapter first.");
                    return;
                }
                openChapter = chapter;
                rebuildChapters();
                rebuildLevels();
            }
        });
        return card;
    }

    private void rebuildLevels() {
        levelArea.clear();
        levelArea.top();

        if (openChapter == null) {
            Label hint = new Label("Pick a chapter to see its levels.", ui.skin(), "muted");
            levelArea.add(hint).center().padTop(Theme.PAD_LARGE);
            return;
        }

        Table box = ui.sunken();
        box.top().left();

        Label heading = new Label(openChapter.getDisplayName(), ui.skin(), "title");
        box.add(heading).left().colspan(LEVELS_PER_CHAPTER).padBottom(Theme.PAD_SMALL).row();

        int cleared = clearedInChapter(openChapter);
        for (int i = 1; i <= LEVELS_PER_CHAPTER; i++) {
            box.add(levelCard(i, i <= cleared + 1, i <= cleared)).width(150f).height(84f)
                    .pad(Theme.PAD_SMALL);
        }
        box.row();

        Table specials = new Table();
        specials.add(new Label("Special level:", ui.skin(), "muted")).padRight(Theme.PAD_SMALL);

        final SelectBox<String> types = new SelectBox<String>(ui.skin());
        types.setItems(LevelBuilder.specialTypes().split(",\\s*"));
        specials.add(types).width(200f).padRight(Theme.PAD_SMALL);
        specials.add(ui.styledButton("Start special", "info", new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{
                        "start", "special", "-t", types.getSelected().trim()});
                context.toasts().info("The lawn screen arrives in the next milestone.");
            }
        }));
        box.add(specials).colspan(LEVELS_PER_CHAPTER).left().padTop(Theme.PAD).row();

        levelArea.add(box).growX().top();
    }

    private Table levelCard(final int number, final boolean playable, boolean cleared) {
        Table card = new Table();
        card.setBackground(ui.primitives().rounded(8,
                playable ? Theme.PANEL : Theme.alpha(Theme.LOCKED, 0.5f),
                Theme.OUTLINE, 2));

        Label label = new Label("Level " + number, ui.skin(), "default");
        label.setAlignment(Align.center);
        card.add(label).expand().center().row();

        Label state = new Label(cleared ? "Cleared" : (playable ? "Play" : "Locked"),
                ui.skin(), "muted");
        state.setAlignment(Align.center);
        card.add(state).padBottom(Theme.PAD_SMALL);

        Animations.attachPress(card);
        UiKit.onClick(card, new Runnable() {
            @Override
            public void run() {
                if (!playable) {
                    context.toasts().error("Clear the level before it to unlock this one.");
                    return;
                }
                selectLevel(number);
            }
        });
        return card;
    }

    private void selectLevel(int number) {
        controller.handleCommand(new String[]{
                "menu", "enter", "chapter", "-c", openChapter.name()});
        controller.handleCommand(new String[]{
                "start", "level", "-c", openChapter.name(), "-l", String.valueOf(number)});
        controller.handleCommand(new String[]{"menu", "enter", "choose_plant_menu"});
    }

    private boolean isChapterUnlocked(ChapterType chapter) {
        User user = context.user();
        if (user == null) {
            return chapter == ChapterType.first();
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
        int index = chapter.number();
        if (index < reachedChapter) {
            return LEVELS_PER_CHAPTER;
        }
        if (index > reachedChapter) {
            return 0;
        }
        return Math.min(LEVELS_PER_CHAPTER, reachedLevel - 1);
    }

}

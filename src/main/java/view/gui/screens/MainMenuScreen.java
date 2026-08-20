package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
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
import view.gui.Pam;
import view.gui.widgets.Carousel;
import view.gui.widgets.PamActor;
import view.gui.widgets.QuestCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MainMenuScreen extends BaseScreen {
    private static final String PORTAL_CLIP = "idle";
    private static final float PORTAL_COVERAGE = 1.5f;
    private static final float LOGO_HEIGHT = 68f;
    private static final float LOGO_OVERHANG = 26f;
    private static final float PANEL_INSET = 12f;
    private static final float QUEST_CARD_HEIGHT = 66f;
    private static final int MAX_PANEL_QUESTS = 6;
    private static final float ADVENTURE_WIDTH = 624f;
    private static final float QUEST_PANEL_WIDTH = 300f;
    private static final float GREENHOUSE_WIDTH = 252f;
    private static final float GREENHOUSE_HEIGHT = 160f;
    private static final float BEE_SIZE = 62f;
    private static final float BEE_EXTENT = 300f;
    private static final float SHOP_DIM = 0.45f;
    private static final String[] BEE_CLIPS = {
            "idle", "idle", "idle", "idle", "idle", "idle", "idle", "idle",
            "idle", "idle", "idle", "idle", "idle", "idle", "idle", "idle",
            "action3", "action3", "action1", "action2"};
    private static final float VEIL_DELAY = 0.35f;
    private static final float VEIL_FADE = 0.45f;
    private static final String[] ISLANDS = {
            "IMAGE_UI_UNIVERSE_WORLDS_EGYPT",
            "IMAGE_UI_UNIVERSE_WORLDS_ICEAGE",
            "IMAGE_UI_UNIVERSE_WORLDS_BEACH",
            "IMAGE_UI_UNIVERSE_WORLDS_DARK"};
    private static final String[] MINIGAMES = {
            "Vasebreaker", "Wallnut Bowling", "I, Zombie", "Beghouled"};

    private final MainMenuController controller;
    private final ChapterMenuController chapters;
    private final QuestRepository questRepository = new QuestRepository();

    private Carousel chapterCarousel;
    private PamActor portal;
    private Image veil;
    private boolean[] lockState;
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
    protected String backdropImage() {
        return "assets/backgrounds/main_menu.png";
    }

    @Override
    protected void build() {
        Table columns = new Table();
        columns.defaults().pad(Theme.PAD_SMALL).space(Theme.PAD_SMALL);

        columns.add(adventurePanel()).growY().width(ADVENTURE_WIDTH);

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
                .setCardSize(264f, 492f)
                .setSpacing(232f)
                .setFalloff(0.68f)
                .setListener(new Carousel.Listener() {
                    @Override
                    public void onSelected(int index) {
                    }

                    @Override
                    public void onActivated(int index) {
                        enterChapter(index);
                    }
                });
        chapterCarousel.setLockAnimation(context.pam(), Pam.WORLD_LOCK);
        syncChapters();

        Stack layers = new Stack();
        portal = new PamActor(context.pam(), Pam.PORTAL, PORTAL_CLIP)
                .setCoverage(PORTAL_COVERAGE);
        if (portal.isReady()) {
            Table backing = new Table();
            backing.setBackground(ui.primitives().flat(Theme.PORTAL_VOID));
            layers.add(backing);
            layers.add(portal);
        }
        layers.add(chapterCarousel);

        panel.add(layers).grow().pad(-PANEL_INSET);

        Stack crested = new Stack();
        crested.add(panel);
        crested.add(logo());

        Table holder = new Table();
        holder.add(crested).grow();
        return holder;
    }

    private Table logo() {
        Table crest = new Table();
        crest.top();
        com.badlogic.gdx.scenes.scene2d.utils.Drawable art =
                ui.imageFile("assets/ui/pvz2_logo_horizontal.png");
        if (art == null) {
            return crest;
        }
        Image mark = new Image(art);
        mark.setScaling(Scaling.fit);
        crest.add(mark).height(LOGO_HEIGHT).padTop(-LOGO_OVERHANG).center();
        return crest;
    }

    private List<Carousel.Item> chapterItems(boolean[] locked) {
        List<Carousel.Item> result = new ArrayList<Carousel.Item>();
        ChapterType[] chapters = ChapterType.values();
        for (int i = 0; i < chapters.length; i++) {
            ChapterType chapter = chapters[i];
            boolean[] special = new boolean[ChapterType.LEVELS_PER_CHAPTER];
            special[1] = true;
            special[2] = true;
            result.add(new Carousel.Item(pretty(chapter.name()),
                    Theme.chapter(chapter.name()), locked[i],
                    ChapterType.LEVELS_PER_CHAPTER, clearedInChapter(chapter), special)
                    .setArt(island(i)));
        }
        return result;
    }

    private com.badlogic.gdx.graphics.g2d.TextureRegion island(int index) {
        if (context.pam() == null || index >= ISLANDS.length) {
            return null;
        }
        return context.pam().region(ISLANDS[index]);
    }

    private boolean[] lockStates() {
        ChapterType[] chapters = ChapterType.values();
        boolean[] locked = new boolean[chapters.length];
        for (int i = 0; i < chapters.length; i++) {
            locked[i] = !isChapterUnlocked(chapters[i]);
        }
        return locked;
    }

    private void syncChapters() {
        boolean[] now = lockStates();
        int opened = firstOpened(lockState, now);
        if (opened < 0) {
            lockState = now;
            chapterCarousel.setItems(chapterItems(now));
            return;
        }
        chapterCarousel.setItems(chapterItems(lockState));
        lockState = now;
        chapterCarousel.playUnlock(opened, new Runnable() {
            @Override
            public void run() {
                chapterCarousel.setItems(chapterItems(lockState));
            }
        });
    }

    private int firstOpened(boolean[] before, boolean[] after) {
        if (before == null) {
            return -1;
        }
        for (int i = 0; i < after.length && i < before.length; i++) {
            if (before[i] && !after[i]) {
                return i;
            }
        }
        return -1;
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

        row.add(questPanel()).grow().minWidth(QUEST_PANEL_WIDTH);

        Table side = new Table();
        side.top();
        side.defaults().space(Theme.PAD_SMALL);
        side.add(greenhouseButton(new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{"menu", "enter", "greenhouse_menu"});
            }
        })).size(GREENHOUSE_WIDTH, GREENHOUSE_HEIGHT).row();
        side.add(shopButton(new Runnable() {
            @Override
            public void run() {
                game().showShop();
            }
        })).size(GREENHOUSE_WIDTH, GREENHOUSE_HEIGHT);
        row.add(side).width(GREENHOUSE_WIDTH).growY();
        return row;
    }

    private Table questPanel() {
        Table panel = ui.panel();
        panel.top();

        questList = new Table();
        questList.top();
        ScrollPane scroll = new ScrollPane(questList, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(true, false);
        UiKit.focusOnHover(scroll);

        Table front = new Table();
        front.top();
        front.pad(Theme.PAD_SMALL);
        front.add(new Label("Quests", ui.skin(), "titleOnDark")).left()
                .padBottom(Theme.PAD_SMALL).row();
        front.add(scroll).grow();

        Stack layers = new Stack();
        com.badlogic.gdx.scenes.scene2d.utils.Drawable art =
                ui.imageFile("assets/backgrounds/quests.png");
        if (art != null) {
            Table backdrop = new Table();
            backdrop.setBackground(art);
            layers.add(backdrop);
        }
        layers.add(front);

        panel.add(layers).grow().pad(-PANEL_INSET);
        Animations.attachPress(panel);
        UiKit.onClick(panel, new Runnable() {
            @Override
            public void run() {
                controller.handleCommand(new String[]{"menu", "enter", "travel_log_menu"});
            }
        });

        rebuildQuests();
        return panel;
    }

    private void rebuildQuests() {
        questList.clear();
        questList.top();
        questList.defaults().growX().padBottom(Theme.PAD_SMALL);

        List<QuestProgress> quests = activeQuests();
        if (quests.isEmpty()) {
            questList.add(new Label("No quests right now.", ui.skin(), "onDark")).left();
            return;
        }
        for (QuestProgress quest : quests) {
            questList.add(new QuestCard(ui, context.pam(), quest, true, null, null))
                    .height(QUEST_CARD_HEIGHT).row();
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
            if (!quest.isCompleted()) {
                result.add(quest);
            }
        }
        Collections.sort(result, new Comparator<QuestProgress>() {
            @Override
            public int compare(QuestProgress a, QuestProgress b) {
                if (a.isPinned() != b.isPinned()) {
                    return a.isPinned() ? -1 : 1;
                }
                return Double.compare(ratio(b), ratio(a));
            }
        });
        return result.subList(0, Math.min(result.size(), MAX_PANEL_QUESTS));
    }

    private static double ratio(QuestProgress quest) {
        return quest.getTarget() <= 0 ? 0d : quest.getProgress() / quest.getTarget();
    }

    private Table greenhouseButton(Runnable action) {
        Stack layers = new Stack();
        layers.add(artLayer("assets/backgrounds/greenhouse.png"));

        PamActor bee = new PamActor(context.pam(), Pam.BEE, "idle")
                .setFit(true)
                .setExtent(-BEE_EXTENT / 2f, -BEE_EXTENT / 2f, BEE_EXTENT, BEE_EXTENT)
                .cycle(BEE_CLIPS);
        if (bee.isReady()) {
            Table beeLayer = new Table();
            beeLayer.top().right();
            beeLayer.add(bee).size(BEE_SIZE).padRight(Theme.PAD_SMALL).padTop(Theme.PAD_SMALL);
            layers.add(beeLayer);
        }

        Table caption = new Table();
        caption.bottom().left();
        caption.add(new Label("GREENHOUSE", ui.skin(), "titleOnDark"))
                .padLeft(Theme.PAD_SMALL).padBottom(Theme.PAD_SMALL);
        layers.add(caption);

        Table button = ui.panel();
        button.add(layers).grow().pad(-PANEL_INSET);
        Animations.attachPress(button);
        UiKit.onClick(button, action);
        return button;
    }

    private Table shopButton(Runnable action) {
        Stack layers = new Stack();

        Table backdrop = artLayerRegion("IMAGE_UI_THYMED_EVENTS_COINS_SPREE_EVENT_BG",
                SHOP_DIM);
        layers.add(backdrop);

        Table front = artLayerRegion("IMAGE_UI_ALMANAC_FINDMORE_STORE", 1f);
        layers.add(front);

        Table caption = new Table();
        caption.bottom().right();
        caption.add(new Label("SHOP", ui.skin(), "titleOnDark"))
                .padRight(Theme.PAD_SMALL).padBottom(Theme.PAD_SMALL);
        layers.add(caption);

        Table button = ui.panel();
        button.add(layers).grow().pad(-PANEL_INSET);
        Animations.attachPress(button);
        UiKit.onClick(button, action);
        return button;
    }

    private Table artLayerRegion(String imageId, float dim) {
        Table layer = new Table();
        com.badlogic.gdx.graphics.g2d.TextureRegion art = context.pam() == null ? null
                : context.pam().region(imageId);
        if (art == null) {
            return layer;
        }
        Image image = new Image(
                new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(art));
        image.setScaling(dim < 1f ? Scaling.fill : Scaling.fit);
        image.setColor(dim, dim, dim, 1f);
        com.badlogic.gdx.scenes.scene2d.ui.Container<Image> box =
                new com.badlogic.gdx.scenes.scene2d.ui.Container<Image>(image);
        box.setClip(true);
        box.fill();
        layer.add(box).grow();
        return layer;
    }

    private Table artLayer(String path) {
        Table layer = new Table();
        com.badlogic.gdx.graphics.g2d.TextureRegion art = ui.regionFile(path);
        if (art != null) {
            Image image = new Image(
                    new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(art));
            image.setScaling(Scaling.fill);
            com.badlogic.gdx.scenes.scene2d.ui.Container<Image> box =
                    new com.badlogic.gdx.scenes.scene2d.ui.Container<Image>(image);
            box.setClip(true);
            box.fill();
            layer.add(box).grow();
        }
        return layer;
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
        if (chapterCarousel.isTransitioning()) {
            return;
        }
        final ChapterType chapter = ChapterType.values()[index];
        if (!isChapterUnlocked(chapter)) {
            if (!context.settings().isDebugMode()) {
                context.toasts().error("Finish the previous chapter first.");
                return;
            }
            chapterCarousel.playUnlock(index, new Runnable() {
                @Override
                public void run() {
                    beginEnter(chapter);
                }
            });
            return;
        }
        beginEnter(chapter);
    }

    private void beginEnter(final ChapterType chapter) {
        if (portal != null) {
            portal.play("open", false, null);
        }
        chapterCarousel.playEnter();
        veil().addAction(Actions.sequence(
                Actions.delay(VEIL_DELAY),
                Actions.alpha(1f, VEIL_FADE),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        openChapter(chapter);
                    }
                })));
    }

    private void openChapter(ChapterType chapter) {
        chapters.handleCommand(new String[]{"menu", "enter", "chapter", "-c", chapter.name()});
        chapters.handleCommand(new String[]{"menu", "enter", "chapter_menu"});
    }

    private Image veil() {
        if (veil == null) {
            veil = new Image(ui.primitives().flat(com.badlogic.gdx.graphics.Color.BLACK));
            veil.setFillParent(true);
            veil.setTouchable(Touchable.disabled);
        }
        veil.clearActions();
        veil.remove();
        veil.getColor().a = 0f;
        stage.addActor(veil);
        return veil;
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
            return ChapterType.LEVELS_PER_CHAPTER;
        }
        if (index > reachedChapter) {
            return 0;
        }
        return Math.min(ChapterType.LEVELS_PER_CHAPTER, reachedLevel - 1);
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
        if (veil != null) {
            veil.clearActions();
            veil.remove();
        }
        if (questList != null) {
            rebuildQuests();
        }
        stage.setKeyboardFocus(chapterCarousel);
        stage.setScrollFocus(chapterCarousel);
    }
}

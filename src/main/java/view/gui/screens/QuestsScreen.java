package view.gui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import controller.menu.TravelLogMenuController;
import database.QuestRepository;
import model.quest.QuestCategory;
import model.quest.QuestProgress;
import model.quest.QuestState;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.QuestCard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class QuestsScreen extends BaseScreen {
    private static final String[] TABS = {"All", "Daily", "Main", "Epic"};
    private static final float TAB_HEIGHT = 32f;
    private static final float TAB_WIDTH = 116f;
    private static final float CARD_WIDTH = 348f;
    private static final int COLUMNS = 3;
    private static final float CARD_MIN_HEIGHT = 254f;
    private static final float CORNER_WIDTH = 232f;
    private static final float CORNER_HEIGHT = 172f;
    private static final float CORNER_PEEK = 84f;
    private static final float SHADOW_SPREAD = 10f;
    private static final float PANEL_LEFT_GAP = 46f;

    private final QuestRepository repository = new QuestRepository();
    private final TravelLogMenuController controller = new TravelLogMenuController();

    private String tab = "All";
    private boolean byProgress;
    private boolean ascending;
    private boolean hideFinished;
    private ScrollPane list;

    public QuestsScreen(GameContext context) {
        super(context, "Quests");
    }

    @Override
    protected view.gui.TopBar.Section section() {
        return view.gui.TopBar.Section.QUESTS;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected String backdropImage() {
        return "assets/backgrounds/quests.png";
    }

    @Override
    protected void build() {
        content.clearChildren();
        Stack layers = new Stack();
        layers.add(panelLayer());
        layers.add(cornerLayer());
        content.add(layers).grow();
    }

    private Table panelLayer() {
        Table column = new Table();
        column.top();
        column.add(tabRow()).left().padLeft(PANEL_LEFT_GAP + Theme.PAD_LARGE)
                .padBottom(-8f).row();

        Table head = new Table();
        head.add(heading()).left().expandX().top();
        head.add(controls()).right().top();

        Table panel = ui.panel();
        panel.top();
        panel.add(head).growX().padBottom(Theme.PAD_SMALL).row();
        panel.add(listArea()).grow();

        column.add(panel).grow().pad(Theme.PAD_SMALL)
                .padLeft(PANEL_LEFT_GAP).padRight(PANEL_LEFT_GAP)
                .padBottom(Theme.PAD_LARGE);
        return column;
    }

    private void rebuild() {
        content.clear();
        content.getColor().a = 1f;
        build();
    }

    private Table tabRow() {
        Table row = new Table();
        row.bottom().left();
        for (final String name : TABS) {
            boolean active = name.equals(tab);
            row.add(tabButton(name)).width(TAB_WIDTH)
                    .height(active ? TAB_HEIGHT + 7f : TAB_HEIGHT).bottom();
        }
        return row;
    }

    private Table tabButton(final String name) {
        boolean active = name.equals(tab);
        Color colour = colourFor(name);
        Table button = new Table();
        button.setBackground(ui.primitives().roundedTop(11,
                active ? colour : Theme.darken(colour, 0.40f),
                Theme.darken(colour, 0.62f), 3));
        Label.LabelStyle base = ui.skin().get("default", Label.LabelStyle.class);
        Label label = new Label(name.toUpperCase(), new Label.LabelStyle(base.font,
                active ? Theme.TEXT_ON_DARK : Theme.alpha(Theme.TEXT_ON_DARK, 0.72f)));
        button.add(label).expand().center().padBottom(3f);
        view.gui.Animations.attachPress(button);
        UiKit.onClick(button, new Runnable() {
            @Override
            public void run() {
                tab = name;
                rebuild();
            }
        });
        return button;
    }

    private Color colourFor(String name) {
        if ("Epic".equals(name)) {
            return Theme.QUEST_EPIC;
        }
        if ("Main".equals(name)) {
            return Theme.QUEST_MAIN;
        }
        if ("Daily".equals(name)) {
            return Theme.QUEST_DAILY;
        }
        return Theme.OUTLINE_SOFT;
    }

    private Table heading() {
        Table row = new Table();
        row.left();
        row.add(new Label("Total ", ui.skin(), "title"));
        if (!"All".equals(tab)) {
            Label category = new Label(tab, ui.skin(), "title");
            category.setColor(tabColour());
            row.add(category);
            row.add(new Label(" Quests", ui.skin(), "title"));
        } else {
            row.add(new Label("quests", ui.skin(), "title"));
        }
        row.add(new Label(" finished: " + finishedCount(), ui.skin(), "title"));
        return row;
    }

    private Color tabColour() {
        return colourFor(tab);
    }

    private Table controls() {
        Table stack = new Table();
        stack.right();
        stack.add(sortControl()).right().row();

        CheckBox box = ui.checkBox("Hide finished");
        box.getLabel().setColor(Theme.TEXT);
        box.setChecked(hideFinished);
        UiKit.onClick(box, new Runnable() {
            @Override
            public void run() {
                hideFinished = !hideFinished;
                rebuild();
            }
        });
        stack.add(box).right().padTop(2f);
        return stack;
    }

    private Table sortControl() {
        Table cell = new Table();
        Label label = new Label(byProgress ? "Progress" : "Priority", ui.skin(), "rowHeader");
        cell.add(label).padTop(UiKit.opticalPad(label));
        Image arrow = new Image(ui.drawable(ascending ? "sortAscending" : "sortDescending"));
        arrow.setScaling(Scaling.fit);
        cell.add(arrow).size(17f).padLeft(4f);
        view.gui.Animations.attachPress(cell);
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                cycleSort();
                rebuild();
            }
        });
        return cell;
    }

    private void cycleSort() {
        if (!ascending) {
            ascending = true;
            return;
        }
        ascending = false;
        byProgress = !byProgress;
    }

    private Table listArea() {
        Table rows = new Table();
        rows.top();

        List<QuestProgress> quests = visibleQuests();
        if (quests.isEmpty()) {
            rows.add(new Label("Nothing here yet.", ui.skin(), "muted")).left().pad(Theme.PAD);
        }
        int column = 0;
        for (final QuestProgress quest : quests) {
            rows.add(card(quest)).width(CARD_WIDTH).minHeight(CARD_MIN_HEIGHT)
                    .padLeft(Theme.PAD_SMALL / 2f).padRight(Theme.PAD_SMALL / 2f)
                    .padBottom(Theme.PAD_SMALL).top();
            column++;
            if (column % COLUMNS == 0) {
                rows.row();
            }
        }

        ScrollPane scroll = new ScrollPane(rows, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, true);
        UiKit.focusOnHover(scroll);
        list = scroll;

        Table holder = new Table();
        Drawable art = ui.imageFile("assets/backgrounds/quests_" + tab.toLowerCase() + ".png");
        if (art != null) {
            holder.setBackground(art);
        }
        holder.add(scroll).grow().pad(Theme.PAD_SMALL);
        return holder;
    }

    private QuestCard card(final QuestProgress quest) {
        final QuestCard card = new QuestCard(ui, context.pam(), quest, new Runnable() {
            @Override
            public void run() {
                claim(quest);
            }
        });
        if (!card.claimable()) {
            UiKit.onClick(card, new Runnable() {
                @Override
                public void run() {
                    if (context.settings().isDebugMode()) {
                        cheatComplete(quest);
                    }
                }
            });
        }
        card.setActionable(new QuestCard.Actionable() {
            @Override
            public boolean isActionable() {
                return card.claimable() || context.settings().isDebugMode();
            }
        });
        return card;
    }

    private void claim(QuestProgress quest) {
        if (controller.claimQuest(quest.getDef().name())) {
            context.toasts().info("Claimed " + quest.getDef().getDisplayName() + ".");
            refreshTopBar();
        }
        rebuild();
    }

    private void cheatComplete(QuestProgress quest) {
        if (controller.completeQuest(quest.getDef().name())) {
            context.toasts().info(quest.getDef().getDisplayName() + " marked complete.");
        }
        rebuild();
    }

    private List<QuestProgress> visibleQuests() {
        List<QuestProgress> result = new ArrayList<QuestProgress>();
        if (context.user() == null) {
            return result;
        }
        QuestState state = repository.load(context.user().getUsername());
        if (state == null || state.getQuests() == null) {
            return result;
        }
        for (QuestProgress quest : state.getQuests()) {
            if (!matchesTab(quest)) {
                continue;
            }
            if (hideFinished && quest.isClaimed()) {
                continue;
            }
            result.add(quest);
        }
        Collections.sort(result, order());
        return result;
    }

    private boolean matchesTab(QuestProgress quest) {
        if ("All".equals(tab)) {
            return true;
        }
        return quest.getDef().getCategory() == QuestCategory.valueOf(tab.toUpperCase());
    }

    private Comparator<QuestProgress> order() {
        return new Comparator<QuestProgress>() {
            @Override
            public int compare(QuestProgress a, QuestProgress b) {
                if (a.isClaimed() != b.isClaimed()) {
                    return a.isClaimed() ? 1 : -1;
                }
                if (a.isCompleted() != b.isCompleted()) {
                    return a.isCompleted() ? -1 : 1;
                }
                int result;
                if (byProgress) {
                    result = Double.compare(ratio(b), ratio(a));
                } else {
                    result = Integer.compare(b.getDef().getPriority().getPrioritynum(),
                            a.getDef().getPriority().getPrioritynum());
                }
                return ascending ? -result : result;
            }
        };
    }

    private static double ratio(QuestProgress quest) {
        return quest.getTarget() <= 0 ? 0d : quest.getProgress() / quest.getTarget();
    }

    private int finishedCount() {
        if (context.user() == null) {
            return 0;
        }
        if ("All".equals(tab)) {
            return model.quest.QuestTally.total(context.user());
        }
        return model.quest.QuestTally.finished(context.user(),
                QuestCategory.valueOf(tab.toUpperCase()));
    }

    private Table cornerLayer() {
        Table layer = new Table();
        layer.bottom().left();
        com.badlogic.gdx.graphics.g2d.TextureRegion art = context.pam() == null ? null
                : context.pam().region("IMAGE_UI_QUESTS_TRAVEL_LOG_CORNER_NORANK");
        if (art == null) {
            return layer;
        }

        final float hidden = 0f;
        final float shown = CORNER_HEIGHT * 0.25f;

        Group nest = new Group();
        nest.setSize(CORNER_WIDTH, CORNER_HEIGHT);
        nest.setTransform(false);

        Image shadow = new Image(new TextureRegionDrawable(art));
        shadow.setScaling(Scaling.fit);
        shadow.setColor(0f, 0f, 0f, 0.45f);
        shadow.setSize(CORNER_WIDTH + SHADOW_SPREAD, CORNER_HEIGHT + SHADOW_SPREAD);

        final Image mate = new Image(new TextureRegionDrawable(art));
        mate.setScaling(Scaling.fit);
        mate.setSize(CORNER_WIDTH, CORNER_HEIGHT);
        mate.setPosition(0f, hidden);
        shadow.setPosition(-SHADOW_SPREAD / 2f, hidden - SHADOW_SPREAD / 2f);

        nest.addActor(shadow);
        nest.addActor(mate);
        peek(mate, shadow, hidden, shown);

        layer.add(nest).size(CORNER_WIDTH, CORNER_HEIGHT)
                .padLeft(-Theme.PAD_LARGE)
                .padBottom(-(CORNER_HEIGHT - CORNER_PEEK));
        return layer;
    }

    private void peek(final Image mate, final Image shadow, final float hidden,
            final float shown) {
        mate.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor from) {
                if (pointer == -1) {
                    slide(mate, shadow, shown);
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor to) {
                if (pointer == -1) {
                    slide(mate, shadow, hidden);
                }
            }

            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x,
                    float y, int pointer, int button) {
                context.toasts().info("He has nothing to say yet.");
                return true;
            }
        });
    }

    private void slide(Image mate, Image shadow, float target) {
        mate.clearActions();
        shadow.clearActions();
        mate.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(
                mate.getX(), target, 0.22f,
                com.badlogic.gdx.math.Interpolation.pow2Out));
        shadow.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo(
                shadow.getX(), target - SHADOW_SPREAD / 2f, 0.22f,
                com.badlogic.gdx.math.Interpolation.pow2Out));
    }

    @Override
    public void show() {
        super.show();
        if (list != null) {
            uiStage().setScrollFocus(list);
        }
    }
}

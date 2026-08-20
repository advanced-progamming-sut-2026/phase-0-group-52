package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.Leaderboard;
import model.User;
import view.gui.BaseScreen;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.List;

public final class LeaderboardScreen extends BaseScreen {
    private static final String[] COLUMNS = {"score", "level", "minigames", "daily", "quests"};
    private static final String[] HEADINGS = {"Score", "Prog", "Mini", "Daily", "Quests"};

    private static final float PAPER_WIDTH = 820f;
    private static final float ROLL_OVERHANG = 300f / 165f;
    private static final float CENTRE_SHARE = 2f / 3f;
    private static final float ROLL_RATIO = 100f / 300f;
    private static final float SEAM_OVERLAP = 0.30f;
    private static final float SHADOW_OFFSET = 14f;
    private static final float SHADOW_ALPHA = 0.55f;
    private static final int SHADOW_LAYERS = 3;
    private static final float CROWN_TILT = -28f;
    private static final float NAME_WIDTH = 170f;
    private static final float VALUE_WIDTH = 96f;
    private static final float RANK_MARK = 52f;
    private static final float ROW_HEIGHT = 54f;

    private String sortColumn = "score";
    private boolean ascending;

    public LeaderboardScreen(GameContext context) {
        super(context, "Leaderboard");
    }

    @Override
    protected view.gui.TopBar.Section section() {
        return view.gui.TopBar.Section.LEADERBOARD;
    }

    @Override
    protected boolean scrollContent() {
        return false;
    }

    @Override
    protected String backdropImage() {
        return "assets/backgrounds/leaderboard.png";
    }

    @Override
    protected void build() {
        content.clearChildren();

        float paper = PAPER_WIDTH;
        float paperHeight = Theme.WORLD_HEIGHT;
        float centre = Theme.WORLD_WIDTH * CENTRE_SHARE;

        Stack scroll = new Stack();
        scroll.add(shadowLayer(paper, paperHeight));
        scroll.add(paperLayer(paper, paperHeight));

        content.add(new Table()).width(centre - paper / 2f - Theme.PAD_LARGE);
        content.add(scroll).width(paper).growY();
        content.add(new Table()).expandX();
    }

    private Table paperLayer(float paper, float paperHeight) {
        Stack stack = new Stack();
        Image parchment = new Image(parchmentSlice(paper, paperHeight));
        parchment.setScaling(Scaling.stretch);
        stack.add(parchment);
        stack.add(listLayer(paper));

        Table layer = new Table();
        layer.top();
        layer.add(stack).width(paper).height(paperHeight);
        return layer;
    }

    private Table listLayer(float paper) {
        Table inner = new Table();
        inner.top();
        inner.pad(Theme.PAD_LARGE, paper * 0.075f, Theme.PAD, paper * 0.075f);
        inner.add(headerRow()).growX().row();
        Table rule = new Table();
        rule.setBackground(ui.primitives().flat(Theme.alpha(Theme.INK, 0.55f)));
        inner.add(rule).height(3f).growX().padTop(4f).padBottom(6f).row();
        inner.add(rankList()).grow();
        return inner;
    }

    private com.badlogic.gdx.scenes.scene2d.utils.Drawable parchmentSlice(
            float width, float paperHeight) {
        com.badlogic.gdx.graphics.g2d.TextureRegion full =
                ui.regionFile("assets/ui/scroll_mid.png");
        if (full == null) {
            return ui.drawable("scrollMid");
        }
        float scale = full.getRegionWidth() / width;
        int needed = Math.min(full.getRegionHeight(), Math.round(paperHeight * scale));
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(
                        full, 0, 0, full.getRegionWidth(), needed));
    }

    private Stack shadowLayer(float paper, float paperHeight) {
        Stack layers = new Stack();
        for (int i = 1; i <= SHADOW_LAYERS; i++) {
            layers.add(shadowPass(paper, paperHeight, SHADOW_OFFSET * i / SHADOW_LAYERS));
        }
        return layers;
    }

    private Table shadowPass(float paper, float paperHeight, float offset) {
        Image parchment = new Image(parchmentSlice(paper, paperHeight));
        parchment.setScaling(Scaling.stretch);
        parchment.setColor(0f, 0f, 0f, SHADOW_ALPHA / SHADOW_LAYERS);

        Table layer = new Table();
        layer.top();
        layer.add(parchment).width(paper).height(paperHeight)
                .padLeft(offset).padTop(offset)
                .padRight(-offset).padBottom(-offset);
        return layer;
    }

    private Table headerRow() {
        Table header = new Table();
        header.defaults().pad(1f);
        Label hash = new Label("#", ui.skin(), "rowValue");
        hash.setAlignment(Align.center);
        header.add(hash).width(RANK_MARK).center();
        header.add(new Label("Player", ui.skin(), "rowValue")).left().width(NAME_WIDTH);
        for (int i = 0; i < COLUMNS.length; i++) {
            header.add(sortHeader(HEADINGS[i], COLUMNS[i])).width(VALUE_WIDTH);
        }
        return header;
    }

    private Table sortHeader(String label, final String column) {
        Table cell = new Table();
        cell.add(new Label(label, ui.skin(), "rowValue")).center();
        if (column.equals(sortColumn)) {
            Image arrow = new Image(ui.drawable(ascending ? "sortAscending" : "sortDescending"));
            arrow.setScaling(Scaling.fit);
            cell.add(arrow).size(18f).padLeft(3f);
        }
        view.gui.Animations.attachPress(cell);
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                if (column.equals(sortColumn)) {
                    ascending = !ascending;
                } else {
                    sortColumn = column;
                    ascending = false;
                }
                build();
            }
        });
        return cell;
    }

    private ScrollPane rankList() {
        Table list = new Table();
        list.top();
        list.defaults().growX();

        List<Leaderboard.Entry> entries = new Leaderboard().getEntries(sortColumn, ascending);
        if (entries.isEmpty()) {
            list.add(new Label("No players registered yet.", ui.skin(), "rowSub"))
                    .left().pad(Theme.PAD).row();
        } else {
            int rank = 1;
            for (Leaderboard.Entry entry : entries) {
                list.add(row(rank++, entry)).height(ROW_HEIGHT).row();
            }
        }

        ScrollPane scroll = new ScrollPane(list, ui.skin());
        scroll.setStyle(ui.skin().get("bare", ScrollPane.ScrollPaneStyle.class));
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, true);
        scroll.setForceScroll(false, true);
        UiKit.focusOnHover(scroll);
        return scroll;
    }

    private Table row(int rank, Leaderboard.Entry entry) {
        User user = entry.getUser();
        boolean isMe = context.user() != null
                && context.user().getUsername() != null
                && context.user().getUsername().equals(user.getUsername());
        String valueStyle = isMe ? "rowNameMe" : "rowSub";
        String nameStyle = isMe ? "rowNameSelected" : "rowNameBig";

        Table row = new Table();
        row.defaults().pad(1f);
        row.add(rankMark(rank)).size(RANK_MARK);

        Table name = new Table();
        name.left();
        name.add(new Label(user.getNickname(), ui.skin(), nameStyle)).left().row();
        if (isMe) {
            Table rule = new Table();
            rule.setBackground(ui.primitives().flat(Theme.INK_SELECTED));
            name.add(rule).height(3f).growX().padTop(1f);
        }
        row.add(name).left().width(NAME_WIDTH);

        addValue(row, String.valueOf(user.getMaxPoint()), valueStyle);
        addValue(row, user.getLastChapter() + "-" + user.getLastLevel(), valueStyle);
        addValue(row, String.valueOf(user.getMiniGamesPlayed()), valueStyle);
        addValue(row, String.valueOf(user.getQuestDailyNum()), valueStyle);
        addValue(row, String.valueOf(user.getQuestNonDailyNum()), valueStyle);
        return row;
    }

    private void addValue(Table row, String text, String style) {
        Label label = new Label(text, ui.skin(), style);
        label.setAlignment(Align.center);
        row.add(label).width(VALUE_WIDTH).center();
    }

    private Table rankMark(int rank) {
        Stack stack = new Stack();

        String banner = null;
        if (rank == 1) {
            banner = "rankFirst";
        } else if (rank == 2) {
            banner = "rankSecond";
        } else if (rank == 3) {
            banner = "rankThird";
        }

        if (banner != null) {
            Image art = new Image(ui.drawable(banner));
            art.setScaling(Scaling.fit);
            stack.add(art);
        }

        Table number = new Table();
        Label label = new Label(String.valueOf(rank), ui.skin(),
                banner == null ? "rowSub" : "rankNumber");
        label.setAlignment(Align.center);
        number.add(label).center().padTop(banner == null ? 0f : RANK_MARK * 0.5f);
        stack.add(number);

        if (rank == 1) {
            stack.add(tiltedCrown());
        }

        Table mark = new Table();
        mark.add(stack).size(RANK_MARK);
        return mark;
    }

    private Table tiltedCrown() {
        Image crown = new Image(ui.drawable("rankCrown")) {
            @Override
            public void layout() {
                super.layout();
                setOrigin(Align.center);
            }
        };
        crown.setScaling(Scaling.fit);
        crown.setRotation(CROWN_TILT);

        Table holder = new Table();
        holder.top().left();
        holder.add(crown).width(RANK_MARK * 0.72f).height(RANK_MARK * 0.44f)
                .padTop(-RANK_MARK * 0.12f);
        return holder;
    }
}

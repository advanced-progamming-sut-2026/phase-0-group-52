package view.gui.screens;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;
import view.gui.Assets;
import view.gui.GameContext;
import view.gui.Theme;
import view.gui.UiKit;
import view.gui.widgets.HeadSwapActor;
import view.gui.widgets.PamActor;
import view.gui.widgets.ZombieCard;

import java.util.List;

public final class ZombieAlmanac {

    private static final float CARD_SCALE = 0.78f;
    private static final float CARD_GAP = 6f;
    private static final float STAGE_COVERAGE = 1f;
    private static final java.util.Set<String> HIDDEN_CLIPS =
            new java.util.HashSet<String>(java.util.Arrays.asList("particles"));
    private static final float ARROW = 40f;
    private static final float BAR_HEIGHT = 14f;
    private static final float BAR_WIDTH = 210f;
    private static final float RATING_WIDTH = 300f;
    private static final int TURF_CROP = 210;
    private static final float BAR_INSET = 2f;
    private static final int CAP = 6;
    private static final float VALUE_SCALE = 1.45f;
    private static final com.badlogic.gdx.graphics.Color BAND =
            new com.badlogic.gdx.graphics.Color(0.33f, 0.29f, 0.55f, 1f);
    private static final com.badlogic.gdx.graphics.Color TILE =
            new com.badlogic.gdx.graphics.Color(0.21f, 0.19f, 0.36f, 1f);
    private static final float STAT_ICON = 62f;
    private static final float NAME_HEIGHT = 46f;

    private final GameContext context;
    private final UiKit ui;
    private final Runnable onChange;

    private final view.gui.widgets.ZombieFilterPopup.Rules rules =
            new view.gui.widgets.ZombieFilterPopup.Rules();
    private ZombieRecord selected;
    private int clipIndex;
    private float scrollX;
    private ScrollPane strip;
    private PamActor stageActor;
    private HeadSwapActor headActor;
    private Label stateLabel;

    public ZombieAlmanac(GameContext context, Runnable onChange) {
        this.context = context;
        this.ui = context.ui();
        this.onChange = onChange;
        List<ZombieRecord> all = ZombieData.all();
        if (!all.isEmpty()) {
            selected = all.get(0);
        }
    }

    private Assets art() {
        return context.assets();
    }

    private Drawable regionOf(String id) {
        Assets assets = art();
        if (assets == null) {
            return null;
        }
        com.badlogic.gdx.graphics.g2d.TextureRegion region = assets.region(id);
        return region == null ? null
                : new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(region);
    }

    private boolean seen(ZombieRecord record) {
        return context.settings().isDebugMode()
                || ZombieData.isSeen(context.user(), record);
    }

    public void rememberScroll() {
        if (strip != null) {
            scrollX = strip.getScrollX();
        }
    }

    public Table stagePanel() {
        Table frame = new Table();
        frame.setBackground(ui.primitives().rounded(24, Theme.PANEL_SUNKEN,
                Theme.OUTLINE_SOFT, 3));
        frame.pad(4f);

        Stack stack = new Stack();
        Drawable ground = turfPatch();
        if (ground != null) {
            Table turfLayer = new Table();
            turfLayer.setBackground(ground);
            stack.add(turfLayer);
        }
        addPerformer(stack);
        Table arrows = new Table();
        arrows.add(arrow(false)).size(ARROW).left().expandX();
        arrows.add(arrow(true)).size(ARROW).right().expandX();
        stack.add(arrows);

        stateLabel = new Label(currentClip(), ui.skin(), "smallOnDark");
        stateLabel.setAlignment(Align.center);
        Table caption = new Table();
        caption.bottom();
        caption.add(stateLabel).expandX().center().padBottom(2f);
        stack.add(caption);
        frame.add(stack).grow();

        Table wrap = new Table();
        wrap.add(titleBar()).growX().height(NAME_HEIGHT).row();
        wrap.add(frame).grow();
        return wrap;
    }

    private Drawable turfPatch() {
        com.badlogic.gdx.graphics.g2d.TextureRegion base =
                art() == null ? null : art().region(turf());
        if (base == null) {
            return ui.primitives().flat(Theme.PANEL_SUNKEN);
        }
        int w = Math.min(base.getRegionWidth(), TURF_CROP);
        int h = Math.min(base.getRegionHeight(), TURF_CROP);
        int x = base.getRegionX() + (base.getRegionWidth() - w) / 2;
        int y = base.getRegionY() + (base.getRegionHeight() - h) / 2;
        return new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(base.getTexture(), x, y, w, h));
    }

    private String turf() {
        String chapter = selected == null ? "" : selected.getChapter();
        if ("ANCIENT_EGYPT".equals(chapter)) {
            return "IMAGE_BACKGROUNDS_EGYPT_TEXTURE";
        }
        if ("FROSTBITE_CAVES".equals(chapter)) {
            return "IMAGE_BACKGROUNDS_ICEAGE_TEXTURE";
        }
        if ("BIG_WAVE_BEACH".equals(chapter)) {
            return "IMAGE_BACKGROUNDS_BEACH_TEXTURE";
        }
        if ("DARK_AGES".equals(chapter)) {
            return "IMAGE_BACKGROUNDS_DARK_TEXTURE";
        }
        if ("ZOMBOSS".equals(chapter)) {
            return "IMAGE_BACKGROUNDS_RIFT_TEXTURE";
        }
        return "IMAGE_BACKGROUNDS_FRONTLAWN_TEXTURE";
    }

    private java.util.Map<String, Boolean> wornParts() {
        if (selected == null || selected.getArmorParts().isEmpty()) {
            return null;
        }
        java.util.Map<String, Boolean> worn = new java.util.HashMap<String, Boolean>();
        for (String part : selected.getArmorParts()) {
            worn.put(part, Boolean.TRUE);
        }
        return worn;
    }

    private void addPerformer(Stack stack) {
        stageActor = null;
        headActor = null;
        if (selected == null || art() == null) {
            return;
        }
        Table holder = new Table();
        if (selected.isComposite()) {
            HeadSwapActor swap = new HeadSwapActor(art(), selected.getBodyPath(),
                    selected.getHeadPath(), currentClip(), selected.getHideParts());
            if (!swap.isReady()) {
                return;
            }
            headActor = swap;
            holder.add(swap).grow();
        } else if (selected.hasAnimation()) {
            PamActor actor = new PamActor(art(), selected.getAnimationPath(), currentClip())
                    .setFit(true)
                    .setCoverage(STAGE_COVERAGE)
                    .setClipped(true)
                    .setParts(wornParts());
            com.badlogic.gdx.math.Rectangle box =
                    art().player().bounds(selected.getAnimationPath(), currentClip());
            if (box != null) {
                actor.setExtent(box.x, -(box.y + box.height), box.width, box.height);
            }
            if (!actor.isReady()) {
                return;
            }
            stageActor = actor;
            holder.add(actor).grow();
        } else {
            return;
        }
        stack.add(holder);
    }

    private Table titleBar() {
        Table bar = new Table();
        bar.setBackground(ui.primitives().rounded(12, Theme.PANEL_SUNKEN, Theme.OUTLINE_SOFT, 2));
        Label name = new Label(selected == null ? "" : selected.getName(),
                ui.skin(), "titleOnDark");
        name.setEllipsis(true);
        name.setAlignment(Align.center);
        bar.add(name).growX().minWidth(0f).center();
        return bar;
    }

    private String currentClip() {
        if (selected == null || selected.getClips().isEmpty()) {
            return "idle";
        }
        List<String> clips = browsable();
        if (clips.isEmpty()) {
            return "idle";
        }
        int size = clips.size();
        int start = defaultClip(clips);
        int at = Math.max(0, start) + clipIndex;
        return clips.get(((at % size) + size) % size);
    }

    private int defaultClip(List<String> clips) {
        int plain = -1;
        for (int i = 0; i < clips.size(); i++) {
            String clip = clips.get(i);
            if ("idle_norm".equals(clip)) {
                return i;
            }
            if (clip.startsWith("idle_")) {
                return i;
            }
            if ("idle".equals(clip) && plain < 0) {
                plain = i;
            }
        }
        if (plain >= 0) {
            return plain;
        }
        int walk = clips.indexOf("walk");
        return walk < 0 ? 0 : walk;
    }

    private List<String> browsable() {
        List<String> out = new java.util.ArrayList<String>();
        for (String clip : selected.getClips()) {
            if (!HIDDEN_CLIPS.contains(clip)) {
                out.add(clip);
            }
        }
        return out;
    }

    private Table arrow(final boolean forward) {
        Table cell = new Table();
        Drawable face = regionOf(forward
                ? "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_NEXT"
                : "IMAGE_UI_ALMANAC_STATS_SCREEN_NAV_ARROW_PREVIOUS");
        if (face != null) {
            Image mark = new Image(face);
            mark.setScaling(Scaling.fit);
            cell.add(mark).grow();
        } else {
            cell.add(new Label(forward ? ">" : "<", ui.skin(), "rowHeader"));
        }
        UiKit.onClick(cell, new Runnable() {
            @Override
            public void run() {
                clipIndex += forward ? 1 : -1;
                showClip();
            }
        });
        return cell;
    }

    private void showClip() {
        String clip = currentClip();
        if (stateLabel != null) {
            stateLabel.setText(clip);
        }
        if (stageActor != null) {
            stageActor.play(clip, true, null);
        }
        if (headActor != null) {
            headActor.play(clip);
        }
    }

    public Table statsPanel() {
        Table box = new Table();
        box.top().left();
        box.defaults().growX();
        if (selected == null) {
            return box;
        }
        Table ratings = new Table();
        ratings.left();
        ratings.setBackground(ui.primitives().rounded(14, BAND,
                Theme.darken(BAND, 0.18f), 2));
        ratings.pad(Theme.PAD_SMALL, Theme.PAD, Theme.PAD_SMALL, Theme.PAD);
        ratings.add(ratingRow("TOUGHNESS", "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIETOUGHNESS_ICON",
                selected.getToughness())).width(RATING_WIDTH).left();
        ratings.add(ratingRow("SPEED", "IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIESPEED_ICON",
                selected.getSpeed())).width(RATING_WIDTH).left().padLeft(Theme.PAD);
        box.add(ratings).growX().padBottom(Theme.PAD).row();

        Label body = new Label(selected.getDescription(), ui.skin(), "almanacBody");
        body.setWrap(true);
        box.add(body).growX().minWidth(0f).left().padTop(Theme.PAD_SMALL).row();

        if (!selected.getFlavor().isEmpty()) {
            Label joke = new Label(selected.getFlavor(), ui.skin(), "story");
            joke.setWrap(true);
            box.add(joke).growX().minWidth(0f).left().padTop(Theme.PAD).row();
        }
        return box;
    }

    private Table ratingRow(String caption, String iconId, ZombieRecord.Rating rating) {
        Table row = new Table();
        row.left().top();
        Drawable icon = regionOf(iconId);
        if (icon != null) {
            Table tile = new Table();
            tile.setBackground(ui.primitives().rounded(10, TILE,
                    Theme.darken(TILE, 0.3f), 2));
            Image mark = new Image(icon);
            mark.setScaling(Scaling.fit);
            tile.add(mark).grow().pad(4f);
            row.add(tile).size(STAT_ICON).top().padRight(Theme.PAD_SMALL);
        }
        Table text = new Table();
        text.left().top();
        Label caps = new Label(caption, ui.skin(), "zombieStatLabel");
        text.add(caps).left().padTop(UiKit.opticalPad(caps)).row();
        Label value = new Label(rating.getLabel(), ui.skin(), "statValue");
        value.setFontScale(VALUE_SCALE);
        text.add(value).left().padTop(-1f).row();
        text.add(bar(rating)).width(BAR_WIDTH).height(BAR_HEIGHT).left().padTop(3f);
        row.add(text).left().top();
        return row;
    }

    private view.gui.widgets.XpBar bar(ZombieRecord.Rating rating) {
        Drawable trough = capsule("IMAGE_UI_ALMANAC_ZOMBIES_ZOMBIE_FUELBAR");
        if (trough == null) {
            trough = ui.primitives().rounded(6, Theme.PANEL_SUNKEN, Theme.OUTLINE_SOFT, 1);
        }
        Drawable fill = capsule("IMAGE_UI_ALMANAC_GENERAL_FUELBAR_FILL");
        if (fill == null) {
            fill = ui.primitives().rounded(5, Theme.SUN, Theme.darken(Theme.SUN, 0.3f), 1);
        }
        return new view.gui.widgets.XpBar(trough, fill, rating.ratio(), BAR_INSET);
    }

    private Drawable capsule(String id) {
        com.badlogic.gdx.graphics.g2d.TextureRegion region =
                art() == null ? null : art().region(id);
        if (region == null) {
            return null;
        }
        int cap = Math.max(1, Math.min(CAP, region.getRegionWidth() / 2 - 1));
        int lid = Math.max(1, Math.min(CAP, region.getRegionHeight() / 2 - 1));
        com.badlogic.gdx.graphics.g2d.NinePatch patch =
                new com.badlogic.gdx.graphics.g2d.NinePatch(region, cap, cap, lid, lid);
        patch.setPadding(0f, 0f, 0f, 0f);
        return new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(patch);
    }

    public Table cardStrip() {
        final Table row = new Table();
        row.left();
        for (final ZombieRecord record : visible()) {
            final ZombieCard card = new ZombieCard(ui, art(), record, CARD_SCALE);
            card.setSeen(seen(record));
            card.setSelected(record == selected);
            UiKit.onClick(card, new Runnable() {
                @Override
                public void run() {
                    pick(record);
                }
            });
            row.add(card).size(card.getPrefWidth(), card.getPrefHeight()).padRight(CARD_GAP);
        }
        strip = new ScrollPane(row, ui.skin());
        strip.setFadeScrollBars(false);
        strip.setScrollingDisabled(false, true);
        strip.setOverscroll(false, false);
        UiKit.focusOnHover(strip);
        strip.layout();
        strip.setScrollX(scrollX);
        strip.updateVisualScroll();

        Table holder = new Table();
        holder.add(strip).grow();
        return holder;
    }

    public List<ZombieRecord> visible() {
        return rules.apply(context.user(), context.settings().isDebugMode());
    }

    public view.gui.widgets.ZombieFilterPopup.Rules rules() {
        return rules;
    }

    private void pick(ZombieRecord record) {
        if (record != selected) {
            selected = record;
            clipIndex = 0;
        } else if (context.settings().isDebugMode() && context.user() != null) {
            model.Result unlocked = new controller.menu.CollectionMenuController(context.app())
                    .unlockZombie(record.getAlias());
            context.toasts().info(unlocked.message());
        }
        rememberScroll();
        onChange.run();
    }

    public ScrollPane scroller() {
        return strip;
    }
}

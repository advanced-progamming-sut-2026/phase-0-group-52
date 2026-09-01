package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import view.gui.Assets;
import view.gui.PacketLayout;
import view.gui.Theme;
import view.gui.UiKit;

public final class SeedPacket extends WidgetGroup {

    public enum Mode { ALMANAC, GAME }

    public static final float ART_W = 119f;
    public static final float ART_H = 75f;
    private static final float BADGE_SCALE = 0.62f;
    private static final float LOCK_SCALE = 1.75f;
    private static final float BANNER_SCALE = 0.58f;
    private static final float BADGE_OVERHANG = 0.35f;
    private static final float TAB_SCALE = 1.12f;
    private static final float COST_FONT = 0.9f;
    private static final float COST_INSET = 5f;
    private static final float BOOST_COVERAGE = 1.18f;
    private static final float LOCKED_DIM = 0.32f;
    private static final float SELECTED_DIM = 0.48f;
    private static final float INSET = 3f;
    private static final float FRAME_LEFT = 5f;
    private static final float FRAME_BOTTOM = 8f;

    private static final class ClipGroup extends WidgetGroup {
        @Override
        public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
            validate();
            batch.flush();
            if (clipBegin()) {
                super.draw(batch, parentAlpha);
                batch.flush();
                clipEnd();
            }
        }
    }

    private final UiKit ui;
    private final Assets assets;
    private final Plants plant;
    private final PlantRecord record;
    private final float scale;

    private Actor background;
    private ClipGroup iconClip;
    private Image icon;
    private Actor mark;
    private Image banner;
    private Image badgeIcon;
    private Image lock;
    private Actor border;
    private Table priceTab;
    private Label costLabel;

    private float markWidth;
    private float markHeight;
    private float badgeWidth;
    private float badgeHeight;
    private float lockWidth;
    private float lockHeight;
    private float tabWidth;
    private float tabHeight;
    private float borderWidth;
    private float borderHeight;

    private int level = 1;
    private boolean locked;
    private boolean boosted;
    private boolean selected;
    private boolean hovered;

    public SeedPacket(UiKit ui, Assets assets, Plants plant) {
        this(ui, assets, plant, Mode.ALMANAC, 1f);
    }

    public SeedPacket(UiKit ui, Assets assets, Plants plant, Mode mode, float scale) {
        this.ui = ui;
        this.assets = assets;
        this.plant = plant;
        this.record = PlantData.record(plant);
        this.scale = scale;

        setSize(width(), height());
        setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        rebuild();

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor from) {
                if (pointer == -1) {
                    hovered = true;
                    refreshBorder();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor to) {
                if (pointer == -1) {
                    hovered = false;
                    refreshBorder();
                }
            }
        });
    }

    public float width() {
        return ART_W * scale;
    }

    public float height() {
        return ART_H * scale;
    }

    private TextureRegion region(String id) {
        if (id == null || assets == null) {
            return null;
        }
        return assets.region(id);
    }

    private void rebuild() {
        clearChildren();
        background = buildBackground();
        addActor(background);

        icon = buildIcon();
        if (icon != null) {
            iconClip = new ClipGroup();
            iconClip.addActor(icon);
            addActor(iconClip);
        } else {
            iconClip = null;
        }
        mark = buildMark();
        if (mark != null) {
            addActor(mark);
        }
        lock = buildLock();
        if (lock != null) {
            addActor(lock);
        }
        priceTab = buildPriceTab();
        if (priceTab != null) {
            addActor(priceTab);
        }
        refreshBorder();
        applyDim();
        for (Actor child : getChildren()) {
            child.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        }
        invalidate();
    }

    private Actor buildBackground() {
        if (boosted && assets != null) {
            PamActor anim = new PamActor(assets, Assets.BOOSTCARD_ANIM, "animation")
                    .setFit(false)
                    .setCoverage(BOOST_COVERAGE)
                    .setClipped(true);
            if (anim.isReady()) {
                return anim;
            }
        }
        TextureRegion art = region(record == null ? null : record.getPacketBackground());
        if (art != null) {
            return new Image(new TextureRegionDrawable(art));
        }
        Table fallback = new Table();
        fallback.setBackground(ui.primitives().rounded(6,
                Theme.plantFamily(plant.getCategory().name()), Theme.OUTLINE, 2));
        return fallback;
    }

    private Image buildIcon() {
        TextureRegion art = region(record == null ? null : record.getPacketIcon());
        if (art == null) {
            return null;
        }
        return new Image(new TextureRegionDrawable(art));
    }

    private Actor buildMark() {
        TextureRegion badge = region(record == null ? null : record.getCategoryBadge());
            if (badge == null) {
                return null;
            }
            TextureRegion bannerArt = region("IMAGE_UI_PACKETS_MINTFAM_BANNER");
            WidgetGroup group = new WidgetGroup();
            if (bannerArt != null) {
                banner = new Image(new TextureRegionDrawable(bannerArt));
                banner.setColor(Theme.plantFamily(plant.getCategory().name()));
                group.addActor(banner);
                markWidth = bannerArt.getRegionWidth() * BANNER_SCALE;
                markHeight = bannerArt.getRegionHeight() * BANNER_SCALE;
            } else {
                markWidth = badge.getRegionWidth() * BADGE_SCALE;
                markHeight = badge.getRegionHeight() * BADGE_SCALE;
            }
            badgeIcon = new Image(new TextureRegionDrawable(badge));
            badgeWidth = badge.getRegionWidth() * BADGE_SCALE;
            badgeHeight = badge.getRegionHeight() * BADGE_SCALE;
            group.addActor(badgeIcon);
            return group;
    }

    private Image buildLock() {
        if (!locked) {
            return null;
        }
        TextureRegion art = region("IMAGE_UI_PACKETS_LOCK_SMALL");
        if (art == null) {
            return null;
        }
        Image image = new Image(new TextureRegionDrawable(art));
        lockWidth = art.getRegionWidth() * LOCK_SCALE;
        lockHeight = art.getRegionHeight() * LOCK_SCALE;
        return image;
    }

    private Table buildPriceTab() {
        if (locked) {
            return null;
        }
        String id = premium() ? "IMAGE_UI_PACKETS_PRICE_TAB_PREMIUM" : "IMAGE_UI_PACKETS_PRICE_TAB";
        TextureRegion art = region(id);
        Table tab = new Table();
        if (art != null) {
            tab.setBackground(new TextureRegionDrawable(art));
        }
        costLabel = new Label(String.valueOf(cost()), ui.skin(), "packetCost");
        costLabel.setAlignment(Align.right);
        costLabel.setFontScale(COST_FONT);
        tab.add(costLabel).right().expandX()
                .padRight(COST_INSET).padBottom(0f);
        tabWidth = art == null ? 51f : art.getRegionWidth();
        tabHeight = art == null ? 36f : art.getRegionHeight();
        return tab;
    }

    private boolean premium() {
        return record != null
                && "IMAGE_UI_PACKETS_READY_PREMIUM".equals(record.getPacketBackground());
    }

    private int cost() {
        return PlantData.effectiveCost(plant, level);
    }

    private void refreshBorder() {
        if (border != null) {
            border.remove();
            border = null;
        }
        if (!selected && !hovered) {
            return;
        }
        TextureRegion art = region(selected
                ? "IMAGE_UI_PACKETS_SELECT" : "IMAGE_UI_ALMANAC_PLANT_SELECT_PKT");
        if (art != null) {
            border = new Image(new TextureRegionDrawable(art));
            borderWidth = art.getRegionWidth();
            borderHeight = art.getRegionHeight();
        } else {
            Table ring = new Table();
            ring.setBackground(ui.primitives().rounded(6, new Color(0, 0, 0, 0),
                    selected ? Theme.SELECTED : Theme.SUN, 3));
            border = ring;
            borderWidth = ART_W;
            borderHeight = ART_H;
        }
        border.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        addActor(border);
        if (mark != null) {
            mark.toFront();
        }
        invalidate();
    }

    private void applyDim() {
        boolean dark = locked || unaffordable;
        float shade = dark ? LOCKED_DIM : 1f;
        float chrome = dark ? LOCKED_DIM : selected ? SELECTED_DIM : 1f;
        paint(background, chrome);
        paint(priceTab, chrome);
        paint(icon, shade);
        paint(badgeIcon, shade);
        if (banner != null) {
            Color base = Theme.plantFamily(plant.getCategory().name());
            banner.setColor(base.r * shade, base.g * shade, base.b * shade, 1f);
        }
        if (locked) {
            paint(border, chrome);
        }
    }

    private static void paint(Actor actor, float shade) {
        if (actor != null) {
            actor.setColor(shade, shade, shade, 1f);
        }
    }

    @Override
    public void layout() {
        float s = getWidth() / ART_W;
        float cardH = ART_H * s;

        if (background != null) {
            background.setBounds(0f, 0f, getWidth(), cardH);
        }
        if (iconClip != null) {
            PacketLayout.Placement place = PacketLayout.of(plant);
            icon.setSize(record.getIconWidth() * place.getScale() * s,
                    record.getIconHeight() * place.getScale() * s);
            float left = FRAME_LEFT * s;
            float bottom = FRAME_BOTTOM * s;
            iconClip.setBounds(left, bottom, getWidth() * 3f, cardH * 4f);
            icon.setPosition(place.getX() * s - left,
                    cardH - place.getY() * s - icon.getHeight() - bottom);
        }
        if (mark != null) {
            mark.setSize(markWidth * s, markHeight * s);
            mark.setPosition(-mark.getWidth() * BADGE_OVERHANG,
                    cardH - mark.getHeight() * (1f - BADGE_OVERHANG));
            if (banner != null) {
                banner.setBounds(0f, 0f, mark.getWidth(), mark.getHeight());
            }
            if (badgeIcon != null) {
                badgeIcon.setSize(badgeWidth * s, badgeHeight * s);
                badgeIcon.setPosition((mark.getWidth() - badgeIcon.getWidth()) / 2f,
                        (mark.getHeight() - badgeIcon.getHeight()) / 2f);
            }
        }
        if (lock != null) {
            lock.setSize(lockWidth * s, lockHeight * s);
            lock.setPosition(getWidth() - lock.getWidth() - INSET * s,
                    FRAME_BOTTOM * s * 0.5f);
        }
        if (border != null) {
            border.setSize(borderWidth * s, borderHeight * s);
            border.setPosition((getWidth() - border.getWidth()) / 2f,
                    (cardH - border.getHeight()) / 2f);
        }
        if (priceTab != null) {
            priceTab.setSize(tabWidth * TAB_SCALE * s, tabHeight * TAB_SCALE * s);
            priceTab.setPosition(getWidth() - priceTab.getWidth() - INSET * s,
                    INSET * s);
        }
    }

    private static final com.badlogic.gdx.graphics.Color COST_DENIED =
            new com.badlogic.gdx.graphics.Color(1f, 0.13f, 0.11f, 1f);
    private static final com.badlogic.gdx.graphics.Color RECHARGE_SHADE =
            new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.62f);

    private static final float BADGE_BLEED = 0.6f;

    private float recharge;
    private boolean affordable = true;
    private boolean unaffordable;

    public SeedPacket setLocked(boolean value) {
        if (this.locked != value) {
            this.locked = value;
            rebuild();
        }
        return this;
    }

    public SeedPacket setBoosted(boolean value) {
        if (this.boosted != value) {
            this.boosted = value;
            rebuild();
        }
        return this;
    }

    public SeedPacket setSelected(boolean value) {
        if (this.selected != value) {
            this.selected = value;
            refreshBorder();
            applyDim();
        }
        return this;
    }

    public SeedPacket setLevel(int value) {
        if (this.level != value) {
            this.level = Math.max(1, value);
            rebuild();
        }
        return this;
    }

    public SeedPacket setCooldown(float secondsRemaining) {
        return this;
    }

    public SeedPacket setAffordable(boolean value) {
        affordable = value;
        if (unaffordable == !value) {
            return this;
        }
        unaffordable = !value;
        applyDim();
        if (costLabel != null) {
            costLabel.setText(String.valueOf(plant.getCost()));
            costLabel.setColor(value ? com.badlogic.gdx.graphics.Color.WHITE : COST_DENIED);
        }
        return this;
    }

    public SeedPacket setRecharge(float remaining) {
        recharge = Math.max(0f, Math.min(1f, remaining));
        return this;
    }

    public boolean isBlocked() {
        return recharge > 0f || !affordable;
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        if (recharge <= 0f) {
            return;
        }
        int step = Math.max(1, Math.round(recharge * view.gui.Primitives.WEDGE_STEPS));
        int size = (int) Math.max(8f, (float) Math.hypot(getWidth(), getHeight()) * 1.05f);
        batch.setColor(1f, 1f, 1f, parentAlpha);
        batch.flush();
        float bleedX = badgeWidth * BADGE_BLEED;
        float bleedY = badgeHeight * BADGE_BLEED;
        if (clipBegin(getX() - bleedX, getY(),
                getWidth() + bleedX, getHeight() + bleedY)) {
            batch.draw(ui.primitives().wedge(size, step, RECHARGE_SHADE),
                    getX() + getWidth() / 2f - size / 2f,
                    getY() + getHeight() / 2f - size / 2f, size, size);
            batch.flush();
            clipEnd();
        }
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }

    public SeedPacket onClick(Runnable action) {
        UiKit.onClick(this, action);
        return this;
    }

    public Plants getPlant() {
        return plant;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isBoosted() {
        return boosted;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public float getPrefWidth() {
        return width();
    }

    @Override
    public float getPrefHeight() {
        return height();
    }
}

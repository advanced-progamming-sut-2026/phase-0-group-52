package view.gui.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Scaling;
import model.entities.zombies.ZombieRecord;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

public final class ZombieCard extends WidgetGroup {

    private static final float ART_W = 112f;
    private static final float ART_H = 157f;
    private static final float ICON_INSET_X = 0.10f;
    private static final float ICON_TOP = 0.07f;
    private static final float ICON_BOTTOM = 0.13f;
    private static final float LOCK_MARGIN = 0.05f;
    private static final float LOCKED_DIM = 0.30f;
    private static final float LOCK_SCALE = 0.42f;
    private static final int RING_THICKNESS = 6;
    private static final float BADGE_SCALE = 0.30f;

    private final UiKit ui;
    private final Assets assets;
    private final ZombieRecord record;
    private final float scale;

    private Image background;
    private Image icon;
    private Image lock;
    private Image chapterBadge;
    private Image selectRing;
    private Image hoverRing;

    private boolean seen;
    private boolean selected;
    private boolean hovered;

    public ZombieCard(UiKit ui, Assets assets, ZombieRecord record, float scale) {
        this.ui = ui;
        this.assets = assets;
        this.record = record;
        this.scale = scale;
        setTransform(false);
        build();
    }

    public ZombieRecord record() {
        return record;
    }

    public ZombieCard setSeen(boolean value) {
        seen = value;
        applyState();
        return this;
    }

    public ZombieCard setSelected(boolean value) {
        selected = value;
        applyState();
        return this;
    }

    private TextureRegion region(String id) {
        return assets == null ? null : assets.region(id);
    }

    private void build() {
        TextureRegion face = region(record.getPacketBackground());
        if (face != null) {
            background = new Image(face);
            background.setScaling(Scaling.stretch);
            background.setTouchable(Touchable.disabled);
            addActor(background);
        }

        TextureRegion art = region(record.getPacketIcon());
        if (art != null) {
            icon = new Image(art);
            icon.setScaling(Scaling.fit);
            icon.setTouchable(Touchable.disabled);
            addActor(icon);
        }

        TextureRegion shackle = region("IMAGE_UI_PACKETS_LOCK_SMALL");
        if (shackle != null) {
            lock = new Image(shackle);
            lock.setScaling(Scaling.fit);
            lock.setTouchable(Touchable.disabled);
            addActor(lock);
        }

        TextureRegion badge = region(chapterIcon(record.getChapter()));
        if (badge != null) {
            chapterBadge = new Image(badge);
            chapterBadge.setScaling(Scaling.fit);
            chapterBadge.setTouchable(Touchable.disabled);
            addActor(chapterBadge);
        }

        hoverRing = ring(Theme.SUN);
        selectRing = ring(Theme.GREEN);
        addActor(hoverRing);
        addActor(selectRing);

        watchHover();
        applyState();
    }

    private void applyState() {
        if (icon != null) {
            icon.setColor(seen ? Color.WHITE
                    : new Color(LOCKED_DIM, LOCKED_DIM, LOCKED_DIM, 1f));
        }
        if (background != null) {
            background.setColor(seen ? Color.WHITE : new Color(0.62f, 0.62f, 0.62f, 1f));
        }
        if (lock != null) {
            lock.setVisible(!seen);
        }
        if (selectRing != null) {
            selectRing.setVisible(selected);
        }
        if (hoverRing != null) {
            hoverRing.setVisible(hovered && !selected);
        }
    }

    private static String chapterIcon(String chapter) {
        if ("ANCIENT_EGYPT".equals(chapter)) {
            return "IMAGE_UI_QUESTS_QUESTICONS_EGYPT";
        }
        if ("FROSTBITE_CAVES".equals(chapter)) {
            return "IMAGE_UI_QUESTS_QUESTICONS_FROSTBITECAVES";
        }
        if ("BIG_WAVE_BEACH".equals(chapter)) {
            return "IMAGE_UI_QUESTS_QUESTICONS_BIGWAVEBEACH";
        }
        if ("DARK_AGES".equals(chapter)) {
            return "IMAGE_UI_QUESTS_QUESTICONS_DARKAGES";
        }
        if ("ZOMBOSS".equals(chapter)) {
            return "IMAGE_UI_QUESTS_QUESTICONS_ZOMBOSS";
        }
        return "IMAGE_UI_QUESTS_QUESTICONS_ZOMBIE";
    }

    private void watchHover() {
        addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor from) {
                if (pointer == -1) {
                    hovered = true;
                    applyState();
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y,
                    int pointer, com.badlogic.gdx.scenes.scene2d.Actor to) {
                if (pointer == -1) {
                    hovered = false;
                    applyState();
                }
            }
        });
    }

    private Image ring(Color colour) {
        Image mark = new Image(ui.primitives().rounded(10,
                new Color(0f, 0f, 0f, 0f), colour, RING_THICKNESS));
        mark.setTouchable(Touchable.disabled);
        mark.setVisible(false);
        return mark;
    }

    @Override
    public float getPrefWidth() {
        return ART_W * scale;
    }

    @Override
    public float getPrefHeight() {
        return ART_H * scale;
    }

    @Override
    public void layout() {
        float cardW = getWidth();
        float cardH = getHeight();
        if (background != null) {
            background.setBounds(0f, 0f, cardW, cardH);
        }
        if (icon != null) {
            float inset = cardW * ICON_INSET_X;
            icon.setBounds(inset, cardH * ICON_BOTTOM, cardW - inset * 2f,
                    cardH * (1f - ICON_TOP - ICON_BOTTOM));
        }
        if (lock != null) {
            float size = cardW * LOCK_SCALE;
            float margin = cardW * LOCK_MARGIN;
            lock.setBounds(cardW - size - margin, margin, size, size);
        }
        if (chapterBadge != null) {
            float size = cardW * BADGE_SCALE;
            float margin = cardW * LOCK_MARGIN;
            chapterBadge.setBounds(margin, cardH - size - margin, size, size);
        }
        if (hoverRing != null) {
            hoverRing.setBounds(0f, 0f, cardW, cardH);
        }
        if (selectRing != null) {
            selectRing.setBounds(0f, 0f, cardW, cardH);
        }
    }
}

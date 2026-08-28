package view.gui.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import view.gui.Animations;
import view.gui.Assets;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.ArrayList;
import java.util.List;

public final class Carousel extends WidgetGroup {
    public static final class Item {
        private final String name;
        private final Color accent;
        private final boolean locked;
        private final int segments;
        private final int completed;
        private final boolean[] special;
        private TextureRegion art;

        public Item setArt(TextureRegion value) {
            this.art = value;
            return this;
        }

        public Item(String name, Color accent, boolean locked) {
            this(name, accent, locked, 0, 0, null);
        }

        public Item(String name, Color accent, boolean locked,
                int segments, int completed, boolean[] special) {
            this.name = name;
            this.accent = accent;
            this.locked = locked;
            this.segments = segments;
            this.completed = completed;
            this.special = special;
        }

        public String getName() {
            return name;
        }

        public boolean isLocked() {
            return locked;
        }
    }

    public interface Listener {
        void onSelected(int index);

        void onActivated(int index);
    }

    private final UiKit ui;
    private final List<Item> items = new ArrayList<Item>();
    private final List<Table> cards = new ArrayList<Table>();
    private final List<Table> overlays = new ArrayList<Table>();
    private final List<PamActor> locks = new ArrayList<PamActor>();

    private static final float ENTER_TIME = 0.9f;
    private static final Color DIMMED = new Color(0.42f, 0.48f, 0.62f, 1f);

    private Assets pam;
    private String lockAnimation;
    private float enterTime = -1f;

    private float cardWidth = 260f;
    private float cardHeight = 320f;
    private float spacing = 300f;
    private float falloff = 0.45f;
    private boolean centreAll;

    private int selected;
    private float visualIndex;
    private float dragAccumulator;
    private Listener listener;

    public Carousel(UiKit ui) {
        this.ui = ui;
        setTouchable(Touchable.enabled);
        addListener(inputHandler());
    }

    private InputListener inputHandler() {
        return new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer,
                    com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    takeFocus();
                }
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                takeFocus();
                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                takeFocus();
                dragAccumulator = 0f;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                accumulateDrag(com.badlogic.gdx.Gdx.input.getDeltaX());
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (Math.abs(dragAccumulator) < 6f) {
                    pickAt(x);
                }
                dragAccumulator = 0f;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                step(amountY > 0 ? 1 : -1);
                return true;
            }

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                return handleKey(keycode);
            }
        };
    }

    private void accumulateDrag(float deltaX) {
        dragAccumulator += deltaX;
        while (dragAccumulator > spacing * 0.5f) {
            dragAccumulator -= spacing;
            step(-1);
        }
        while (dragAccumulator < -spacing * 0.5f) {
            dragAccumulator += spacing;
            step(1);
        }
    }

    private void takeFocus() {
        if (getStage() == null) {
            return;
        }
        if (getStage().getScrollFocus() != this) {
            getStage().setScrollFocus(this);
        }
        if (getStage().getKeyboardFocus() != this) {
            getStage().setKeyboardFocus(this);
        }
    }

    private boolean handleKey(int keycode) {
        if (keycode == Input.Keys.LEFT) {
            step(-1);
            return true;
        }
        if (keycode == Input.Keys.RIGHT) {
            step(1);
            return true;
        }
        if (keycode == Input.Keys.ENTER) {
            activate();
            return true;
        }
        return false;
    }

    public Carousel setCardSize(float width, float height) {
        this.cardWidth = width;
        this.cardHeight = height;
        return this;
    }

    public Carousel setSpacing(float value) {
        this.spacing = value;
        return this;
    }

    public Carousel setFalloff(float value) {
        this.falloff = value;
        return this;
    }

    public Carousel setCentreAll(boolean value) {
        this.centreAll = value;
        return this;
    }

    public Carousel setLockAnimation(Assets value, String path) {
        this.pam = value;
        this.lockAnimation = path;
        return this;
    }

    public boolean isTransitioning() {
        return enterTime >= 0f;
    }

    public void playEnter() {
        if (enterTime < 0f) {
            enterTime = 0f;
        }
    }

    public void resetTransition() {
        enterTime = -1f;
    }

    public void playUnlock(int index, Runnable done) {
        if (index < 0 || index >= locks.size() || locks.get(index) == null) {
            if (done != null) {
                done.run();
            }
            return;
        }
        locks.get(index).play("open", false, done);
    }

    public Carousel setListener(Listener value) {
        this.listener = value;
        return this;
    }

    public void setItems(List<Item> values) {
        items.clear();
        items.addAll(values);
        rebuildCards();
        selected = Math.min(selected, Math.max(0, items.size() - 1));
        visualIndex = selected;
    }

    public int getSelected() {
        return selected;
    }

    public void select(int index) {
        if (items.isEmpty()) {
            return;
        }
        int clamped = Math.max(0, Math.min(items.size() - 1, index));
        if (clamped == selected) {
            return;
        }
        selected = clamped;
        if (listener != null) {
            listener.onSelected(selected);
        }
    }

    private void step(int delta) {
        select(selected + delta);
    }

    private void activate() {
        if (listener != null && !items.isEmpty()) {
            listener.onActivated(selected);
        }
    }

    private float anchor() {
        return centreAll ? (items.size() - 1) / 2f : visualIndex;
    }

    private void pickAt(float x) {
        if (items.isEmpty()) {
            return;
        }
        float centre = getWidth() / 2f;
        int nearest = selected;
        float best = Float.MAX_VALUE;
        for (int i = 0; i < cards.size(); i++) {
            float cardCentre = centre + (i - anchor()) * spacing;
            float distance = Math.abs(x - cardCentre);
            if (distance < best) {
                best = distance;
                nearest = i;
            }
        }
        if (nearest == selected) {
            activate();
        } else {
            select(nearest);
        }
    }

    private void rebuildCards() {
        for (Table card : cards) {
            card.remove();
        }
        cards.clear();
        overlays.clear();
        locks.clear();
        for (Item item : items) {
            Table card = buildCard(item);
            cards.add(card);
            addActor(card);
        }
    }

    private Table buildCard(Item item) {
        Stack stack = new Stack();
        stack.add(item.art != null ? islandFace(item) : paintedFace(item));

        Table overlay = new Table();
        if (item.art == null) {
            overlay.bottom();
            overlay.pad(Theme.PAD);
        } else {
            overlay.center();
        }

        boolean roomy = cardWidth >= 180f;
        Label name = new Label(item.name, ui.skin(), roomy ? "titleOnDark" : "smallOnDark");
        name.setAlignment(Align.center);
        name.setWrap(true);
        overlay.add(name).growX().row();

        if (item.locked && item.art == null) {
            Label lock = new Label("LOCKED", ui.skin(), "smallOnDark");
            lock.setAlignment(Align.center);
            overlay.add(lock).padTop(4f);
        } else if (!item.locked && item.segments > 0) {
            overlay.add(levelTrack(item))
                    .width(item.art == null ? cardWidth : cardWidth * 0.72f)
                    .padTop(Theme.PAD_SMALL);
        }
        stack.add(overlay);
        overlays.add(overlay);
        locks.add(addLock(item, stack));

        Table card = new Table();
        card.add(stack).grow();
        card.setTransform(true);
        card.setSize(cardWidth, cardHeight);
        card.setOrigin(Align.center);
        if (item.locked && item.art != null) {
            card.setColor(DIMMED);
        }
        return card;
    }

    private Table islandFace(Item item) {
        Image island = new Image(new TextureRegionDrawable(item.art));
        island.setScaling(Scaling.fit);
        Table holder = new Table();
        holder.add(island).grow();
        return holder;
    }

    private Table paintedFace(Item item) {
        Color face = item.locked ? Theme.LOCKED : item.accent;
        Table art = new Table();
        art.setBackground(ui.primitives().rounded(Theme.RADIUS + 2,
                face, Theme.darken(face, 0.45f), Theme.BORDER));
        return art;
    }

    private PamActor addLock(Item item, Stack stack) {
        if (!item.locked || pam == null || lockAnimation == null) {
            return null;
        }
        PamActor lock = new PamActor(pam, lockAnimation, "idle").setFit(true);
        if (!lock.isReady()) {
            return null;
        }
        Table holder = new Table();
        holder.top();
        holder.add(lock).size(cardWidth * 0.44f).padTop(cardHeight * 0.14f);
        stack.add(holder);
        return lock;
    }

    private LevelTrack levelTrack(Item item) {
        return new LevelTrack(ui, item.segments, item.completed, item.special,
                pam == null ? null : pam.region(Assets.SKULL));
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        visualIndex += (selected - visualIndex) * Math.min(1f, delta * 9f);
        if (enterTime >= 0f) {
            enterTime = Math.min(ENTER_TIME, enterTime + delta);
        }
        layoutCards();
    }

    private void layoutCards() {
        float centreX = getWidth() / 2f;
        float centreY = getHeight() / 2f;
        float drawWidth = Math.min(cardWidth, getWidth() - 8f);
        float drawHeight = Math.min(cardHeight, getHeight() - 8f);

        for (int i = 0; i < cards.size(); i++) {
            Table card = cards.get(i);
            float distance = Math.abs(i - visualIndex);
            float shrink = Math.min(1f, distance);
            float scale = 1f - falloff * Interpolation.smooth.apply(shrink);
            float alpha = 1f - 0.55f * Interpolation.smooth.apply(shrink);

            float exit = enterTime < 0f ? 0f : enterTime / ENTER_TIME;
            boolean focused = distance < 0.5f;
            if (focused) {
                scale *= 1f - 0.9f * Interpolation.pow2In.apply(exit);
            } else {
                alpha *= Math.max(0f, 1f - exit * 2.5f);
            }
            if (overlays.size() > i) {
                float focus = Math.max(0f, 1f - distance * 2.5f);
                overlays.get(i).getColor().a = focus * Math.max(0f, 1f - exit * 3f);
            }

            card.setSize(drawWidth, drawHeight);
            card.setOrigin(Align.center);
            card.setScale(scale);
            card.setRotation(focused ? 540f * Interpolation.pow2In.apply(exit) : 0f);
            card.getColor().a = alpha;
            card.setPosition(
                    centreX + (i - anchor()) * spacing - drawWidth / 2f,
                    centreY - drawHeight / 2f);
            card.setZIndex(distance < 0.5f
                    ? cards.size() : Math.max(0, cards.size() - (int) distance - 1));
        }
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        batch.flush();
        if (clipBegin(getX(), getY(), getWidth(), getHeight())) {
            super.draw(batch, parentAlpha);
            batch.flush();
            clipEnd();
        }
    }

    public void bump() {
        if (!cards.isEmpty()) {
            Animations.pulse(cards.get(selected));
        }
    }

    @Override
    public float getPrefWidth() {
        return cardWidth;
    }

    @Override
    public float getPrefHeight() {
        return cardHeight;
    }

    @Override
    public float getMinWidth() {
        return 0f;
    }

    @Override
    public float getMinHeight() {
        return 0f;
    }
}

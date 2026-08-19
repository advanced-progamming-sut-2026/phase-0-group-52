package view.gui.widgets;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Align;
import view.gui.Animations;
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
        for (Item item : items) {
            Table card = buildCard(item);
            cards.add(card);
            addActor(card);
        }
    }

    private Table buildCard(Item item) {
        Color face = item.locked ? Theme.LOCKED : item.accent;

        Table art = new Table();
        art.setBackground(ui.primitives().rounded(Theme.RADIUS + 2,
                face, Theme.darken(face, 0.45f), Theme.BORDER));

        Table overlay = new Table();
        overlay.bottom();
        overlay.pad(Theme.PAD);

        boolean roomy = cardWidth >= 180f;
        Label name = new Label(item.name, ui.skin(), roomy ? "titleOnDark" : "smallOnDark");
        name.setAlignment(Align.center);
        name.setWrap(true);
        overlay.add(name).growX().row();

        if (item.locked) {
            Label lock = new Label("LOCKED", ui.skin(), "smallOnDark");
            lock.setAlignment(Align.center);
            overlay.add(lock).padTop(4f);
        } else if (item.segments > 0) {
            overlay.add(progressBar(item)).growX().padTop(Theme.PAD_SMALL);
        }

        Stack stack = new Stack();
        stack.add(art);
        stack.add(overlay);

        Table card = new Table();
        card.add(stack).grow();
        card.setSize(cardWidth, cardHeight);
        card.setOrigin(Align.center);
        return card;
    }

    private Table progressBar(Item item) {
        com.badlogic.gdx.scenes.scene2d.ui.ProgressBar skinBar =
                ui.skinProgressBar(item.segments, item.completed, "xp_green");
        if (skinBar != null) {
            return skinnedProgress(item, skinBar);
        }
        return segmentedProgress(item);
    }

    private Table skinnedProgress(Item item, com.badlogic.gdx.scenes.scene2d.ui.ProgressBar bar) {
        Table track = new Table();
        track.add(bar).grow().height(18f);

        Table markers = new Table();
        markers.left();
        boolean any = false;
        for (int i = 0; i < item.segments; i++) {
            boolean isSpecial = item.special != null && i < item.special.length && item.special[i];
            Table cell = new Table();
            if (isSpecial) {
                any = true;
                cell.add(ui.token(8, Theme.SUN)).size(8f);
            }
            markers.add(cell).growX().uniformX();
        }
        if (!any) {
            return track;
        }

        Stack stack = new Stack();
        stack.add(track);
        stack.add(markers);

        Table holder = new Table();
        holder.add(stack).grow();
        return holder;
    }

    private Table segmentedProgress(Item item) {
        Table bar = new Table();
        for (int i = 0; i < item.segments; i++) {
            boolean done = i < item.completed;
            boolean isSpecial = item.special != null && i < item.special.length && item.special[i];

            Table segment = new Table();
            segment.setBackground(ui.primitives().rounded(3,
                    done ? Theme.GREEN : Theme.darken(Theme.PANEL_SUNKEN, 0.35f),
                    Theme.darken(Theme.OUTLINE, 0.2f), 2));

            Stack cell = new Stack();
            cell.add(segment);
            if (isSpecial) {
                Table dotHolder = new Table();
                dotHolder.center();
                dotHolder.add(ui.token(9, Theme.SUN)).size(9f);
                cell.add(dotHolder);
            }
            bar.add(cell).growX().height(14f).padRight(i == item.segments - 1 ? 0f : 3f);
        }
        return bar;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        visualIndex += (selected - visualIndex) * Math.min(1f, delta * 9f);
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

            card.setSize(drawWidth, drawHeight);
            card.setOrigin(Align.center);
            card.setScale(scale);
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

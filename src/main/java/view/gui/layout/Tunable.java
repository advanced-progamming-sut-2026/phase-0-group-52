package view.gui.layout;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public final class Tunable extends WidgetGroup {

    private final String id;
    private final String kind;
    private final Actor child;

    public Tunable(String id, Actor child) {
        this.id = id;
        this.kind = UiLayout.kindOf(child);
        this.child = child;
        setTouchable(Touchable.childrenOnly);
        setName(child.getName());
        addActor(child);
    }

    public String id() {
        return id;
    }

    public String kind() {
        return kind;
    }

    public Actor child() {
        return child;
    }

    private Layout sizes() {
        return child instanceof Layout ? (Layout) child : null;
    }

    @Override
    public float getPrefWidth() {
        Layout inner = sizes();
        return inner == null ? child.getWidth() : inner.getPrefWidth();
    }

    @Override
    public float getPrefHeight() {
        Layout inner = sizes();
        return inner == null ? child.getHeight() : inner.getPrefHeight();
    }

    @Override
    public float getMinWidth() {
        Layout inner = sizes();
        return inner == null ? 0f : inner.getMinWidth();
    }

    @Override
    public float getMinHeight() {
        Layout inner = sizes();
        return inner == null ? 0f : inner.getMinHeight();
    }

    @Override
    public float getMaxWidth() {
        Layout inner = sizes();
        return inner == null ? 0f : inner.getMaxWidth();
    }

    @Override
    public float getMaxHeight() {
        Layout inner = sizes();
        return inner == null ? 0f : inner.getMaxHeight();
    }

    @Override
    public void layout() {
        UiLayout.Tweak tweak = UiLayout.tweak(id);
        float width = Math.max(UiLayout.MIN_SIZE, getWidth() + tweak.getDw());
        float height = Math.max(UiLayout.MIN_SIZE, getHeight() + tweak.getDh());
        child.setBounds(tweak.getDx(), tweak.getDy(), width, height);
        Layout inner = sizes();
        if (inner != null) {
            inner.validate();
        }
    }
}

package view.gui.layout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import view.gui.GameContext;

import java.util.ArrayDeque;
import java.util.Deque;

public final class LayoutEditor extends WidgetGroup {

    private static final float STEP = 1f;
    private static final float BIG_STEP = 10f;
    private static final float GRIP = 18f;
    private static final int MAX_UNDO = 60;
    private static final int MAX_DEPTH = 32;
    private static final Color PICK = new Color(0.15f, 0.95f, 1f, 0.95f);
    private static final Color HOVER = new Color(1f, 0.85f, 0.2f, 0.7f);

    private final GameContext context;
    private final LayoutHud hud;
    private final Drawable pixel;
    private final Vector2 point = new Vector2();
    private final Vector2 origin = new Vector2();
    private final Rectangle frame = new Rectangle();
    private final Vector2 lastPick = new Vector2();
    private final Deque<Step> undo = new ArrayDeque<Step>();

    private Actor selected;
    private Actor hovered;
    private String selectedId;
    private UiLayout.Tweak dragBase;
    private boolean resizing;
    private boolean dragging;

    public LayoutEditor(GameContext context) {
        this.context = context;
        this.pixel = context.ui().primitives().flat(Color.WHITE);
        this.hud = new LayoutHud(this, context.ui());
        setFillParent(true);
        setTouchable(Touchable.enabled);
        addActor(hud);
        addListener(picker());
    }

    private static final class Step {
        private final String id;
        private final UiLayout.Tweak value;

        Step(String id, UiLayout.Tweak value) {
            this.id = id;
            this.value = value;
        }
    }

    public void attach(Stage stage) {
        if (getStage() != stage) {
            stage.addActor(this);
            clearSelection();
        }
        if (getZIndex() != stage.getRoot().getChildren().size - 1) {
            toFront();
        }
    }

    public void detach() {
        if (getStage() != null) {
            UiLayout.save();
            remove();
        }
        clearSelection();
    }

    @Override
    public void layout() {
        hud.place(getWidth(), getHeight());
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        Actor found = super.hit(x, y, touchable);
        if (found != null && found != this) {
            return found;
        }
        return passThrough() ? null : found;
    }

    private static boolean passThrough() {
        return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
    }

    private static boolean shift() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private static boolean control() {
        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }

    private InputListener picker() {
        return new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return begin(x, y, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                drag(x, y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                finish();
            }

            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                stagePoint(x, y);
                hovered = UiLayout.pickAt(rootGroup(), lastPick.x, lastPick.y);
                return false;
            }
        };
    }

    private boolean begin(float x, float y, int button) {
        stagePoint(x, y);
        if (button == Input.Buttons.RIGHT) {
            clearSelection();
            return true;
        }
        Actor target = UiLayout.pickAt(rootGroup(), lastPick.x, lastPick.y);
        if (target == null) {
            clearSelection();
            return true;
        }
        if (target != selected) {
            select(target);
        }
        if (selectedId == null) {
            return true;
        }
        resizing = onGrip(lastPick.x, lastPick.y);
        dragBase = UiLayout.tweak(selectedId).copy();
        origin.set(lastPick);
        dragging = true;
        return true;
    }

    private void stagePoint(float x, float y) {
        lastPick.set(x, y);
        localToStageCoordinates(lastPick);
    }

    private Group rootGroup() {
        Stage stage = getStage();
        return stage == null ? null : stage.getRoot();
    }

    private void select(Actor actor) {
        String id = UiLayout.pathOf(actor);
        if (id == null) {
            clearSelection();
            return;
        }
        selected = actor;
        selectedId = id;
        hud.refresh();
    }

    private void clearSelection() {
        selected = null;
        selectedId = null;
        hovered = null;
        dragging = false;
        dragBase = null;
        hud.refresh();
    }

    private boolean onGrip(float stageX, float stageY) {
        if (selected == null) {
            return false;
        }
        Rectangle box = boundsOf(selected);
        return stageX >= box.x + box.width - GRIP && stageX <= box.x + box.width + GRIP
                && stageY >= box.y - GRIP && stageY <= box.y + GRIP;
    }

    private Rectangle boundsOf(Actor actor) {
        point.set(0f, 0f);
        actor.localToStageCoordinates(point);
        float left = point.x;
        float bottom = point.y;
        point.set(actor.getWidth(), actor.getHeight());
        actor.localToStageCoordinates(point);
        return frame.set(Math.min(left, point.x), Math.min(bottom, point.y),
                Math.abs(point.x - left), Math.abs(point.y - bottom));
    }

    private void drag(float x, float y) {
        Group parent = (!dragging || selected == null) ? null : selected.getParent();
        if (parent == null || dragBase == null) {
            return;
        }
        stagePoint(x, y);
        float toX = lastPick.x;
        float toY = lastPick.y;
        parent.stageToLocalCoordinates(point.set(origin.x, origin.y));
        float fromX = point.x;
        float fromY = point.y;
        parent.stageToLocalCoordinates(point.set(toX, toY));
        float moveX = point.x - fromX;
        float moveY = point.y - fromY;
        UiLayout.Tweak live = UiLayout.edit(selectedId);
        if (resizing) {
            live.set(dragBase.getDx(), dragBase.getDy() + moveY,
                    dragBase.getDw() + moveX, dragBase.getDh() - moveY);
        } else {
            live.set(dragBase.getDx() + moveX, dragBase.getDy() + moveY,
                    dragBase.getDw(), dragBase.getDh());
        }
        applyNow();
    }

    private void finish() {
        if (dragging && dragBase != null && selectedId != null
                && !sameAs(dragBase, UiLayout.tweak(selectedId))) {
            remember(selectedId, dragBase);
            UiLayout.save();
        }
        dragging = false;
        dragBase = null;
        hud.refresh();
    }

    private static boolean sameAs(UiLayout.Tweak one, UiLayout.Tweak other) {
        return one.getDx() == other.getDx() && one.getDy() == other.getDy()
                && one.getDw() == other.getDw() && one.getDh() == other.getDh();
    }

    private void remember(String id, UiLayout.Tweak before) {
        undo.push(new Step(id, before));
        while (undo.size() > MAX_UNDO) {
            undo.removeLast();
        }
    }

    private void applyNow() {
        Stage stage = getStage();
        if (stage == null) {
            return;
        }
        UiLayout.apply(stage.getRoot());
        if (selected != null && selected.getParent() instanceof Tunable) {
            ((Tunable) selected.getParent()).invalidate();
        }
        hud.refresh();
    }

    public void poll() {
        if (passThrough() || getStage() == null) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            clearSelection();
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            hud.cycleCorner();
            invalidate();
            return;
        }
        reconnect();
        if (control() && Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            undoLast();
            return;
        }
        if (control() && Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            saveNow();
            return;
        }
        if (selectedId != null && !(getStage().getKeyboardFocus() instanceof TextField)) {
            arrows();
        }
    }

    private void reconnect() {
        if (selectedId == null || (selected != null && selected.getStage() != null)) {
            return;
        }
        Actor found = findById(getStage().getRoot(), selectedId, 0);
        selected = found;
        if (found == null) {
            clearSelection();
        }
    }

    private Actor findById(Group parent, String id, int depth) {
        if (depth > MAX_DEPTH) {
            return null;
        }
        for (Actor child : parent.getChildren()) {
            if (child instanceof LayoutEditor) {
                continue;
            }
            if (UiLayout.isTunable(child) && id.equals(UiLayout.pathOf(child))) {
                return child;
            }
            if (child instanceof Group) {
                Actor found = findById((Group) child, id, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void arrows() {
        float step = shift() ? BIG_STEP : STEP;
        float moveX = axis(Input.Keys.RIGHT) - axis(Input.Keys.LEFT);
        float moveY = axis(Input.Keys.UP) - axis(Input.Keys.DOWN);
        if (moveX == 0f && moveY == 0f) {
            return;
        }
        UiLayout.Tweak before = UiLayout.tweak(selectedId).copy();
        UiLayout.Tweak live = UiLayout.edit(selectedId);
        if (control()) {
            live.set(before.getDx(), before.getDy(),
                    before.getDw() + moveX * step, before.getDh() + moveY * step);
        } else {
            live.set(before.getDx() + moveX * step, before.getDy() + moveY * step,
                    before.getDw(), before.getDh());
        }
        remember(selectedId, before);
        applyNow();
        UiLayout.save();
    }

    private static float axis(int key) {
        return Gdx.input.isKeyJustPressed(key) ? 1f : 0f;
    }

    private void undoLast() {
        if (undo.isEmpty()) {
            return;
        }
        Step step = undo.pop();
        UiLayout.restore(step.id, step.value);
        applyNow();
        UiLayout.save();
        context.toasts().info("Reverted the last change.");
    }

    public void selectParent() {
        if (selected == null) {
            return;
        }
        Actor node = selected.getParent();
        while (node != null && !UiLayout.isTunable(node)) {
            node = node.getParent();
        }
        if (node != null) {
            select(node);
        }
    }

    public void selectChild() {
        Actor found = innerOf(selected, 0);
        if (found != null) {
            select(found);
        }
    }

    private Actor innerOf(Actor node, int depth) {
        if (!(node instanceof Group) || depth > MAX_DEPTH) {
            return null;
        }
        Actor fallback = null;
        for (Actor child : ((Group) node).getChildren()) {
            if (child instanceof LayoutEditor) {
                continue;
            }
            Actor real = child instanceof Tunable ? ((Tunable) child).child() : child;
            if (!UiLayout.isTunable(real)) {
                fallback = fallback != null ? fallback : innerOf(real, depth + 1);
            } else if (UiLayout.covers(real, lastPick.x, lastPick.y)) {
                return real;
            } else if (fallback == null) {
                fallback = real;
            }
        }
        return fallback;
    }

    public boolean hasSelection() {
        return selectedId != null;
    }

    public String selectionId() {
        return selectedId == null ? "Click any panel, button or label"
                : UiLayout.shortId(selectedId);
    }

    public String selectionValues() {
        if (selectedId == null) {
            return UiLayout.count() + " tweaks saved   .   editing " + UiLayout.scope();
        }
        UiLayout.Tweak live = UiLayout.tweak(selectedId);
        return UiLayout.kindOf(selected) + "  " + (int) selected.getWidth()
                + "x" + (int) selected.getHeight()
                + "     x " + signed(live.getDx()) + "   y " + signed(live.getDy())
                + "   w " + signed(live.getDw()) + "   h " + signed(live.getDh());
    }

    private static String signed(float value) {
        int whole = (int) value;
        return whole > 0 ? "+" + whole : String.valueOf(whole);
    }

    public void resetSelected() {
        if (selectedId == null) {
            return;
        }
        remember(selectedId, UiLayout.tweak(selectedId).copy());
        UiLayout.clear(selectedId);
        refreshFrames();
        context.toasts().info("Element restored.");
    }

    public void resetScreen() {
        int removed = UiLayout.clearScope(UiLayout.scope());
        refreshFrames();
        context.toasts().info(removed + " tweaks cleared on this screen.");
    }

    public void saveNow() {
        UiLayout.save();
        context.toasts().info("UI layout saved to " + UiLayout.PATH);
    }

    public void leaveMode() {
        UiLayout.save();
        context.settings().setUiEditMode(false);
    }

    private void refreshFrames() {
        Stage stage = getStage();
        if (stage == null) {
            return;
        }
        UiLayout.apply(stage.getRoot());
        UiLayout.refresh(stage.getRoot());
        UiLayout.save();
        hud.refresh();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float packed = batch.getPackedColor();
        if (hovered != null && hovered != selected && hovered.getStage() != null) {
            outline(batch, boundsOf(hovered), HOVER, 1f);
        }
        if (selected != null && selected.getStage() != null) {
            Rectangle box = boundsOf(selected);
            outline(batch, box, PICK, 2f);
            bar(batch, box.x + box.width - GRIP, box.y, GRIP, GRIP, PICK);
        }
        batch.setPackedColor(packed);
        super.draw(batch, parentAlpha);
    }

    private void outline(Batch batch, Rectangle box, Color tint, float weight) {
        bar(batch, box.x, box.y, box.width, weight, tint);
        bar(batch, box.x, box.y + box.height - weight, box.width, weight, tint);
        bar(batch, box.x, box.y, weight, box.height, tint);
        bar(batch, box.x + box.width - weight, box.y, weight, box.height, tint);
    }

    private void bar(Batch batch, float x, float y, float width, float height, Color tint) {
        batch.setColor(tint);
        pixel.draw(batch, x, y, Math.max(1f, width), Math.max(1f, height));
    }

}

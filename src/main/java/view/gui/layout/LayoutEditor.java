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
import java.util.List;
import java.util.Deque;

public final class LayoutEditor extends WidgetGroup {

    private static final float STEP = 1f;
    private static final float BIG_STEP = 10f;
    private static final float GRIP = 12f;
    private static final float CYCLE_SLOP = 16f;
    private static final int MAX_UNDO = 60;
    private static final int MAX_DEPTH = 32;
    private static final Color PICK = new Color(0.15f, 0.95f, 1f, 0.95f);
    private static final Color HOVER = new Color(1f, 0.85f, 0.2f, 0.7f);
    private static final Color GUIDE = new Color(1f, 0.2f, 0.65f, 0.9f);

    private final GameContext context;
    private final LayoutHud hud;
    private final Drawable pixel;
    private final Vector2 point = new Vector2();
    private final Vector2 origin = new Vector2();
    private final Rectangle frame = new Rectangle();
    private final Vector2 lastPick = new Vector2();
    private final Deque<Step> undo = new ArrayDeque<Step>();

    private final Snap snap = new Snap();
    private final Vector2 cycleAt = new Vector2(Float.NaN, Float.NaN);

    private Actor selected;
    private Actor hovered;
    private String selectedId;
    private UiLayout.Tweak dragBase;
    private Handle handle = Handle.NONE;
    private boolean dragging;
    private int cycleIndex;
    private int pictureIndex;
    private float baseWidth;
    private float baseHeight;

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
        dropStaleSelection();
        Handle grabbed = selected == null ? Handle.NONE
                : Handle.at(boundsOf(selected), lastPick.x, lastPick.y, GRIP);
        if (grabbed.resizes()) {
            handle = grabbed;
            return beginDrag();
        }
        Actor target = cycleTarget();
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
        handle = Handle.MOVE;
        return beginDrag();
    }

    private boolean beginDrag() {
        dragBase = UiLayout.tweak(selectedId).copy();
        baseWidth = Math.max(1f, selected.getWidth());
        baseHeight = Math.max(1f, selected.getHeight());
        origin.set(lastPick);
        snap.release();
        snap.gather(rootGroup(), selected, hostOf(selected), getWidth(), getHeight());
        dragging = true;
        return true;
    }

    private static Actor hostOf(Actor actor) {
        return actor != null && actor.getParent() instanceof Tunable
                ? actor.getParent() : null;
    }

    private Actor cycleTarget() {
        java.util.List<Actor> found =
                UiLayout.candidatesAt(rootGroup(), lastPick.x, lastPick.y);
        if (found.isEmpty()) {
            cycleAt.set(Float.NaN, Float.NaN);
            return null;
        }
        boolean samePlace = !Float.isNaN(cycleAt.x)
                && Math.abs(cycleAt.x - lastPick.x) <= CYCLE_SLOP
                && Math.abs(cycleAt.y - lastPick.y) <= CYCLE_SLOP;
        cycleIndex = samePlace ? (cycleIndex + 1) % found.size() : 0;
        cycleAt.set(lastPick);
        if (samePlace && found.size() > 1) {
            context.toasts().info("Pick " + (cycleIndex + 1) + " of " + found.size()
                    + ": " + UiLayout.shortId(UiLayout.pathOf(found.get(cycleIndex))));
        }
        return found.get(cycleIndex);
    }

    private void dropStaleSelection() {
        if (selected != null && selected.getStage() == null) {
            reconnect();
        }
        if (selected != null && selected.getStage() == null) {
            clearSelection();
        }
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
        UiLayout.unbreak(id);
        String why = UiLayout.blocked(actor);
        if (why != null) {
            context.toasts().error(why);
        }
        if (getStage() != null && getStage().getKeyboardFocus() instanceof TextField) {
            getStage().setKeyboardFocus(null);
        }
        hud.refresh();
    }

    private void clearSelection() {
        selected = null;
        selectedId = null;
        hovered = null;
        dragging = false;
        dragBase = null;
        handle = Handle.NONE;
        cycleAt.set(Float.NaN, Float.NaN);
        cycleIndex = 0;
        snap.release();
        hud.refresh();
    }

    private Rectangle boundsOf(Actor actor) {
        if (actor == null || actor.getStage() == null) {
            return frame.set(0f, 0f, 0f, 0f);
        }
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
        if (parent == null || dragBase == null || selected.getStage() == null) {
            dragging = false;
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
        if (handle.resizes()) {
            resize(live, moveX, moveY);
        } else {
            move(live, moveX, moveY);
        }
        applyNow();
    }

    private void move(UiLayout.Tweak live, float moveX, float moveY) {
        Rectangle box = boundsOf(selected);
        float shiftedX = moveX - (live.getDx() - dragBase.getDx());
        float shiftedY = moveY - (live.getDy() - dragBase.getDy());
        float wantX = box.x + shiftedX;
        float wantY = box.y + shiftedY;
        float fixX = snap.correctX(wantX, wantX + box.width);
        float fixY = snap.correctY(wantY, wantY + box.height);
        live.set(dragBase.getDx() + moveX + fixX, dragBase.getDy() + moveY + fixY,
                dragBase.getDw(), dragBase.getDh());
    }

    private void resize(UiLayout.Tweak live, float moveX, float moveY) {
        float dx = dragBase.getDx();
        float dy = dragBase.getDy();
        float dw = dragBase.getDw();
        float dh = dragBase.getDh();
        if (handle.pullsRight()) {
            dw += moveX;
        }
        if (handle.pullsLeft()) {
            dx += moveX;
            dw -= moveX;
        }
        if (handle.pullsTop()) {
            dh += moveY;
        }
        if (handle.pullsBottom()) {
            dy += moveY;
            dh -= moveY;
        }
        if (shift()) {
            float locked = (dw - dragBase.getDw()) * baseHeight / baseWidth;
            if (handle.pullsBottom()) {
                dy = dragBase.getDy() - locked;
            }
            dh = dragBase.getDh() + locked;
        }
        live.set(dx, dy, dw, dh);
    }

    private void finish() {
        if (dragging && dragBase != null && selectedId != null
                && !sameAs(dragBase, UiLayout.tweak(selectedId))) {
            remember(selectedId, dragBase);
            UiLayout.save();
        }
        dragging = false;
        dragBase = null;
        handle = Handle.NONE;
        snap.release();
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
        if (layerKeys()) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)
                || Gdx.input.isKeyJustPressed(Input.Keys.DEL)) {
            removeSelected();
            return;
        }
        if (control() && Gdx.input.isKeyJustPressed(Input.Keys.N)) {
            addPicture(shift() ? -1 : 1);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT_BRACKET)) {
            browse(-1);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT_BRACKET)) {
            browse(1);
            return;
        }
        reconnect();
        if (dragging && !Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            finish();
        }
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

    private boolean layerKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            stepUnder(1);
            return true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            stepUnder(-1);
            return true;
        }
        return false;
    }

    void stepUnder(int step) {
        point.set(Gdx.input.getX(), Gdx.input.getY());
        getStage().screenToStageCoordinates(point);
        List<Actor> found = UiLayout.candidatesAt(rootGroup(), point.x, point.y);
        if (found.isEmpty()) {
            context.toasts().info("Nothing under the cursor.");
            return;
        }
        int at = found.indexOf(selected);
        cycleIndex = Math.floorMod((at < 0 ? -1 : at) + step, found.size());
        cycleAt.set(point);
        select(found.get(cycleIndex));
        context.toasts().info("Layer " + (cycleIndex + 1) + " of " + found.size()
                + ": " + UiLayout.shortId(selectedId));
    }

    private void removeSelected() {
        if (selectedId == null) {
            context.toasts().error("Nothing selected.");
            return;
        }
        String id = selectedId;
        remember(id, UiLayout.tweak(id).copy());
        if (UiLayout.isExtra(id)) {
            UiLayout.drop(id);
            context.toasts().info("Picture removed.");
        } else {
            UiLayout.hide(id, true);
            context.toasts().info("Hidden. Ctrl+Z brings it back.");
        }
        clearSelection();
        refreshFrames();
    }

    private void browse(int step) {
        List<String> files = Pictures.available();
        if (files.isEmpty()) {
            return;
        }
        pictureIndex = Math.floorMod(pictureIndex + step, files.size());
        context.toasts().info("Ctrl+N adds " + files.get(pictureIndex));
    }

    private void addPicture(int step) {
        List<String> files = Pictures.available();
        if (files.isEmpty()) {
            context.toasts().error("No PNGs found under assets/.");
            return;
        }
        pictureIndex = Math.floorMod(pictureIndex, files.size());
        String file = files.get(pictureIndex);
        float size = Math.min(getWidth(), getHeight()) / 4f;
        String id = UiLayout.addImage(file, size, size);
        UiLayout.Tweak made = UiLayout.edit(id);
        made.set((getWidth() - size) / 2f, (getHeight() - size) / 2f, size, size);
        remember(id, made.copy());
        context.toasts().info("Added " + file + ". [ and ] pick another.");
        refreshFrames();
        pictureIndex += step;
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
        if (UiLayout.isExtra(id)) {
            return parent.findActor(id);
        }
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
                + "   w " + signed(live.getDw()) + "   h " + signed(live.getDh())
                + "     " + fitState();
    }

    private String fitState() {
        if (!(selected.getParent() instanceof Tunable)) {
            return "NOT WRAPPED (" + UiLayout.kindOf(selected.getParent()) + " parent)";
        }
        Tunable holder = (Tunable) selected.getParent();
        boolean moved = Math.abs(selected.getX() - UiLayout.tweak(selectedId).getDx()) < 1f
                && Math.abs(selected.getY() - UiLayout.tweak(selectedId).getDy()) < 1f;
        return moved ? "applied" : "OVERWRITTEN by "
                + UiLayout.kindOf(holder.getParent());
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
            handles(batch, box);
            guides(batch);
        }
        batch.setPackedColor(packed);
        super.draw(batch, parentAlpha);
    }

    private void handles(Batch batch, Rectangle box) {
        float mid = GRIP / 2f;
        float[] xs = {box.x - mid, box.x + box.width / 2f - mid,
            box.x + box.width - mid};
        float[] ys = {box.y - mid, box.y + box.height / 2f - mid,
            box.y + box.height - mid};
        for (int col = 0; col < xs.length; col++) {
            for (int row = 0; row < ys.length; row++) {
                if (col != 1 || row != 1) {
                    bar(batch, xs[col], ys[row], GRIP, GRIP, PICK);
                }
            }
        }
    }

    private void guides(Batch batch) {
        if (!dragging) {
            return;
        }
        if (snap.isHoldingX()) {
            bar(batch, snap.getGuideX(), 0f, 1f, getHeight(), GUIDE);
        }
        if (snap.isHoldingY()) {
            bar(batch, 0f, snap.getGuideY(), getWidth(), 1f, GUIDE);
        }
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

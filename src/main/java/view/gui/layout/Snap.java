package view.gui.layout;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.SnapshotArray;

import java.util.ArrayList;
import java.util.List;

public final class Snap {

    public static final float CAPTURE = 7f;
    public static final float RELEASE = 15f;

    private final List<Float> columns = new ArrayList<Float>();
    private final List<Float> rows = new ArrayList<Float>();

    private float heldX;
    private float heldY;
    private boolean holdingX;
    private boolean holdingY;
    private float guideX;
    private float guideY;

    public void gather(Group root, Actor moving, Actor host, float width, float height) {
        columns.clear();
        rows.clear();
        columns.add(0f);
        columns.add(width / 2f);
        columns.add(width);
        rows.add(0f);
        rows.add(height / 2f);
        rows.add(height);
        walk(root, moving, host, 0);
    }

    private void walk(Group parent, Actor moving, Actor host, int depth) {
        if (depth > 24) {
            return;
        }
        SnapshotArray<Actor> children = parent.getChildren();
        for (int i = 0; i < children.size; i++) {
            Actor child = children.get(i);
            if (child == moving || child == host || child instanceof LayoutEditor
                    || !child.isVisible()) {
                continue;
            }
            if (UiLayout.isTunable(child) && child.getWidth() > 0f && child.getHeight() > 0f) {
                add(child);
            }
            if (child instanceof Group) {
                walk((Group) child, moving, host, depth + 1);
            }
        }
    }

    private void add(Actor actor) {
        Rectangle box = UiLayout.stageBounds(actor);
        columns.add(box.x);
        columns.add(box.x + box.width / 2f);
        columns.add(box.x + box.width);
        rows.add(box.y);
        rows.add(box.y + box.height / 2f);
        rows.add(box.y + box.height);
    }

    public float correctX(float low, float high) {
        float found = nearest(columns, low, high, holdingX, heldX);
        holdingX = !Float.isNaN(found);
        if (holdingX) {
            heldX = found;
            guideX = found;
        }
        return holdingX ? offsetTo(found, low, high) : 0f;
    }

    public float correctY(float low, float high) {
        float found = nearest(rows, low, high, holdingY, heldY);
        holdingY = !Float.isNaN(found);
        if (holdingY) {
            heldY = found;
            guideY = found;
        }
        return holdingY ? offsetTo(found, low, high) : 0f;
    }

    private static float offsetTo(float guide, float low, float high) {
        float mid = (low + high) / 2f;
        float best = guide - low;
        if (Math.abs(guide - mid) < Math.abs(best)) {
            best = guide - mid;
        }
        if (Math.abs(guide - high) < Math.abs(best)) {
            best = guide - high;
        }
        return best;
    }

    private static float nearest(List<Float> guides, float low, float high,
            boolean holding, float held) {
        float mid = (low + high) / 2f;
        float limit = holding ? RELEASE : CAPTURE;
        float best = Float.NaN;
        float bestGap = limit;
        for (int i = 0; i < guides.size(); i++) {
            float guide = guides.get(i);
            float gap = Math.min(Math.abs(guide - low),
                    Math.min(Math.abs(guide - mid), Math.abs(guide - high)));
            boolean sticky = holding && guide == held && gap <= RELEASE;
            if (sticky) {
                return guide;
            }
            if (gap < bestGap) {
                bestGap = gap;
                best = guide;
            }
        }
        return best;
    }

    public void release() {
        holdingX = false;
        holdingY = false;
    }

    public boolean isHoldingX() {
        return holdingX;
    }

    public boolean isHoldingY() {
        return holdingY;
    }

    public float getGuideX() {
        return guideX;
    }

    public float getGuideY() {
        return guideY;
    }
}

package view.gui.layout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;
import util.Json;
import util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class UiLayout {

    public static final String PATH = "assets/ui-layout.json";
    public static final float MIN_SIZE = 8f;

    private static final float LIMIT = 4000f;
    private static final int MAX_ENTRIES = 600;
    private static final int MAX_DEPTH = 32;
    private static final String DEFAULT_SCOPE = "Screen";
    private static final Tweak IDENTITY = new Tweak();
    private static final Map<String, Tweak> TWEAKS = new LinkedHashMap<String, Tweak>();
    private static final Set<String> BROKEN = new HashSet<String>();
    private static final Rectangle BOX = new Rectangle();
    private static final Vector2 POINT = new Vector2();
    private static final int SHORT_TAIL = 3;

    private static boolean loaded;
    private static boolean dirty;
    private static String scope = DEFAULT_SCOPE;

    private UiLayout() {
    }

    public static final class Tweak {
        private float dx;
        private float dy;
        private float dw;
        private float dh;
        private String image;
        private boolean hidden;

        public String getImage() {
            return image;
        }

        public void setImage(String value) {
            this.image = value;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean value) {
            this.hidden = value;
        }

        public float getDx() {
            return dx;
        }

        public float getDy() {
            return dy;
        }

        public float getDw() {
            return dw;
        }

        public float getDh() {
            return dh;
        }

        public boolean isIdentity() {
            return dx == 0f && dy == 0f && dw == 0f && dh == 0f
                    && image == null && !hidden;
        }

        public Tweak copy() {
            Tweak made = new Tweak();
            made.set(dx, dy, dw, dh);
            made.image = image;
            made.hidden = hidden;
            return made;
        }

        public void set(float x, float y, float w, float h) {
            dx = clamp(x);
            dy = clamp(y);
            dw = clamp(w);
            dh = clamp(h);
        }

        private static float clamp(float value) {
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return 0f;
            }
            return Math.max(-LIMIT, Math.min(LIMIT, Math.round(value)));
        }
    }

    public static void setScope(String value) {
        scope = (value == null || value.isEmpty()) ? DEFAULT_SCOPE : value;
    }

    public static String scope() {
        return scope;
    }

    public static Tweak tweak(String id) {
        Tweak found = id == null ? null : all().get(id);
        return found == null ? IDENTITY : found;
    }

    public static Tweak edit(String id) {
        Tweak found = all().get(id);
        if (found != null) {
            return found;
        }
        if (id == null || TWEAKS.size() >= MAX_ENTRIES) {
            Log.warn("gui", "The UI layout already holds " + MAX_ENTRIES + " tweaks");
            return IDENTITY;
        }
        Tweak made = new Tweak();
        TWEAKS.put(id, made);
        dirty = true;
        return made;
    }

    public static void restore(String id, Tweak value) {
        if (id == null || value == null) {
            return;
        }
        if (value.isIdentity()) {
            clear(id);
            return;
        }
        edit(id).set(value.getDx(), value.getDy(), value.getDw(), value.getDh());
        dirty = true;
    }

    public static void clear(String id) {
        if (id != null && all().remove(id) != null) {
            dirty = true;
        }
    }

    public static int clearScope(String name) {
        String prefix = name + "|";
        int removed = 0;
        for (String id : new ArrayList<String>(all().keySet())) {
            if (id.startsWith(prefix)) {
                TWEAKS.remove(id);
                removed++;
            }
        }
        dirty = dirty || removed > 0;
        return removed;
    }

    public static int clearAll() {
        int removed = all().size();
        TWEAKS.clear();
        BROKEN.clear();
        dirty = dirty || removed > 0;
        return removed;
    }

    public static int count() {
        return all().size();
    }

    private static Map<String, Tweak> all() {
        if (!loaded) {
            loaded = true;
            load();
        }
        return TWEAKS;
    }

    private static void load() {
        FileHandle file = Gdx.files == null ? null : Gdx.files.local(PATH);
        if (file == null || !file.exists()) {
            return;
        }
        try {
            Object parsed = Json.parse(file.readString("UTF-8"));
            if (parsed instanceof Map) {
                read((Map<?, ?>) parsed);
            }
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not read " + PATH + "; the stock layout is used");
            TWEAKS.clear();
        }
        Log.debug("gui", "UI layout loaded with " + TWEAKS.size() + " tweaks");
    }

    private static void read(Map<?, ?> rows) {
        for (Map.Entry<?, ?> entry : rows.entrySet()) {
            if (!(entry.getValue() instanceof Map) || TWEAKS.size() >= MAX_ENTRIES) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) entry.getValue();
            Tweak made = new Tweak();
            made.set((float) Json.doubleOf(row, "dx"), (float) Json.doubleOf(row, "dy"),
                    (float) Json.doubleOf(row, "dw"), (float) Json.doubleOf(row, "dh"));
            if (!made.isIdentity()) {
                Object art = ((Map<?, ?>) entry.getValue()).get("image");
                if (art != null) {
                    made.setImage(String.valueOf(art));
                }
                made.setHidden(Json.boolOf((Map<?, ?>) entry.getValue(), "hidden"));
                TWEAKS.put(String.valueOf(entry.getKey()), made);
            }
        }
    }

    public static void save() {
        if (!dirty) {
            return;
        }
        StringBuilder sb = new StringBuilder("{\n");
        boolean first = true;
        for (Map.Entry<String, Tweak> entry : all().entrySet()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            row(sb, entry.getKey(), entry.getValue());
        }
        sb.append("\n}\n");
        write(sb.toString());
    }

    private static void row(StringBuilder sb, String id, Tweak value) {
        sb.append("  \"").append(Json.escape(id)).append("\": {\"dx\": ")
                .append((int) value.getDx()).append(", \"dy\": ").append((int) value.getDy())
                .append(", \"dw\": ").append((int) value.getDw())
                .append(", \"dh\": ").append((int) value.getDh());
        if (value.getImage() != null) {
            sb.append(", \"image\": \"").append(Json.escape(value.getImage())).append('"');
        }
        if (value.isHidden()) {
            sb.append(", \"hidden\": true");
        }
        sb.append('}');
    }

    private static void write(String text) {
        try {
            Gdx.files.local(PATH).writeString(text, false, "UTF-8");
            dirty = false;
            Log.info("gui", "UI layout saved to " + PATH);
        } catch (RuntimeException e) {
            Log.warn("gui", "Could not write " + PATH);
        }
    }

    public static String kindOf(Actor actor) {
        if (actor instanceof Tunable) {
            return ((Tunable) actor).kind();
        }
        Class<?> type = actor.getClass();
        String name = type.getSimpleName();
        while (name.isEmpty() && type.getSuperclass() != null) {
            type = type.getSuperclass();
            name = type.getSimpleName();
        }
        return name.isEmpty() ? "Actor" : name;
    }

    public static boolean isTunable(Actor actor) {
        if (actor == null || actor instanceof Tunable || actor instanceof view.gui.Toasts) {
            return false;
        }
        Group parent = actor.getParent();
        if (parent == null || parent.getParent() == null) {
            return false;
        }
        if (parent instanceof SplitPane) {
            return false;
        }
        for (Group up = parent; up != null; up = up.getParent()) {
            if (up instanceof LayoutEditor) {
                return false;
            }
        }
        return true;
    }

    public static String pathOf(Actor actor) {
        if (actor == null || actor.getParent() == null) {
            return null;
        }
        if (isExtra(actor.getName())) {
            return actor.getName();
        }
        List<String> parts = new ArrayList<String>();
        Actor node = actor;
        while (node.getParent() != null && node.getParent().getParent() != null) {
            if (node.getParent() instanceof Tunable) {
                node = node.getParent();
                continue;
            }
            if (parts.size() > MAX_DEPTH) {
                return null;
            }
            parts.add(segment(node));
            node = node.getParent();
        }
        if (node.getParent() == null || parts.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(anchorScope(node)).append('|').append(kindOf(node));
        for (int i = parts.size() - 1; i >= 0; i--) {
            sb.append('/').append(parts.get(i));
        }
        return sb.toString();
    }

    private static String anchorScope(Actor anchor) {
        return anchor instanceof view.gui.Popup ? kindOf(anchor) : scope;
    }

    private static String segment(Actor node) {
        String name = node.getName();
        if (name != null && !name.isEmpty()) {
            return name.replace('/', '_').replace('|', '_');
        }
        String kind = kindOf(node);
        int index = 0;
        SnapshotArray<Actor> siblings = node.getParent().getChildren();
        for (int i = 0; i < siblings.size; i++) {
            Actor sibling = siblings.get(i);
            if (sibling == node) {
                break;
            }
            if (kindOf(sibling).equals(kind)) {
                index++;
            }
        }
        return kind + "#" + index;
    }

    public static void refresh(Group root) {
        if (root != null) {
            invalidateAll(root, 0);
        }
    }

    private static void invalidateAll(Group parent, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        for (Actor child : parent.getChildren()) {
            if (child instanceof Tunable) {
                ((Tunable) child).invalidate();
            }
            if (child instanceof Group) {
                invalidateAll((Group) child, depth + 1);
            }
        }
    }

    public static boolean covers(Actor actor, float stageX, float stageY) {
        if (actor == null || actor.getWidth() <= 0f || actor.getHeight() <= 0f) {
            return false;
        }
        actor.stageToLocalCoordinates(POINT.set(stageX, stageY));
        return POINT.x >= 0f && POINT.x < actor.getWidth()
                && POINT.y >= 0f && POINT.y < actor.getHeight();
    }

    public static Rectangle stageBounds(Actor actor) {
        BOX.set(0f, 0f, 0f, 0f);
        if (actor == null) {
            return BOX;
        }
        POINT.set(0f, 0f);
        actor.localToStageCoordinates(POINT);
        float left = POINT.x;
        float bottom = POINT.y;
        POINT.set(actor.getWidth(), actor.getHeight());
        actor.localToStageCoordinates(POINT);
        return BOX.set(Math.min(left, POINT.x), Math.min(bottom, POINT.y),
                Math.abs(POINT.x - left), Math.abs(POINT.y - bottom));
    }

    public static Actor pickAt(Group root, float stageX, float stageY) {
        List<Actor> found = candidatesAt(root, stageX, stageY);
        return found.isEmpty() ? null : found.get(0);
    }

    public static List<Actor> candidatesAt(Group root, float stageX, float stageY) {
        List<Actor> out = new ArrayList<Actor>();
        List<Actor> oversized = new ArrayList<Actor>();
        if (root != null) {
            collect(root, stageX, stageY, 0, out, oversized);
        }
        out.addAll(oversized);
        return out;
    }

    private static void collect(Group parent, float x, float y, int depth,
            List<Actor> out, List<Actor> oversized) {
        if (depth > MAX_DEPTH) {
            return;
        }
        SnapshotArray<Actor> children = parent.getChildren();
        List<Actor> shallow = new ArrayList<Actor>();
        for (int i = children.size - 1; i >= 0; i--) {
            Actor child = children.get(i);
            if (!child.isVisible() || child instanceof LayoutEditor
                    || child instanceof view.gui.Toasts) {
                continue;
            }
            int mark = out.size();
            if (child instanceof Group) {
                collect((Group) child, x, y, depth + 1, out, oversized);
            }
            for (int k = out.size() - 1; k >= mark; k--) {
                if (!fitsInside(out.get(k), child)) {
                    oversized.add(out.remove(k));
                }
            }
            if (isTunable(child) && covers(child, x, y)) {
                shallow.add(child);
            }
        }
        out.addAll(shallow);
    }

    private static boolean fitsInside(Actor inner, Actor outer) {
        float room = outer.getWidth() * outer.getHeight();
        return room <= 0f || inner.getWidth() * inner.getHeight() <= room;
    }

    public static String shortId(String id) {
        if (id == null) {
            return "";
        }
        String[] parts = id.split("/");
        if (parts.length <= SHORT_TAIL) {
            return id;
        }
        StringBuilder sb = new StringBuilder("...");
        for (int i = parts.length - SHORT_TAIL; i < parts.length; i++) {
            sb.append('/').append(parts[i]);
        }
        return sb.toString();
    }

    public static void placeAt(Group parent, Actor child,
            float x, float y, float width, float height) {
        Actor target = positioned(parent, child);
        target.setBounds(x, y, width, height);
        if (target instanceof Tunable) {
            ((Tunable) target).invalidate();
        }
    }

    public static Actor positioned(Group parent, Actor child) {
        if (parent == null || child == null) {
            return child;
        }
        Actor target = child;
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            Group holder = target.getParent();
            if (holder == null) {
                return child;
            }
            if (holder == parent) {
                return target;
            }
            target = holder;
        }
        return child;
    }

    public static String scopeName() {
        return scope;
    }

    public static String addImage(String file, float width, float height) {
        int index = 0;
        String id;
        do {
            id = scope + "|extra#" + index++;
        } while (TWEAKS.containsKey(id) && index < MAX_ENTRIES);
        Tweak made = edit(id);
        made.setImage(file);
        made.set(0f, 0f, width, height);
        return id;
    }

    public static List<String> extrasFor(String owner) {
        List<String> out = new ArrayList<String>();
        for (Map.Entry<String, Tweak> entry : TWEAKS.entrySet()) {
            if (entry.getValue().getImage() != null
                    && entry.getKey().startsWith(owner + "|extra#")) {
                out.add(entry.getKey());
            }
        }
        return out;
    }

    public static boolean isExtra(String id) {
        return id != null && id.contains("|extra#");
    }

    public static void drop(String id) {
        if (id != null && TWEAKS.remove(id) != null) {
            dirty = true;
        }
    }

    public static void hide(String id, boolean value) {
        if (id == null || isExtra(id)) {
            return;
        }
        edit(id).setHidden(value);
    }

    public static void apply(Group root) {
        if (root == null || all().isEmpty()) {
            return;
        }
        walk(root, 0);
    }

    private static void walk(Group parent, int depth) {
        if (depth > MAX_DEPTH) {
            return;
        }
        SnapshotArray<Actor> children = parent.getChildren();
        int size = children.size;
        Actor[] items = children.begin();
        for (int i = 0; i < size; i++) {
            step(parent, items[i], depth);
        }
        children.end();
    }

    private static void step(Group parent, Actor child, int depth) {
        if (child instanceof LayoutEditor) {
            return;
        }
        if (child instanceof Tunable) {
            Actor inner = ((Tunable) child).child();
            if (inner instanceof Group) {
                walk((Group) inner, depth + 1);
            }
            return;
        }
        if (depth > 0) {
            consider(parent, child);
        }
        if (child instanceof Group) {
            walk((Group) child, depth + 1);
        }
    }

    private static void consider(Group parent, Actor child) {
        if (!isTunable(child) || isExtra(child.getName())
                || view.gui.layout.Extras.LAYER.equals(parent.getName())) {
            return;
        }
        String id = pathOf(child);
        if (id == null || BROKEN.contains(id) || !TWEAKS.containsKey(id)) {
            return;
        }
        child.setVisible(!TWEAKS.get(id).isHidden());
        if (TWEAKS.get(id).isIdentity()) {
            return;
        }
        try {
            wrap(parent, child, id);
        } catch (RuntimeException e) {
            BROKEN.add(id);
            Log.warn("gui", "UI tweak " + id + " could not be applied and was skipped");
        }
    }

    private static void wrap(Group parent, Actor child, String id) {
        int index = parent.getChildren().indexOf(child, true);
        Cell<Actor> cell = parent instanceof Table ? ((Table) parent).getCell(child) : null;
        Tunable holder = new Tunable(id, child);
        if (cell != null) {
            cell.setActor(holder);
            holder.setZIndex(index);
        } else if (parent instanceof ScrollPane) {
            ((ScrollPane) parent).setActor(holder);
        } else if (parent instanceof Container) {
            asContainer(parent).setActor(holder);
        } else {
            parent.addActorAt(index, holder);
        }
        if (parent instanceof Layout) {
            ((Layout) parent).invalidate();
        }
    }

    @SuppressWarnings("unchecked")
    private static Container<Actor> asContainer(Group parent) {
        return (Container<Actor>) parent;
    }

    public static String blocked(Actor actor) {
        if (actor == null) {
            return "Nothing selected.";
        }
        String id = pathOf(actor);
        if (id == null) {
            return "This widget has no stable id and cannot be tuned.";
        }
        if (BROKEN.contains(id)) {
            return "This widget failed to wrap once and is being skipped.";
        }
        Group parent = actor.getParent();
        if (parent instanceof SplitPane) {
            return "Split panes position their own halves; tune a child instead.";
        }
        return null;
    }

    public static void unbreak(String id) {
        if (id != null) {
            BROKEN.remove(id);
        }
    }
}

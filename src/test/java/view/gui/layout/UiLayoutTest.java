package view.gui.layout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiLayoutTest {

    private static final float PANEL_WIDTH = 120f;
    private static final float PANEL_HEIGHT = 60f;
    private static final float ROW_WIDTH = 304f;
    private static final float ROW_HEIGHT = 64f;
    private static final float BAR_HEIGHT = 12f;
    private static final float BADGE = 64f;

    private final Group root = new Group();
    private final Table anchor = new Table();
    private final Table panel = new Table();
    private final Table over = new Table();
    private final Actor bar = new Actor();
    private final Actor badge = new Actor();

    @BeforeAll
    static void sandbox() {
        File temp = new File(System.getProperty("java.io.tmpdir"), "pvz-ui-layout-test");
        assertTrue(temp.isDirectory() || temp.mkdirs());
        Gdx.files = new SandboxFiles(temp);
    }

    @BeforeEach
    void setUp() {
        UiLayout.clearAll();
        UiLayout.setScope("Probe");
        root.addActor(anchor);
        panel.add(new Actor()).size(PANEL_WIDTH, PANEL_HEIGHT);
        anchor.add(panel);
        anchor.setSize(400f, 300f);
        relayout();
    }

    @AfterEach
    void tearDown() {
        UiLayout.clearAll();
    }

    private void relayout() {
        anchor.invalidate();
        anchor.validate();
    }

    private String tweakPanel(float dx, float dy, float dw, float dh) {
        String id = UiLayout.pathOf(panel);
        UiLayout.edit(id).set(dx, dy, dw, dh);
        UiLayout.apply(root);
        relayout();
        return id;
    }

    @Test
    void aPathNamesTheChainOfWidgets() {
        assertEquals("Probe|Table/Table#0", UiLayout.pathOf(panel));
    }

    @Test
    void onlyWidgetsBelowTheStageRootCanBeTuned() {
        assertFalse(UiLayout.isTunable(anchor));
        assertFalse(UiLayout.isTunable(root));
        assertTrue(UiLayout.isTunable(panel));
    }

    @Test
    void aTweakMovesAndResizesTheWidget() {
        tweakPanel(12f, -7f, 20f, 30f);

        assertTrue(panel.getParent() instanceof Tunable);
        assertEquals(12f, panel.getX(), 0.01f);
        assertEquals(-7f, panel.getY(), 0.01f);
        assertEquals(PANEL_WIDTH + 20f, panel.getWidth(), 0.01f);
        assertEquals(PANEL_HEIGHT + 30f, panel.getHeight(), 0.01f);
    }

    @Test
    void aTweakNeverChangesWhatTheParentAsksFor() {
        float width = anchor.getPrefWidth();
        float height = anchor.getPrefHeight();

        tweakPanel(90f, 90f, 200f, 200f);

        assertEquals(width, anchor.getPrefWidth(), 0.01f);
        assertEquals(height, anchor.getPrefHeight(), 0.01f);
    }

    @Test
    void theIdSurvivesTheWrapping() {
        String id = tweakPanel(5f, 5f, 0f, 0f);

        assertEquals(id, UiLayout.pathOf(panel));
        assertEquals(id, ((Tunable) panel.getParent()).id());
    }

    @Test
    void applyingTwiceWrapsOnlyOnce() {
        tweakPanel(5f, 5f, 0f, 0f);
        Actor holder = panel.getParent();

        UiLayout.apply(root);
        relayout();

        assertSame(holder, panel.getParent());
        assertSame(anchor, holder.getParent());
    }

    @Test
    void clearingRestoresTheStockLayout() {
        String id = tweakPanel(40f, 40f, 60f, 60f);

        UiLayout.clear(id);
        UiLayout.refresh(root);
        relayout();

        assertEquals(0f, panel.getX(), 0.01f);
        assertEquals(0f, panel.getY(), 0.01f);
        assertEquals(PANEL_WIDTH, panel.getWidth(), 0.01f);
        assertEquals(PANEL_HEIGHT, panel.getHeight(), 0.01f);
    }

    @Test
    void clearingAScopeLeavesOtherScreensAlone() {
        tweakPanel(4f, 4f, 0f, 0f);
        UiLayout.setScope("Other");
        UiLayout.edit(UiLayout.pathOf(panel)).set(9f, 9f, 0f, 0f);

        assertEquals(1, UiLayout.clearScope("Other"));
        assertEquals(1, UiLayout.count());
    }

    @Test
    void anUnknownIdIsIgnored() {
        UiLayout.edit("Probe|Table/Nothing#7").set(50f, 50f, 50f, 50f);
        UiLayout.apply(root);
        relayout();

        assertSame(anchor, panel.getParent());
        assertNotNull(UiLayout.pathOf(panel));
    }

    @Test
    void aWidgetIsNeverSqueezedBelowItsFloor() {
        tweakPanel(0f, 0f, -9000f, -9000f);

        assertEquals(UiLayout.MIN_SIZE, panel.getWidth(), 0.01f);
        assertEquals(UiLayout.MIN_SIZE, panel.getHeight(), 0.01f);
    }

    private void buildXpRow() {
        Table holder = new Table();
        Stack stack = new Stack();

        Table barHolder = new Table();
        barHolder.add(bar).growX().height(BAR_HEIGHT);
        stack.add(barHolder);

        over.right();
        over.add(badge).size(BADGE);
        stack.add(over);

        holder.add(stack).grow();
        anchor.row();
        anchor.add(holder).size(ROW_WIDTH, ROW_HEIGHT);
        relayout();
    }

    private Vector2 pointOn(Actor actor, float fractionX, float fractionY) {
        return actor.localToStageCoordinates(new Vector2(
                actor.getWidth() * fractionX, actor.getHeight() * fractionY));
    }

    @Test
    void aThinWidgetUnderAFullWidthLayerIsStillPicked() {
        buildXpRow();
        Vector2 at = pointOn(bar, 0.25f, 0.5f);

        assertTrue(UiLayout.covers(over, at.x, at.y));
        assertSame(bar, UiLayout.pickAt(root, at.x, at.y));
    }

    @Test
    void theWidgetOnTopWinsWhereItActuallySits() {
        buildXpRow();
        Vector2 at = pointOn(badge, 0.5f, 0.5f);

        assertTrue(UiLayout.covers(bar, at.x, at.y));
        assertSame(badge, UiLayout.pickAt(root, at.x, at.y));
    }

    @Test
    void aContainerIsPickedWhereNothingDeeperSits() {
        buildXpRow();
        Vector2 at = pointOn(over, 0.5f, 0.94f);

        assertSame(over, UiLayout.pickAt(root, at.x, at.y));
    }

    @Test
    void nothingIsPickedOutsideTheTree() {
        buildXpRow();

        assertNull(UiLayout.pickAt(root, -500f, -500f));
    }

    @Test
    void aShortIdKeepsTheTailOfThePath() {
        assertEquals(".../a/b/c", UiLayout.shortId("Screen|Root/x/y/a/b/c"));
        assertEquals("Screen|Root/a", UiLayout.shortId("Screen|Root/a"));
    }

    @Test
    void aGroupThatPositionsItsChildrenByHandStillFindsThemThroughATunable() {
        Group strip = new Group();
        Actor node = new Actor();
        strip.addActor(node);

        assertSame(node, UiLayout.positioned(strip, node),
                "an unwrapped child positions itself");

        Tunable holder = new Tunable("Strip|node", node);
        strip.addActor(holder);

        assertSame(holder, UiLayout.positioned(strip, node),
                "a wrapped child must be positioned through its Tunable, "
                        + "or absolute layouts leave it at 0x0 and it vanishes");
        assertSame(node, UiLayout.positioned((Group) holder, node),
                "inside the Tunable the child is still the direct target");
    }

    @Test
    void positionedIsSafeWhenTheActorIsNotUnderThatGroupAtAll() {
        Group strip = new Group();
        Group elsewhere = new Group();
        Actor orphan = new Actor();
        Actor adopted = new Actor();
        elsewhere.addActor(adopted);

        assertSame(orphan, UiLayout.positioned(strip, orphan));
        assertSame(adopted, UiLayout.positioned(strip, adopted));
        assertSame(adopted, UiLayout.positioned(null, adopted));
    }

    @Test
    void placeAtReappliesTheTweakWhenOnlyThePositionChanges() {
        Group strip = new Group();
        com.badlogic.gdx.scenes.scene2d.ui.Widget node =
                new com.badlogic.gdx.scenes.scene2d.ui.Widget();
        node.setSize(40f, 40f);
        strip.addActor(node);

        UiLayout.edit("Strip|node").set(11f, 7f, 0f, 0f);
        Tunable holder = new Tunable("Strip|node", node);
        strip.addActor(holder);

        UiLayout.placeAt(strip, node, 100f, 200f, 40f, 40f);
        holder.validate();
        assertEquals(11f, node.getX(), 0.01f);
        assertEquals(7f, node.getY(), 0.01f);

        UiLayout.placeAt(strip, node, 300f, 200f, 40f, 40f);
        holder.validate();
        assertEquals(300f, holder.getX(), 0.01f);
        assertEquals(11f, node.getX(), 0.01f,
                "a position-only move must still re-apply the tweak");
    }

    @Test
    void wrappingANamedWidgetKeepsItsPathId() {
        Group strip = new Group();
        Group host = new Group();
        strip.addActor(host);
        Actor node = new Actor();
        node.setName("node-1");
        node.setSize(40f, 40f);
        host.addActor(node);

        String before = UiLayout.pathOf(node);
        assertNotNull(before);
        assertTrue(before.endsWith("/node-1"), before);

        Tunable holder = new Tunable("scope|node-1", node);
        host.addActor(holder);

        assertEquals(before, UiLayout.pathOf(node),
                "a named widget must keep its id once wrapped, "
                        + "or the editor writes tweaks the Tunable never reads");
    }

    @Test
    void hidingAWidgetSurvivesASaveAndReload() {
        UiLayout.edit("Screen|thing").set(4f, 5f, 0f, 0f);
        UiLayout.save();

        UiLayout.hide("Screen|thing", true);
        UiLayout.save();
        UiLayout.reload();

        assertTrue(UiLayout.tweak("Screen|thing").isHidden(),
                "a hide that only flips the flag must still be written to disk");
        assertEquals(4f, UiLayout.tweak("Screen|thing").getDx(), 0.01f);

        UiLayout.hide("Screen|thing", false);
        UiLayout.save();
        UiLayout.reload();
        assertFalse(UiLayout.tweak("Screen|thing").isHidden());
    }
}

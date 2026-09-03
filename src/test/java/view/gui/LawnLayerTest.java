package view.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LawnLayerTest {

    private static final float TOP_ROW = 500f;
    private static final float LOWER_ROW = 400f;

    @Test
    void anythingOnALowerRowDrawsInFrontOfAnythingAbove() {
        for (LawnLayer above : LawnLayer.values()) {
            for (LawnLayer below : LawnLayer.values()) {
                if (above == LawnLayer.SUN || below == LawnLayer.SUN) {
                    continue;
                }
                assertTrue(below.depth(LOWER_ROW) < above.depth(TOP_ROW),
                        below + " on the lower row must draw in front of " + above
                                + " on the row above it");
            }
        }
    }

    @Test
    void withinOneRowTheOrderIsGraveThenMowerThenPlantThenZombieThenShot() {
        assertTrue(LawnLayer.SHOT.depth(TOP_ROW) < LawnLayer.ZOMBIE.depth(TOP_ROW),
                "a shot draws over the zombie it is about to hit");
        assertTrue(LawnLayer.ZOMBIE.depth(TOP_ROW) < LawnLayer.PLANT.depth(TOP_ROW),
                "a zombie draws over the plant it is eating");
        assertTrue(LawnLayer.PLANT.depth(TOP_ROW) < LawnLayer.MOWER_PARKED.depth(TOP_ROW),
                "a parked mower sits behind the plants");
        assertTrue(LawnLayer.MOWER_PARKED.depth(TOP_ROW)
                        < LawnLayer.TOMBSTONE.depth(TOP_ROW),
                "a tombstone sits behind everything sharing its row");
    }

    @Test
    void sunDrawsOverEverythingWhereverItIs() {
        for (LawnLayer other : LawnLayer.values()) {
            if (other == LawnLayer.SUN) {
                continue;
            }
            assertTrue(LawnLayer.SUN.depth(TOP_ROW) < other.depth(0f),
                    "sun should stay in front of " + other + " on any row");
        }
    }
}

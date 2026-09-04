package view.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IceGeometryTest {

    @Test
    void theTunerAndTheGameSizeAnIceBlockTheSameWay() {
        assertEquals(1.35f, FrostArt.BLOCK_SPAN, 0.001f,
                "one constant decides how wide an ice block is");
    }

    @Test
    void anIceBlockIsCentredOnItsCell() {
        float cell = 100f;
        float span = cell * FrostArt.BLOCK_SPAN;
        float left = 400f;
        float centredA = left + (cell - span) / 2f;
        float centredB = left + cell / 2f - span / 2f;
        assertEquals(centredA, centredB, 0.001f,
                "the tuner and the game must centre it identically");
    }
}

package model.entities.plants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PacketCompositionTest {

    @Test
    void iconOffsetsAndSizesParse() {
        PlantRecord sun = PlantData.record(Plants.SUNFLOWER);
        assertEquals(25, sun.getIconOffsetX());
        assertEquals(0, sun.getIconOffsetY());
        assertEquals(69, sun.getIconWidth());
        assertEquals(68, sun.getIconHeight());

        PlantRecord bonk = PlantData.record(Plants.BONK_CHOY);
        assertEquals(14, bonk.getIconOffsetX());
        assertEquals(-6, bonk.getIconOffsetY());
        assertEquals(89, bonk.getIconWidth());
        assertEquals(71, bonk.getIconHeight());
    }
}

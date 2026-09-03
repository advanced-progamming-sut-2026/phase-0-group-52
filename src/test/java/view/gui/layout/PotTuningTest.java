package view.gui.layout;

import com.badlogic.gdx.Gdx;
import model.entities.plants.Plants;
import model.greenhouse.Pot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import view.gui.EntityTuning;
import view.gui.widgets.PotSlot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class PotTuningTest {

    @TempDir
    java.io.File temp;

    @BeforeEach
    void sandbox() {
        Gdx.files = new SandboxFiles(temp);
        EntityTuning.reset();
    }

    @Test
    void everyPlantKeepsItsOwnSeedlingAndFullGrownTune() {
        Pot sunflower = new Pot(1, 1, true);
        sunflower.plantSpecial(Plants.SUNFLOWER);
        Pot wallnut = new Pot(2, 1, true);
        wallnut.plantSpecial(Plants.WALL_NUT);
        Pot marigold = new Pot(3, 1, true);
        marigold.plantMarigold();

        String one = PotSlot.keyFor(PotSlot.SEEDLING_KEY, sunflower);
        String two = PotSlot.keyFor(PotSlot.SEEDLING_KEY, wallnut);
        String gold = PotSlot.keyFor(PotSlot.PLANT_KEY, marigold);
        assertNotEquals(one, two, "two plants must not share a seedling tune");
        assertEquals(PotSlot.SEEDLING_KEY + "|SUNFLOWER", one, "keyed by the plant");
        assertEquals(PotSlot.PLANT_KEY + "|MARIGOLD", gold, "and the marigold has its own");

        EntityTuning.edit(one).dy = 12f;
        EntityTuning.touch();
        EntityTuning.reset();
        assertEquals(12f, EntityTuning.of(one).dy, 0.01f, "it survives a save and load");
        assertEquals(0f, EntityTuning.of(two).dy, 0.01f, "and does not leak to another plant");
    }

    @Test
    void editingAPlantsKeyChangesWhatThePotResolves() {
        Pot pot = new Pot(1, 1, true);
        pot.plantSpecial(Plants.SUNFLOWER);
        String own = PotSlot.keyFor(PotSlot.SEEDLING_KEY, pot);

        EntityTuning.edit(own).dy = 25f;
        assertEquals(25f, PotSlot.seedlingTune(pot).dy, 0.01f,
                "the pot should read the tune the tuner just wrote");

        EntityTuning.edit(PotSlot.PLANT_KEY + "|SUNFLOWER").dy = 40f;
        assertEquals(40f, PotSlot.fullTune(pot).dy, 0.01f,
                "and the same for its grown size");
    }

    @Test
    void aSeedlingStartsSmallAndItsScaleIsTheOneYouTune() {
        Pot pot = new Pot(1, 1, true);
        pot.plantSpecial(Plants.SUNFLOWER);
        assertEquals(0.35f, PotSlot.seedlingTune(pot).scale, 0.01f,
                "an untouched seedling starts at the small default");

        PotSlot.seedlingTune(pot).scale = 0.8f;
        assertEquals(0.8f, PotSlot.seedlingTune(pot).scale, 0.01f,
                "and the value you set is the value that is used, not a fraction of it");
    }

    @Test
    void aPerPlantKeyIsNeverConfusedWithThePieceItBelongsTo() {
        Pot pot = new Pot(1, 1, true);
        pot.plantSpecial(Plants.SUNFLOWER);
        String own = PotSlot.keyFor(PotSlot.SEEDLING_KEY, pot);
        assertNotEquals(PotSlot.SEEDLING_KEY, own,
                "the per-plant key is not the bare piece name");
        assertEquals(PotSlot.SEEDLING_KEY, own.substring(0, PotSlot.SEEDLING_KEY.length()),
                "but it starts with it, so callers must compare the piece, not the key");
    }

    @Test
    void aFreshlyPlantedPotIsAtItsSeedlingSizeAndAGrownOneIsNot() {
        Pot young = new Pot(1, 1, true);
        young.plantSpecial(Plants.SUNFLOWER);
        long now = System.currentTimeMillis();
        young.setTimestamps(now, now + Pot.GROW_MILLIS);
        assertEquals(0d, young.growth(), 0.02d, "a fresh pot is at the start of its growth");

        Pot grown = new Pot(2, 1, true);
        grown.plantSpecial(Plants.SUNFLOWER);
        grown.setTimestamps(now - Pot.GROW_MILLIS, now - 1L);
        assertEquals(1d, grown.growth(), 0.02d, "and a finished one is at the end");
    }

    @Test
    void theTunerAndTheGardenAgreeOnHowBigAPotIs() {
        assertEquals(152f, PotSlot.SLOT_WIDTH, 0.01f,
                "the slot width both the garden and the tuner use");
        assertEquals(156f, PotSlot.SLOT_HEIGHT, 0.01f,
                "and its height - offsets are absolute pixels, so these must match");
    }
}

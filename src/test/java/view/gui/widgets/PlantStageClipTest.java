package view.gui.widgets;

import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantStageClipTest {

    @Test
    void everyPlantResolvesToAClipItActuallyHas() {
        for (Plants plant : Plants.values()) {
            PlantRecord record = PlantData.record(plant);
            String clip = PlantStage.clipOf(record, "idle");
            assertNotNull(clip, plant.getName());
            assertTrue(record.getAnimations().hasClip(clip),
                    plant.getName() + " has no clip named " + clip);
        }
    }

    @Test
    void theFifteenRigsWithoutAnIdleClipStillResolve() {
        Plants[] awkward = {Plants.GRAVE_BUSTER, Plants.SUN_SHROOM, Plants.PUFF_SHROOM,
                Plants.DOOM_SHROOM, Plants.CAULIPOWER, Plants.ELECTRIC_BLUEBERRY,
                Plants.KIWIBEAST, Plants.ENLIGHTEN_MINT, Plants.APPEASE_MINT,
                Plants.ARMA_MINT, Plants.BOMBARD_MINT, Plants.ENFORCE_MINT,
                Plants.REINFORCE_MINT, Plants.ENCHANT_MINT, Plants.PIERCE_MINT};
        for (Plants plant : awkward) {
            PlantRecord record = PlantData.record(plant);
            assertTrue(record.getAnimations().hasClip(PlantStage.clipOf(record, "idle")),
                    plant.getName());
        }
    }
}

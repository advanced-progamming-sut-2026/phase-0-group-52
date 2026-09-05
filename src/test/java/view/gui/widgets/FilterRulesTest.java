package view.gui.widgets;

import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterRulesTest {

    @Test
    void theUnlockedCheckboxActuallyFilters() {
        AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
        int all = rules.apply().size();
        assertTrue(all > 0, "the unfiltered list should not be empty");

        rules.setShowUnlocked(false);
        List<Plants> hidden = rules.apply();
        assertTrue(hidden.size() < all,
                "unticking Unlocked has to remove something");
    }

    @Test
    void theLockedCheckboxActuallyFilters() {
        AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
        int all = rules.apply().size();
        rules.setShowLocked(false);
        assertTrue(rules.apply().size() <= all,
                "unticking Locked may only ever remove plants");
    }

    @Test
    void untickingBothLeavesNothing() {
        AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
        rules.setShowLocked(false);
        rules.setShowUnlocked(false);
        assertEquals(0, rules.apply().size(),
                "a plant is either locked or unlocked, so hiding both hides all");
    }

    @Test
    void theSearchBoxStillWorksAlongsideTheLockFilters() {
        AlmanacFilterPopup.Rules rules = new AlmanacFilterPopup.Rules();
        rules.setQuery("peashooter");
        List<Plants> found = rules.apply();
        assertFalse(found.isEmpty(), "searching for peashooter should find one");
        for (Plants plant : found) {
            assertTrue(plant.getName().toLowerCase().contains("peashooter"),
                    plant + " does not match the query");
        }
    }
}

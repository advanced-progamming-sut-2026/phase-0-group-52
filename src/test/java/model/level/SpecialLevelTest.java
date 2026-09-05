package model.level;

import model.App;
import model.ChapterType;
import model.Game;
import model.LevelBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialLevelTest {

    @Test
    void everySpecialLevelBuilds() {
        for (SpecialLevel special : SpecialLevel.values()) {
            Game game = LevelBuilder.buildSpecial(App.getInstance(), ChapterType.ANCIENT_EGYPT,
                    2, special.getKey());
            assertNotNull(game, special + " must build a game");
            assertNotNull(game.getLevel(), special + " must carry a Level");
            assertFalse(game.getWaves().isEmpty(), special + " needs waves");
        }
    }

    @Test
    void everyLevelStatesWhatItWantsFromYou() {
        Set<String> objectives = new HashSet<String>();
        Set<String> tags = new HashSet<String>();
        for (SpecialLevel special : SpecialLevel.values()) {
            Game game = LevelBuilder.buildSpecial(App.getInstance(), ChapterType.ANCIENT_EGYPT,
                    2, special.getKey());
            String goal = game.getLevel().objective();
            String tag = game.getLevel().objectiveTag();
            assertTrue(goal != null && !goal.isEmpty(), special + " needs an objective");
            assertTrue(tag != null && !tag.isEmpty(), special + " needs a short tag");
            objectives.add(goal);
            tags.add(tag);
        }
        assertEquals(SpecialLevel.values().length, objectives.size(),
                "no two specials may share an objective");
        assertEquals(SpecialLevel.values().length, tags.size(),
                "no two specials may share a tag");
    }

    @Test
    void anOrdinaryLevelAlsoCarriesALevelAndAnObjective() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.FROSTBITE_CAVES, 1);
        assertNotNull(game.getLevel(),
                "a plain level needs a Level or victory and defeat never run");
        assertEquals("DEFEND THE HOUSE", game.getLevel().objectiveTag());
    }

    @Test
    void lockedAndLoadedComesLateEnoughToHavePlantsForIt() {
        assertFalse(java.util.Arrays.asList(SpecialLevel.of(ChapterType.ANCIENT_EGYPT))
                        .contains(SpecialLevel.LOCKED_PLANTS),
                "taking plants away in the first world leaves nothing to play with");
        assertTrue(java.util.Arrays.asList(SpecialLevel.of(ChapterType.BIG_WAVE_BEACH))
                        .contains(SpecialLevel.LOCKED_PLANTS),
                "it belongs in a late world where the deck is deep");
    }

    @Test
    void everySpecialIsUsedByExactlyOneChapter() {
        Set<SpecialLevel> used = new HashSet<SpecialLevel>();
        for (ChapterType chapter : ChapterType.values()) {
            for (SpecialLevel special : SpecialLevel.of(chapter)) {
                assertTrue(used.add(special),
                        special + " is claimed by more than one chapter");
            }
        }
        assertEquals(SpecialLevel.values().length, used.size(),
                "every special level should appear somewhere");
    }
}

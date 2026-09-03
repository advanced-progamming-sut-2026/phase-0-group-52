package model.level;

import model.ChapterType;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WavePatternTest {

    @Test
    void everyChapterHasAWaveRosterForEveryLevel() {
        for (ChapterType chapter : ChapterType.values()) {
            for (int level = 1; level <= ChapterType.LEVELS_PER_CHAPTER; level++) {
                List<Zombies> roster = WavePlan.roster(chapter, level);
                assertFalse(roster.isEmpty(),
                        chapter.name() + " level " + level + " has nobody to send");
            }
        }
    }

    @Test
    void aChapterGetsHarderAsItsLevelsGoOn() {
        for (ChapterType chapter : ChapterType.values()) {
            int first = WavePlan.roster(chapter, 1).size();
            int last = WavePlan.roster(chapter, ChapterType.LEVELS_PER_CHAPTER).size();
            assertTrue(last >= first,
                    chapter.name() + " does not grow: " + first + " then " + last);
        }
    }

    @Test
    void everyChapterFieldsItsOwnZombiesAndThePlainOnes() {
        assertTrue(WavePlan.roster(ChapterType.FROSTBITE_CAVES, 1)
                .contains(Zombies.ZOMBIE_ICE_AGE_DODO), "Frostbite fields its dodo");
        assertTrue(WavePlan.roster(ChapterType.DARK_AGES, 1)
                .contains(Zombies.ZOMBIE_WIZARD), "Dark Ages fields its wizard");
        assertTrue(WavePlan.roster(ChapterType.BIG_WAVE_BEACH, 1)
                .contains(Zombies.ZOMBIE_BEACH_SNORKEL), "the Beach fields its snorkeler");
        for (ChapterType chapter : ChapterType.values()) {
            assertTrue(WavePlan.roster(chapter, 1).contains(Zombies.ZOMBIE_DEFAULT),
                    chapter.name() + " should still see plain zombies");
        }
    }

    @Test
    void everySpecialLevelHasSomeoneToFight() {
        for (ChapterType chapter : ChapterType.values()) {
            for (SpecialLevel special : SpecialLevel.of(chapter)) {
                List<Zombies> roster = WavePlan.restrict(
                        WavePlan.roster(chapter, ChapterType.LEVELS_PER_CHAPTER), special);
                assertFalse(roster.isEmpty(),
                        chapter.name() + " / " + special.name() + " has an empty roster");
            }
        }
    }

    @Test
    void gentleSpecialsLeaveOutTheGiants() {
        List<Zombies> soft = WavePlan.restrict(
                WavePlan.roster(ChapterType.ANCIENT_EGYPT, 4), SpecialLevel.SAVE_OUR_SEEDS);
        assertFalse(soft.contains(Zombies.ZOMBIE_GARGANTUAR),
                "Save Our Seeds should not throw a Gargantuar at defenceless plants");
        assertFalse(soft.contains(Zombies.ZOMBIE_TOMB_RAISER),
                "nor a Tomb Raiser burying the tiles you must protect");
    }
}

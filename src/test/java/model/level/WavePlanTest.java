package model.level;

import model.ChapterType;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WavePlanTest {

    @Test
    void everyChapterOpensWithMostOfItsRoster() {
        for (ChapterType chapter : ChapterType.values()) {
            List<Zombies> first = WavePlan.roster(chapter, 1);
            List<Zombies> last = WavePlan.roster(chapter, ChapterType.LEVELS_PER_CHAPTER);
            assertTrue(first.size() >= last.size() - 2,
                    chapter + " holds back too much from level 1");
            assertTrue(first.contains(Zombies.ZOMBIE_DEFAULT), chapter.toString());
        }
    }

    @Test
    void harderDifficultyBuysMoreZombies() {
        double easy = WavePlan.budget(1, 1, 0, 4);
        double normal = WavePlan.budget(1, WavePlan.NORMAL_DIFFICULTY, 0, 4);
        double brutal = WavePlan.budget(1, 5, 0, 4);
        assertTrue(easy < normal, "easy should be lighter than normal");
        assertTrue(normal < brutal, "impossible should be heavier than normal");
    }

    @Test
    void laterWavesAndLevelsGetHeavier() {
        assertTrue(WavePlan.budget(1, 3, 0, 6) < WavePlan.budget(1, 3, 3, 6));
        assertTrue(WavePlan.budget(1, 3, 0, 6) < WavePlan.budget(4, 3, 0, 6));
    }

    @Test
    void theFinalWaveIsAFlagWave() {
        assertTrue(WavePlan.isFlagWave(5, 6));
        assertFalse(WavePlan.isFlagWave(1, 6));
    }

    @Test
    void tombRaiserIsKeptOutOfLevelsItWouldBreak() {
        List<Zombies> roster = WavePlan.roster(ChapterType.ANCIENT_EGYPT, 4);
        assertTrue(roster.contains(Zombies.ZOMBIE_TOMB_RAISER));
        assertFalse(WavePlan.restrict(roster, SpecialLevel.SAVE_OUR_SEEDS)
                .contains(Zombies.ZOMBIE_TOMB_RAISER));
        assertFalse(WavePlan.restrict(roster, SpecialLevel.LOCKED_PLANTS)
                .contains(Zombies.ZOMBIE_TOMB_RAISER));
    }

    @Test
    void compositionStaysWithinBudgetAndIsNeverEmpty() {
        List<Zombies> roster = WavePlan.roster(ChapterType.ANCIENT_EGYPT, 1);
        for (int seed = 0; seed < 20; seed++) {
            List<Zombies> wave = WavePlan.compose(roster, 300d, new Random(seed));
            assertFalse(wave.isEmpty(), "a wave must contain at least one zombie");
        }
        assertEquals(1, WavePlan.compose(roster, 1d, new Random(0)).size(),
                "a tiny budget still yields exactly one zombie");
    }
}

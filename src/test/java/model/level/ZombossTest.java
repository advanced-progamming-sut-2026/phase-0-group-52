package model.level;

import model.App;
import model.ChapterType;
import model.Game;
import model.LevelBuilder;
import model.entities.zombies.types.Zomboss;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZombossTest {

    private ZombossLevel fight(ChapterType chapter) {
        Game game = LevelBuilder.build(App.getInstance(), chapter,
                ChapterType.LEVELS_PER_CHAPTER);
        assertTrue(game.getLevel() instanceof ZombossLevel,
                chapter + " must end in a Zomboss fight");
        ZombossLevel level = (ZombossLevel) game.getLevel();
        level.onTick(game);
        return level;
    }

    @Test
    void everyChapterEndsInABossFightThatSuppliesItsOwnPlants() {
        for (ChapterType chapter : ChapterType.values()) {
            ZombossLevel level = fight(chapter);
            assertNotNull(level.getBoss(), chapter + " must put a boss on the lawn");
            assertFalse(level.getBelt().isEmpty(),
                    chapter + " boss levels have no seed picking, so the belt must stock");
            assertFalse(level.isSkySunEnabled(), chapter + " boss levels drop no sky sun");
        }
    }

    @Test
    void theBossFillsTwoRowsSoTwoRowsOfPlantsCanHitIt() {
        Zomboss boss = fight(ChapterType.ANCIENT_EGYPT).getBoss();
        assertEquals(2, boss.rowSpan(), "the machine covers two lanes");
        assertTrue(boss.occupiesRow(boss.getRow()), "its own row");
        assertTrue(boss.occupiesRow(boss.getRow() + 1), "and the one below");
        assertFalse(boss.occupiesRow(boss.getRow() + 2), "but no further");
    }

    @Test
    void healthIsThreeSegmentsAndLosingOneStunsIt() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.DARK_AGES,
                ChapterType.LEVELS_PER_CHAPTER);
        ZombossLevel level = (ZombossLevel) game.getLevel();
        level.onTick(game);
        Zomboss boss = level.getBoss();
        assertEquals(Zomboss.SEGMENTS, boss.segmentsLeft(), "it starts at full health");
        assertFalse(boss.isStunned(), "and is not stunned");

        boss.setHp(boss.maxHp() - Zomboss.SEGMENT_HP - 1d);
        boss.onTick(game);
        assertTrue(boss.isStunned(), "losing a segment leaves it dazed");
        assertEquals(Zomboss.SEGMENTS - 1, boss.segmentsLeft(), "one segment gone");
    }

    @Test
    void theMammothNeitherRoamsNorSummons() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.FROSTBITE_CAVES,
                ChapterType.LEVELS_PER_CHAPTER);
        ZombossLevel level = (ZombossLevel) game.getLevel();
        level.onTick(game);
        Zomboss boss = level.getBoss();
        int startRow = boss.getRow();
        int startZombies = game.getZombies().size();
        for (int tick = 0; tick < 400; tick++) {
            game.setCurrentTick(game.getCurrentTick() + 1);
            boss.onTick(game);
        }
        assertEquals(startRow, boss.getRow(), "the mammoth stays in its lanes");
        assertEquals(startZombies, game.getZombies().size(),
                "and never calls in help");
    }

    @Test
    void theDragonBurnsGroundThatCoolsOffAgain() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.DARK_AGES,
                ChapterType.LEVELS_PER_CHAPTER);
        ZombossLevel level = (ZombossLevel) game.getLevel();
        level.onTick(game);
        model.entities.Cell cell = game.getField().getCell(3, 2);
        cell.burn(game.getCurrentTick() + 5);
        assertTrue(cell.getType().isBurning(), "the tile is alight");
        assertFalse(cell.getType().isPlantable(), "and cannot be planted on");
        game.setCurrentTick(game.getCurrentTick() + 6);
        level.onTick(game);
        assertFalse(cell.getType().isBurning(), "it cools off on its own");
        assertTrue(cell.getType().isPlantable(), "and takes plants again");
    }
}

package model;

import model.entities.Cell;
import model.entities.CellType;
import model.entities.Tombstone;
import model.entities.zombies.Zombie;
import model.mechanics.ChapterMechanics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelHygieneTest {

    private boolean hasStone(Game game, int column, int row) {
        for (Tombstone stone : game.getTombstones()) {
            if (stone.getColumn() == column && stone.getRow() == row) {
                return true;
            }
        }
        return false;
    }

    private int ghostCells(Game game) {
        int ghosts = 0;
        for (int c = 0; c < game.getField().getCols(); c++) {
            for (int r = 0; r < game.getField().getRows(); r++) {
                Cell cell = game.getField().getCell(c, r);
                if (cell != null && cell.getType() == CellType.TOMBSTONE
                        && !hasStone(game, c, r)) {
                    ghosts++;
                }
            }
        }
        return ghosts;
    }

    @Test
    void noChapterLeavesABlockedTileWithNothingStandingOnIt() {
        for (ChapterType chapter : ChapterType.values()) {
            Game game = LevelBuilder.build(App.getInstance(), chapter, 2);
            assertEquals(0, ghostCells(game),
                    chapter + " starts with a tombstone cell that has no tombstone");
            ChapterMechanics mechanics = ChapterMechanics.forChapter(chapter);
            if (mechanics == null) {
                continue;
            }
            for (int tick = 0; tick < 40; tick++) {
                game.setCurrentTick(game.getCurrentTick() + 1);
                mechanics.onTick(game);
            }
            assertEquals(0, ghostCells(game),
                    chapter + " grew a tombstone cell with no tombstone on it");
        }
    }

    @Test
    void aPlantFoodCarrierIsMarkedAndIsTheOnlyThingThatDropsFood() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.ANCIENT_EGYPT, 3);
        boolean anyCarrier = false;
        for (Wave wave : game.getWaves()) {
            for (Zombie zombie : wave.getZombies()) {
                anyCarrier = anyCarrier || zombie.carriesPlantFood();
            }
        }
        assertTrue(anyCarrier, "later waves must send a zombie carrying plant food");

        assertEquals(0, game.getWaves().get(0).getZombies().isEmpty() ? 0
                : countCarriers(game.getWaves().get(0)),
                "the opening wave should not carry plant food");
    }

    private int countCarriers(Wave wave) {
        int carriers = 0;
        for (Zombie zombie : wave.getZombies()) {
            if (zombie.carriesPlantFood()) {
                carriers++;
            }
        }
        return carriers;
    }

    @Test
    void killingTheCarrierBanksExactlyOnePlantFood() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.ANCIENT_EGYPT, 2);
        game.setPlantFoodCount(0);
        Zombie carrier = model.entities.zombies.ZombieFactory.create(
                model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 1, 5,
                ChapterType.ANCIENT_EGYPT);
        carrier.setCarriesPlantFood(true);
        carrier.setHp(0);
        game.getZombies().add(carrier);
        model.entities.plants.PlantCombat.removeDeadZombies(game);
        assertEquals(1, game.getPlantFoodCount(),
                "the carrier hands over one plant food");
        assertFalse(carrier.carriesPlantFood(),
                "and it cannot pay out twice");
    }

    @Test
    void anOrdinaryZombieNeverDropsPlantFood() {
        Game game = LevelBuilder.build(App.getInstance(), ChapterType.ANCIENT_EGYPT, 2);
        game.setPlantFoodCount(0);
        for (int i = 0; i < 200; i++) {
            Zombie plain = model.entities.zombies.ZombieFactory.create(
                    model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 1, 5,
                    ChapterType.ANCIENT_EGYPT);
            plain.setHp(0);
            game.getZombies().add(plain);
            model.entities.plants.PlantCombat.removeDeadZombies(game);
        }
        assertEquals(0, game.getPlantFoodCount(),
                "plant food comes from the marked carrier, never from a lucky roll");
    }
}

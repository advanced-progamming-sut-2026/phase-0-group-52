package model.entities.plants;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class EveryPlantTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.ANCIENT_EGYPT);
        Game game = new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
        game.setEndless(true);
        game.getTombstones().clear();
        for (int c = 0; c < field.getCols(); c++) {
            for (int r = 0; r < field.getRows(); r++) {
                Cell cell = field.getCell(c, r);
                if (cell != null && cell.getType() == CellType.TOMBSTONE) {
                    cell.setType(CellType.NORMAL);
                }
            }
        }
        return game;
    }

    private void crowd(Game game) {
        for (int row = 0; row < game.getField().getRows(); row++) {
            for (int at = 3; at <= 7; at += 4) {
                Zombie made = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, row, at,
                        ChapterType.ANCIENT_EGYPT);
                made.setHp(9999d);
                game.getZombies().add(made);
            }
        }
    }

    @Test
    void everyPlantSurvivesBeingPlantedTickedAndFed() {
        for (Plants type : Plants.values()) {
            Game game = lawn();
            crowd(game);
            try {
                Plant made = PlantFactory.create(type, new Vec2(2, 2));
                assertNotNull(made, type.getName() + " has no factory entry");
                game.getField().getCell(2, 2).getPlants().add(made);
                game.getPlants().add(made);
                made.onPlanted(game);
                GameLoop loop = new GameLoop(game);
                for (int i = 0; i < 40; i++) {
                    loop.step(game);
                }
                for (Plant alive : new ArrayList<Plant>(game.getPlants())) {
                    alive.onPlantFood(game);
                }
                for (int i = 0; i < 60; i++) {
                    loop.step(game);
                }
            } catch (RuntimeException e) {
                fail(type.getName() + " blew up during a normal life cycle: " + e);
            }
        }
    }

    @Test
    void everyPlantResolvesToArtThatIsActuallyInTheBundle() {
        for (Plants type : Plants.values()) {
            PlantRecord record = PlantData.record(type);
            if (record == null || record.getAnimations() == null
                    || !record.getAnimations().hasPlant()) {
                continue;
            }
            String rig = record.getAnimations().getPlant();
            assertTrue(new java.io.File("assets/pvz/IMAGES/" + rig).exists(),
                    type.getName() + " points at a rig that is not bundled: " + rig);
        }
    }

    @Test
    void everyPlantThatFiresResolvesAShotAndASplat() {
        for (Plants type : Plants.values()) {
            Plant made = PlantFactory.create(type, new Vec2(0, 0));
            if (!(made instanceof Shooter) && !(made instanceof Lobber)) {
                continue;
            }
            model.entities.Projectile.Kind kind =
                    model.entities.Projectile.kindOf(type);
            for (String state : new String[] {"", model.entities.Projectile.FED}) {
                String shot = view.gui.ShotArt.rig(type, kind, state);
                String splat = view.gui.ShotArt.splatRig(type, state);
                assertTrue(new java.io.File("assets/pvz/IMAGES/" + shot).exists(),
                        type.getName() + " (" + state + ") wants a missing shot: " + shot);
                assertTrue(new java.io.File("assets/pvz/IMAGES/" + splat).exists(),
                        type.getName() + " (" + state + ") wants a missing splat: " + splat);
            }
        }
    }
}

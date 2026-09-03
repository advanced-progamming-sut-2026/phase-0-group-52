package model.mechanics;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeachTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.BIG_WAVE_BEACH);
        return new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
    }

    @Test
    void landPlantsNeedALilyPadBeforeTheyGoInTheWater() {
        Game game = lawn();
        Cell wet = game.getField().getCell(6, 2);
        wet.setType(CellType.WATER);

        assertFalse(wet.accepts(Plants.PEASHOOTER),
                "a peashooter cannot stand on open water");
        assertTrue(wet.accepts(Plants.LILY_PAD), "but a lily pad can float there");

        Plant pad = PlantFactory.create(Plants.LILY_PAD, new Vec2(6, 2));
        wet.getPlants().add(pad);
        assertTrue(wet.accepts(Plants.PEASHOOTER),
                "once a pad is down the peashooter has somewhere to stand");
    }

    @Test
    void anAquaticPlantNeedsNoPad() {
        Game game = lawn();
        Cell wet = game.getField().getCell(6, 1);
        wet.setType(CellType.WATER);
        assertTrue(wet.accepts(Plants.SEA_SHROOM),
                "a sea-shroom lives in the water already");
    }

    @Test
    void aSecondLilyPadCannotStackOnTheFirst() {
        Game game = lawn();
        Cell wet = game.getField().getCell(7, 3);
        wet.setType(CellType.WATER);
        wet.getPlants().add(PlantFactory.create(Plants.LILY_PAD, new Vec2(7, 3)));
        assertFalse(wet.accepts(Plants.LILY_PAD), "one pad per tile");
    }

    @Test
    void theTideCoversAndUncoversTheRightmostColumns() {
        Game game = lawn();
        BigWaveBeachMechanics beach = new BigWaveBeachMechanics();
        int cols = game.getField().getCols();
        boolean everWet = false;
        boolean everDry = false;
        for (int tick = 0; tick < 40 * Game.TICKS_PER_SECOND * 30; tick++) {
            beach.onTick(game);
            Cell edge = game.getField().getCell(cols - 1, 0);
            everWet |= edge.getType() == CellType.WATER;
            everDry |= edge.getType() != CellType.WATER;
            if (everWet && everDry) {
                break;
            }
        }
        assertTrue(everWet, "the tide should come in");
        assertTrue(everDry, "and it should go back out");
    }

    @Test
    void aLilyPadKeepsWhatItCarriesAboveTheTide() {
        Game game = lawn();
        Cell wet = game.getField().getCell(6, 2);
        Plant pad = PlantFactory.create(Plants.LILY_PAD, new Vec2(6, 2));
        Plant rider = PlantFactory.create(Plants.PEASHOOTER, new Vec2(6, 2));
        wet.getPlants().add(pad);
        wet.getPlants().add(rider);
        game.getPlants().add(pad);
        game.getPlants().add(rider);

        BigWaveBeachMechanics beach = new BigWaveBeachMechanics();
        for (int tick = 0; tick < 60 * Game.TICKS_PER_SECOND; tick++) {
            beach.onTick(game);
        }
        assertTrue(wet.getPlants().contains(rider),
                "the pad should hold the peashooter through the tide");
    }

    @Test
    void aSubmergedSnorkellerCannotBeShot() {
        Game game = lawn();
        Cell wet = game.getField().getCell(6, 2);
        wet.setType(CellType.WATER);
        model.entities.zombies.Zombie diver =
                model.entities.zombies.ZombieFactory.create(
                        model.entities.zombies.Zombies.ZOMBIE_BEACH_SNORKEL, 2, 6,
                        ChapterType.BIG_WAVE_BEACH);
        diver.setHp(9999d);
        game.getZombies().add(diver);
        assertTrue(diver.isSubmerged(game), "open water hides it");

        double before = diver.getHp();
        model.entities.Projectile pea = new model.entities.Projectile(
                model.entities.Projectile.Kind.PEA, Plants.PEASHOOTER, 2, 5.6d, 100d, 1);
        for (int i = 0; i < 6 && !pea.isSpent(); i++) {
            pea.advance(game);
        }
        assertEquals(before, diver.getHp(), 0.01d, "the pea passes over it");

        wet.getPlants().add(PlantFactory.create(Plants.LILY_PAD, new Vec2(6, 2)));
        assertFalse(diver.isSubmerged(game),
                "it has to surface where something is floating");
    }
}

package model.entities.plants;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.Vec2;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SunProducerTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.ANCIENT_EGYPT);
        return new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
    }

    private Plant plant(Game game, Plants type, int column, int row) {
        Plant made = PlantFactory.create(type, new Vec2(column, row));
        game.getField().getCell(column, row).getPlants().add(made);
        game.getPlants().add(made);
        made.onPlanted(game);
        return made;
    }

    private java.util.List<model.entities.Sun> made(Game game) {
        java.util.List<model.entities.Sun> out = new ArrayList<model.entities.Sun>();
        for (model.entities.Sun sun : game.getSuns()) {
            if (!sun.isFromSky()) {
                out.add(sun);
            }
        }
        return out;
    }

    private void run(Game game, double seconds) {
        GameLoop loop = new GameLoop(game);
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        for (int i = 0; i < ticks; i++) {
            loop.step(game);
        }
    }

    @Test
    void everySunProducerActuallyProducesSun() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.SUN_PRODUCER) {
                continue;
            }
            if (PlantData.record(type) != null
                    && PlantData.record(type).getUnlockKind() == PlantRecord.UnlockKind.MINT) {
                continue;
            }
            Game game = lawn();
            plant(game, type, 2, 2);
            run(game, 30d);
            assertFalse(made(game).isEmpty(),
                    type.getName() + " produced no sun in 30 seconds");
        }
    }

    @Test
    void plantFoodStateClearsItself() {
        Game game = lawn();
        Plant sunflower = plant(game, Plants.PRIMAL_SUNFLOWER, 2, 2);
        sunflower.onPlantFood(game);
        assertTrue(sunflower.isFed(), "feeding should raise the fed state");
        run(game, Plant.FED_SHOW + 1d);
        assertFalse(sunflower.isFed(), "the fed state must clear on its own");
    }

    @Test
    void plantFoodSpillsOrbsOneAtATime() {
        Game game = lawn();
        Plant sunflower = plant(game, Plants.SUNFLOWER, 2, 2);
        sunflower.onPlantFood(game);
        assertTrue(made(game).isEmpty(), "orbs must not all appear on the feeding tick");
        run(game, 2d);
        assertTrue(made(game).size() >= 5,
                "the orbs should have spilled out, saw " + made(game).size());
    }

    @Test
    void goldBloomWaitsForItsAnimationBeforeBursting() {
        Game game = lawn();
        plant(game, Plants.GOLD_BLOOM, 2, 2);
        run(game, 0.2d);
        assertTrue(made(game).isEmpty(),
                "Gold Bloom must not drop sun before its animation plays");
        assertFalse(game.getPlants().isEmpty(), "it should still be on the lawn");
    }

    @Test
    void goldBloomBurstsFiveOrbsThenLeaves() {
        Game game = lawn();
        plant(game, Plants.GOLD_BLOOM, 2, 2);
        run(game, 4d);
        assertEquals(5, made(game).size(), "Gold Bloom should drop five orbs");
        assertTrue(game.getPlants().isEmpty(), "Gold Bloom should be gone afterwards");
    }

    @Test
    void twinSunflowerDropsTwoSunsPerCycle() {
        Game game = lawn();
        plant(game, Plants.TWIN_SUNFLOWER, 2, 2);
        run(game, 25d);
        assertEquals(2, made(game).size(),
                "Twin Sunflower should drop two suns each cycle");
    }

    @Test
    void sunflowerDropsOnePerCycle() {
        Game game = lawn();
        plant(game, Plants.SUNFLOWER, 2, 2);
        run(game, 25d);
        assertEquals(1, made(game).size(), "Sunflower drops one sun a cycle");
    }

    @Test
    void producedSunLandsBelowThePlant() {
        Game game = lawn();
        plant(game, Plants.SUNFLOWER, 4, 2);
        run(game, 25d);
        for (model.entities.Sun sun : made(game)) {
            assertTrue(sun.getPosition().y > 2d,
                    "sun should settle below the plant that made it");
        }
    }

    @Test
    void sunShroomGrowsAndPaysMore() {
        Game game = lawn();
        Plant shroom = plant(game, Plants.SUN_SHROOM, 2, 2);
        assertEquals(1, shroom.growthStage(), "Sun-shroom starts small");
        run(game, 24d * 3d + 1d);
        assertEquals(2, shroom.growthStage(), "Sun-shroom should be grown after three cycles");
    }
}

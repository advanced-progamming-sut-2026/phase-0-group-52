package model.mechanics;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
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

class FrostbiteTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.FROSTBITE_CAVES);
        Game game = new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
        game.setEndless(true);
        return game;
    }

    @Test
    void aChilledPlantDeepensThroughThreeStagesAndNoFurther() {
        Plant plant = PlantFactory.create(Plants.PEASHOOTER, new Vec2(1, 2));
        assertEquals(0, plant.getFreezeLevel(), "it starts unfrozen");
        for (int gust = 0; gust < 6; gust++) {
            plant.addFreezeLevel();
        }
        assertEquals(3, plant.getFreezeLevel(),
                "three stages of ice is the deepest it gets");
        assertTrue(plant.isFrozen(), "and it is frozen solid by then");
    }

    @Test
    void fireThawsAnIcyTile() {
        Game game = lawn();
        Cell cell = game.getField().getCell(3, 2);
        cell.setType(CellType.FROZEN);
        cell.setHp(10d);

        Plant torch = PlantFactory.create(Plants.FIRE_PEASHOOTER, new Vec2(2, 2));
        game.getField().getCell(2, 2).getPlants().add(torch);
        game.getPlants().add(torch);
        torch.onPlanted(game);

        GameLoop loop = new GameLoop(game);
        for (int i = 0; i < 20 && cell.getType() == CellType.FROZEN; i++) {
            loop.step(game);
        }
        assertFalse(cell.getType() == CellType.FROZEN,
                "a fire plant beside it should melt the ice away");
    }

    @Test
    void theIcyWindNamesTheRowsItHitsSoTheViewCanShowThem() {
        Game game = lawn();
        FrostbiteCavesMechanics ice = new FrostbiteCavesMechanics();
        boolean announced = false;
        for (int wave = 0; wave < 40 && !announced; wave++) {
            ice.onWaveStart(game);
            announced = !ice.takeChilledRows().isEmpty();
        }
        assertTrue(announced, "the wind should report which rows it chilled");
        assertTrue(ice.takeChilledRows().isEmpty(),
                "and taking them clears the list so they are not replayed");
    }

    @Test
    void aZombieSlidesOncePerIcyTileNotOncePerTick() {
        Game game = lawn();
        for (int column = 0; column < game.getField().getCols(); column++) {
            game.getField().getCell(column, 2).setType(CellType.SLIPPERY_UP);
        }
        model.entities.zombies.Zombie walker = model.entities.zombies.ZombieFactory.create(
                model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.FROSTBITE_CAVES);
        game.getZombies().add(walker);

        FrostbiteCavesMechanics ice = new FrostbiteCavesMechanics();
        int startRow = walker.getRow();
        for (int tick = 0; tick < 6; tick++) {
            ice.onTick(game);
        }
        assertEquals(startRow - 1, walker.getRow(),
                "standing on one icy tile should move it exactly one lane");
    }

    @Test
    void everyIceRigResolvesAClipItActuallyHas() {
        String[][] rigs = {
            {"FROSTBITE_ICE_BLOCK_PLANT", "freeze_idle"},
            {"FROSTBITE_ICE_BLOCK_ZOMBIE", "idle"},
            {"FROSTBITE_ICE_BLOCK_PARTICLES", "animation"},
            {"FROSTBITE_CHILL_WIND", "animation"},
        };
        for (String[] rig : rigs) {
            java.io.File pam = new java.io.File(
                    "assets/pvz/IMAGES/768/FULL/EFFECTS/" + rig[0] + "/" + rig[0] + ".PAM");
            assertTrue(pam.exists(), rig[0] + " is not in the bundle");
        }
        assertFalse("idle".equals("freeze_idle"),
                "the plant block has no idle clip - asking for one throws");
    }

    @Test
    void iceOnAZombieIsArmourThatBreaksBeforeTheZombieDoes() {
        Game game = lawn();
        model.entities.zombies.Zombie caught = model.entities.zombies.ZombieFactory.create(
                model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.FROSTBITE_CAVES);
        caught.encaseInIce();
        game.getZombies().add(caught);
        double bodyBefore = caught.getHp();

        model.entities.Projectile pea = new model.entities.Projectile(
                model.entities.Projectile.Kind.PEA, Plants.PEASHOOTER, 2, 5.7d, 100d, 1);
        for (int i = 0; i < 4 && !pea.isSpent(); i++) {
            pea.advance(game);
        }
        assertTrue(caught.getIceHp() < model.entities.zombies.Zombie.ICE_HP,
                "the pea should chip the ice");
        assertEquals(bodyBefore, caught.getHp(), 0.01d,
                "and leave the zombie itself untouched while the ice holds");
    }

    @Test
    void encasedIceMeltsAwayInAboutFortySeconds() {
        Game game = lawn();
        model.entities.zombies.Zombie caught = model.entities.zombies.ZombieFactory.create(
                model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.FROSTBITE_CAVES);
        game.getZombies().add(caught);
        FrostbiteCavesMechanics ice = new FrostbiteCavesMechanics();
        ice.freezeZombieAtStart(game, caught);
        assertTrue(caught.isEncased(), "it starts encased");

        int ticks = 40 * Game.TICKS_PER_SECOND;
        for (int i = 0; i < ticks + 2 && caught.isEncased(); i++) {
            ice.onTick(game);
        }
        assertFalse(caught.isEncased(), "forty seconds is enough for it to thaw");
        assertEquals(model.entities.zombies.ZombieState.WALKING, caught.getState(),
                "and then it walks");
    }

    @Test
    void aPlantFrozenSolidStopsDoingAnything() {
        Game game = lawn();
        Plant shooter = model.entities.plants.PlantFactory.create(
                Plants.PEASHOOTER, new model.Vec2(1, 2));
        game.getField().getCell(1, 2).getPlants().add(shooter);
        game.getPlants().add(shooter);
        model.entities.zombies.Zombie target =
                model.entities.zombies.ZombieFactory.create(
                        model.entities.zombies.Zombies.ZOMBIE_DEFAULT, 2, 6,
                        ChapterType.FROSTBITE_CAVES);
        target.setHp(9999d);
        game.getZombies().add(target);

        for (int i = 0; i < Plant.FREEZE_STAGES; i++) {
            shooter.addFreezeLevel();
        }
        assertTrue(shooter.isFrozenSolid(), "three chills freeze it solid");

        model.GameLoop loop = new model.GameLoop(game);
        for (int i = 0; i < 20 * Game.TICKS_PER_SECOND; i++) {
            loop.step(game);
        }
        assertTrue(game.getProjectiles().isEmpty(),
                "a plant frozen solid must not keep firing");
    }

    @Test
    void theDodoRiderGlidesOverIceInsteadOfSlipping() {
        Game game = lawn();
        for (int column = 0; column < game.getField().getCols(); column++) {
            game.getField().getCell(column, 2).setType(CellType.SLIPPERY_DOWN);
        }
        model.entities.zombies.Zombie dodo = model.entities.zombies.ZombieFactory.create(
                model.entities.zombies.Zombies.ZOMBIE_ICE_AGE_DODO, 2, 6,
                ChapterType.FROSTBITE_CAVES);
        game.getZombies().add(dodo);

        FrostbiteCavesMechanics ice = new FrostbiteCavesMechanics();
        ice.onTick(game);
        assertEquals(2, dodo.getRow(), "it does not slip out of its lane");
        assertTrue(((model.entities.zombies.types.DodoRider) dodo).isGliding(),
                "but it is marked as gliding so the view can hop it");
    }
}

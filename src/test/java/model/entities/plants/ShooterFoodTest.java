package model.entities.plants;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.Projectile;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShooterFoodTest {

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

    private void clearTombs(Game game) {
        game.getTombstones().clear();
        for (int c = 0; c < game.getField().getCols(); c++) {
            for (int r = 0; r < game.getField().getRows(); r++) {
                Cell cell = game.getField().getCell(c, r);
                if (cell != null && cell.getType() == CellType.TOMBSTONE) {
                    cell.setType(CellType.NORMAL);
                }
            }
        }
    }

    private int runCountingShots(Game game, double seconds) {
        GameLoop loop = new GameLoop(game);
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        int seen = 0;
        for (int i = 0; i < ticks; i++) {
            int before = game.getProjectiles().size();
            loop.step(game);
            clearTombs(game);
            seen += Math.max(0, game.getProjectiles().size() - before);
        }
        return seen;
    }

    @Test
    void everyShooterFiresABarrageOnPlantFood() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.SHOOTER
                    || type == Plants.APPEASE_MINT
                    || type == Plants.FIRE_PEASHOOTER) {
                continue;
            }
            Game game = lawn();
            clearTombs(game);
            Plant shooter = plant(game, type, 1, 2);
            shooter.onPlantFood(game);
            int wanted = type == Plants.BOWLING_BULB ? 1 : 3;
            int fired = runCountingShots(game, Plant.FED_SHOW * 3d);
            assertTrue(fired >= wanted,
                    type.getName() + " only fired " + fired + " times on plant food");
        }
    }

    @Test
    void aBarrageFiresWithNoZombiesOnTheLawn() {
        Game game = lawn();
        clearTombs(game);
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        assertTrue(game.getZombies().isEmpty(), "this test wants an empty lawn");
        peashooter.onPlantFood(game);
        assertTrue(runCountingShots(game, 6d) >= 8,
                "a fed Peashooter should empty its magazine regardless of targets");
    }

    @Test
    void theFedStateLastsAboutAsLongAsTheBarrage() {
        Game game = lawn();
        clearTombs(game);
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        peashooter.onPlantFood(game);
        assertTrue(peashooter.isFed(), "feeding should raise the fed state");
        runCountingShots(game, Plant.FED_SHOW * 3d);
        assertFalse(peashooter.isFed(), "the fed state must clear on its own");
    }

    @Test
    void aFedFirePeashooterBurnsItsLaneInsteadOfFiringPeas() {
        Game game = lawn();
        clearTombs(game);
        Plant fire = plant(game, Plants.FIRE_PEASHOOTER, 1, 2);
        Zombie caught = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 7,
                ChapterType.ANCIENT_EGYPT);
        caught.setHp(9999d);
        game.getZombies().add(caught);
        double before = caught.getHp();
        fire.onPlantFood(game);
        int fired = runCountingShots(game, Plant.FED_SHOW);
        assertTrue(caught.getHp() < before,
                "the fire should reach every zombie in the lane, not fly to the first one");
        assertTrue(fired == 0, "a fed Fire Peashooter throws fire, not peas, saw " + fired);
    }

    @Test
    void citronsFedOrbHitsFarHarderThanItsNormalOne() {
        Game plain = lawn();
        clearTombs(plain);
        plant(plain, Plants.CITRON, 1, 2);
        Zombie ordinary = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.ANCIENT_EGYPT);
        plain.getZombies().add(ordinary);
        double plainStart = ordinary.getHp();
        runCountingShots(plain, 20d);
        double normal = plainStart - ordinary.getHp();

        Game fed = lawn();
        clearTombs(fed);
        Plant citron = plant(fed, Plants.CITRON, 1, 2);
        Zombie bombarded = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.ANCIENT_EGYPT);
        bombarded.setHp(9999d);
        fed.getZombies().add(bombarded);
        double before = bombarded.getHp();
        citron.onPlantFood(fed);
        runCountingShots(fed, 20d);
        double boosted = before - bombarded.getHp();

        assertTrue(boosted > normal,
                "the fed orb should out-damage the plain one, saw " + boosted
                        + " against " + normal);
    }

    @Test
    void snowPeasFoodFreezesItsLane() {
        Game game = lawn();
        clearTombs(game);
        Plant snow = plant(game, Plants.SNOW_PEA, 1, 2);
        Zombie target = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.ANCIENT_EGYPT);
        game.getZombies().add(target);
        snow.onPlantFood(game);
        assertTrue(target.isFrozenSolid(),
                "Snow Pea's plant food should stop its lane cold");
    }

    @Test
    void aBarrageOnlyRunsOnce() {
        Game game = lawn();
        clearTombs(game);
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        peashooter.onPlantFood(game);
        runCountingShots(game, 10d);
        assertTrue(runCountingShots(game, 4d) == 0,
                "the barrage must not repeat itself with no zombies about");
        for (Projectile shot : game.getProjectiles()) {
            assertTrue(shot.getRow() == 2, "every pea should stay in its lane");
        }
    }
}

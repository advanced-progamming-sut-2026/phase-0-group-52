package model.entities;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.GameLoop;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;
import model.entities.plants.PlantsCategory;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileTest {

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

    private Zombie zombie(Game game, int row, int column) {
        Zombie made = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, row, column,
                ChapterType.ANCIENT_EGYPT);
        game.getZombies().add(made);
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

    private void run(Game game, double seconds) {
        GameLoop loop = new GameLoop(game);
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        for (int i = 0; i < ticks; i++) {
            loop.step(game);
            clearTombs(game);
        }
    }

    private java.util.List<Projectile> watch(Game game, double seconds) {
        java.util.List<Projectile> seen = new ArrayList<Projectile>();
        GameLoop loop = new GameLoop(game);
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        for (int i = 0; i < ticks; i++) {
            loop.step(game);
            clearTombs(game);
            for (Projectile shot : game.getProjectiles()) {
                if (!seen.contains(shot)) {
                    seen.add(shot);
                }
            }
        }
        return seen;
    }

    @Test
    void everyShooterFiresSomething() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.SHOOTER
                    || type == Plants.APPEASE_MINT) {
                continue;
            }
            Game game = lawn();
            plant(game, type, 1, 2);
            GameLoop loop = new GameLoop(game);
            boolean fired = false;
            for (int tick = 0; tick < 30 * Game.TICKS_PER_SECOND && !fired; tick++) {
                if (game.getZombies().isEmpty()) {
                    for (int row = 0; row < game.getField().getRows(); row++) {
                        zombie(game, row, 4);
                    }
                }
                game.setGameOver(false);
                loop.step(game);
                clearTombs(game);
                fired = !game.getProjectiles().isEmpty();
            }
            assertTrue(fired, type.getName() + " never fired anything");
        }
    }

    @Test
    void aPeaTravelsAndDamagesTheZombieItReaches() {
        Game game = lawn();
        plant(game, Plants.PEASHOOTER, 0, 2);
        Zombie target = zombie(game, 2, 6);
        double before = target.getHp();
        run(game, 12d);
        assertTrue(target.getHp() < before,
                "the pea should have reached the zombie and hurt it");
    }

    @Test
    void aPeaIgnoresZombiesInOtherRows() {
        Game game = lawn();
        plant(game, Plants.PEASHOOTER, 0, 2);
        Zombie other = zombie(game, 4, 6);
        double before = other.getHp();
        run(game, 6d);
        assertTrue(other.getHp() >= before - 0.001,
                "a pea must not hit a zombie in a different row");
    }

    @Test
    void snowPeaSlowsWhatItHits() {
        Game game = lawn();
        plant(game, Plants.SNOW_PEA, 0, 2);
        Zombie target = zombie(game, 2, 5);
        double fullSpeed = target.getSpeed();
        run(game, 12d);
        assertTrue(target.getSpeed() < fullSpeed || target.getHp() <= 0d,
                "a frost pea should slow the zombie it strikes");
    }

    @Test
    void threepeaterCoversThreeRows() {
        Game game = lawn();
        plant(game, Plants.THREEPEATER, 0, 2);
        zombie(game, 2, 7);
        zombie(game, 1, 7);
        zombie(game, 3, 7);
        boolean above = false;
        boolean below = false;
        for (Projectile shot : watch(game, 8d)) {
            above |= shot.getRow() == 1;
            below |= shot.getRow() == 3;
        }
        assertTrue(above && below, "Threepeater should fire into the rows either side");
    }

    @Test
    void splitPeaAlsoFiresBackwards() {
        Game game = lawn();
        plant(game, Plants.SPLIT_PEA, 4, 2);
        zombie(game, 2, 7);
        zombie(game, 2, 1);
        boolean back = false;
        for (Projectile shot : watch(game, 8d)) {
            back |= shot.getDirection() < 0;
        }
        assertTrue(back, "Split Pea should send a pea behind it");
    }

    @Test
    void aCitrusOrbPassesThroughSeveralZombies() {
        Game game = lawn();
        plant(game, Plants.CITRON, 0, 2);
        Zombie front = zombie(game, 2, 5);
        Zombie behind = zombie(game, 2, 7);
        front.setHp(9999d);
        behind.setHp(9999d);
        double frontBefore = front.getHp();
        double behindBefore = behind.getHp();
        for (Plant grown : game.getPlants()) {
            grown.onPlantFood(game);
        }
        run(game, 25d);
        assertTrue(front.getHp() < frontBefore && behind.getHp() < behindBefore,
                "a fed citrus orb should carry on through the zombie it hits");
    }

    @Test
    void aSporeRunsOutOfRange() {
        Game game = lawn();
        plant(game, Plants.PUFF_SHROOM, 0, 2);
        Zombie far = zombie(game, 2, 8);
        double before = far.getHp();
        run(game, 8d);
        assertTrue(far.getHp() >= before - 0.001,
                "Puff-shroom's spore must not reach the far end of the lawn");
    }

    @Test
    void spentProjectilesAreCleanedUp() {
        Game game = lawn();
        plant(game, Plants.PEASHOOTER, 0, 2);
        run(game, 40d);
        assertFalse(game.getProjectiles().size() > 30,
                "projectiles must be cleared once they leave the lawn, saw "
                        + game.getProjectiles().size());
    }
}

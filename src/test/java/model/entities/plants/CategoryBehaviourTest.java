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

class CategoryBehaviourTest {

    private Game lawn() {
        GameField field = new GameField(ChapterType.ANCIENT_EGYPT);
        Game game = new Game(null, null, null, field, 0, new ArrayList<Plant>(),
                new ArrayList<model.Wave>());
        clearTombs(game);
        return game;
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
        made.setHp(9999d);
        game.getZombies().add(made);
        return made;
    }

    private void run(Game game, double seconds) {
        GameLoop loop = new GameLoop(game);
        int ticks = (int) Math.round(seconds * Game.TICKS_PER_SECOND);
        for (int i = 0; i < ticks; i++) {
            loop.step(game);
            clearTombs(game);
        }
    }

    @Test
    void everyLobberThrowsSomethingThatFliesOverGraves() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.LOBBER
                    || PlantData.record(type) == null
                    || PlantData.record(type).getUnlockKind() == PlantRecord.UnlockKind.MINT) {
                continue;
            }
            Game game = lawn();
            plant(game, type, 1, 2);
            zombie(game, 2, 7);
            GameLoop loop = new GameLoop(game);
            boolean lobbed = false;
            for (int i = 0; i < 30 * Game.TICKS_PER_SECOND && !lobbed; i++) {
                loop.step(game);
                clearTombs(game);
                for (Projectile shot : game.getProjectiles()) {
                    lobbed |= shot.isLobbed();
                }
            }
            assertTrue(lobbed, type.getName() + " never lobbed anything");
        }
    }

    @Test
    void everyPlantThatActsAlsoAnimatesIt() {
        Plants[] hitters = {Plants.BONK_CHOY, Plants.PHAT_BEET, Plants.CABBAGE_PULT,
            Plants.SNAPDRAGON, Plants.PEASHOOTER};
        for (Plants type : hitters) {
            Game game = lawn();
            Plant made = plant(game, type, 1, 2);
            zombie(game, 2, 2);
            zombie(game, 2, 6);
            boolean acted = false;
            GameLoop loop = new GameLoop(game);
            for (int i = 0; i < 30 * Game.TICKS_PER_SECOND && !acted; i++) {
                loop.step(game);
                clearTombs(game);
                acted = made.isActing();
            }
            assertTrue(acted, type.getName() + " damages without ever playing its attack");
        }
    }

    @Test
    void aShooterIdlesWhileItIsRecharging() {
        Game game = lawn();
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        zombie(game, 2, 7);
        run(game, 12d);
        int acting = 0;
        int idle = 0;
        GameLoop loop = new GameLoop(game);
        for (int i = 0; i < 60; i++) {
            loop.step(game);
            clearTombs(game);
            if (peashooter.isActing()) {
                acting++;
            } else {
                idle++;
            }
        }
        assertTrue(idle > 0, "a recharging shooter should spend time idle, not always attacking");
        assertTrue(acting > 0, "it should still play its attack when it fires");
    }

    @Test
    void anImitaterBecomesThePlantItCopies() {
        Game game = lawn();
        model.entities.plants.types.Imitater fake =
                (model.entities.plants.types.Imitater)
                        PlantFactory.create(Plants.IMITATER, new Vec2(1, 2));
        fake.setCopiedType(Plants.SUNFLOWER);
        game.getField().getCell(1, 2).getPlants().add(fake);
        game.getPlants().add(fake);
        fake.onPlanted(game);
        assertFalse(game.getPlants().contains(fake),
                "the Imitater itself should not stay on the lawn");
        assertTrue(game.getPlants().size() == 1
                        && game.getPlants().get(0).getType() == Plants.SUNFLOWER,
                "it should have become the plant it copied");
    }

    @Test
    void aMineIsNotArmedTheMomentItIsPlanted() {
        Game game = lawn();
        model.entities.plants.types.PotatoMine mine =
                (model.entities.plants.types.PotatoMine) plant(game, Plants.POTATO_MINE, 1, 2);
        assertFalse(mine.isArmed(), "a fresh mine is still burying itself");
        zombie(game, 2, 1);
        run(game, 2d);
        assertTrue(game.getPlants().contains(mine),
                "an unarmed mine must not detonate under a zombie");
        run(game, 20d);
        assertTrue(mine.isArmed(), "it should arm after its fuse");
    }

    @Test
    void anEndlessGameKeepsTickingAfterTheLastZombieDies() {
        Game game = lawn();
        game.setEndless(true);
        Plant peashooter = plant(game, Plants.PEASHOOTER, 1, 2);
        Zombie doomed = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.ANCIENT_EGYPT);
        doomed.setHp(1d);
        game.getZombies().add(doomed);
        run(game, 20d);
        assertTrue(game.getZombies().isEmpty(), "the zombie should be gone");
        assertFalse(game.isGameOver(), "an endless game must not declare victory");

        Zombie next = zombie(game, 2, 6);
        double full = next.getHp();
        run(game, 12d);
        assertTrue(next.getHp() < full,
                "the lawn must keep working once something has died");
        assertTrue(peashooter.getHp() > 0d, "and the plant should still be there");
    }

    @Test
    void anOrdinaryLevelStillEndsWhenTheLastZombieDies() {
        Game game = lawn();
        plant(game, Plants.PEASHOOTER, 1, 2);
        Zombie doomed = ZombieFactory.create(Zombies.ZOMBIE_DEFAULT, 2, 6,
                ChapterType.ANCIENT_EGYPT);
        doomed.setHp(1d);
        game.getZombies().add(doomed);
        game.getWaves().add(new model.Wave(new ArrayList<Zombie>(), 1, 0));
        run(game, 20d);
        assertTrue(game.isGameOver(),
                "a real level ends as soon as the last wave is cleared");
    }

    @Test
    void everyLobberRunsTheSameCycleAndMuzzleMachineryAsAShooter() {
        for (Plants type : Plants.values()) {
            if (type.getCategory() != PlantsCategory.LOBBER
                    || PlantData.record(type) == null
                    || PlantData.record(type).getUnlockKind() == PlantRecord.UnlockKind.MINT) {
                continue;
            }
            Plant made = PlantFactory.create(type, new Vec2(0, 0));
            assertTrue(made instanceof Shooter,
                    type.getName() + " must share the shooter cycle, not its own loop");
            assertTrue(!((Shooter) made).ports().isEmpty(),
                    type.getName() + " needs at least one tunable muzzle");
        }
    }

    @Test
    void aLobbedShotDestroysTheGraveInItsWay() {
        Game game = lawn();
        game.getField().getCell(4, 2).setType(CellType.TOMBSTONE);
        model.entities.Tombstone stone = new model.entities.Tombstone(4, 2);
        game.getTombstones().add(stone);
        double before = stone.getHp();

        Projectile cabbage = new Projectile(Projectile.Kind.LOB, Plants.CABBAGE_PULT, 2,
                1.5d, 100d, 1);
        cabbage.from(2);
        for (int i = 0; i < 40 && !cabbage.isSpent(); i++) {
            cabbage.advance(game);
        }
        assertTrue(stone.getHp() < before,
                "a lobbed cabbage should smash the grave in its path");
    }

    @Test
    void aFedCabbagePultRainsOnEveryZombieButTheGiants() {
        Game game = lawn();
        Plant pult = plant(game, Plants.CABBAGE_PULT, 1, 2);
        Zombie ordinary = zombie(game, 0, 6);
        Zombie another = zombie(game, 4, 8);
        Zombie giant = ZombieFactory.create(Zombies.ZOMBIE_GARGANTUAR, 2, 7,
                ChapterType.ANCIENT_EGYPT);
        game.getZombies().add(giant);
        double giantBefore = giant.getHp();
        pult.onPlantFood(game);
        run(game, 3d);
        assertTrue(ordinary.isDead() || ordinary.getHp() < 9999d,
                "the rain should reach a zombie in another lane");
        assertTrue(another.isDead() || another.getHp() < 9999d,
                "and the far lane too");
        assertTrue(giant.getHp() >= giantBefore,
                "a Gargantuar is too big to be rained on");
    }

    @Test
    void instantUsePlantsDoNotPretendToHavePlantFood() {
        Plants[] instant = {Plants.CHERRY_BOMB, Plants.JALAPENO, Plants.DOOM_SHROOM,
            Plants.ICE_SHROOM, Plants.GRAVE_BUSTER};
        for (Plants type : instant) {
            PlantRecord record = PlantData.record(type);
            assertFalse(record != null && record.isBoostable(),
                    type.getName() + " is instant use and must not be boostable");
        }
    }
}

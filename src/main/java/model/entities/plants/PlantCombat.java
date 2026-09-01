package model.entities.plants;

import model.Game;
import model.GameField;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PlantCombat {

    public static final Random RANDOM = new Random();

    private PlantCombat() {}

    public static Zombie frontmostAhead(Game game, int row, double fromX) {
        Zombie best = null;
        for (Zombie z : game.getZombies()) {
            if (z.getRow() != row || z.isDead()) continue;
            if (z.getPosition().x >= fromX
                    && (best == null || z.getPosition().x < best.getPosition().x))
                best = z;
        }
        return best;
    }

    public static Zombie nearestBehind(Game game, int row, double fromX) {
        Zombie best = null;
        for (Zombie z : game.getZombies()) {
            if (z.getRow() != row || z.isDead()) continue;
            if (z.getPosition().x < fromX
                    && (best == null || z.getPosition().x > best.getPosition().x))
                best = z;
        }
        return best;
    }

    public static List<Zombie> zombiesInRow(Game game, int row) {
        List<Zombie> result = new ArrayList<Zombie>();
        for (Zombie z : game.getZombies())
            if (z.getRow() == row && !z.isDead()) result.add(z);
        return result;
    }

    public static List<Zombie> zombiesInArea(Game game, int col, int row, int radius) {
        List<Zombie> result = new ArrayList<Zombie>();
        for (Zombie z : game.getZombies()) {
            if (z.isDead()) continue;
            if (Math.abs(z.getRow() - row) <= radius && Math.abs(z.getCol() - col) <= radius)
                result.add(z);
        }
        return result;
    }

    public static List<Zombie> zombiesOnCell(Game game, int col, int row) {
        return zombiesInArea(game, col, row, 0);
    }

    public static void slow(Zombie z) {
        if (!z.isSlowed()) {
            z.setSlowed(true);
            z.setSpeed(z.getSpeed() * 0.5);
        }
    }

    public static final int FREEZE_TICKS = 10 * model.Game.TICKS_PER_SECOND;

    public static void freeze(Zombie z) {
        if (z.getChapter() == model.ChapterType.FROSTBITE_CAVES) {
            slow(z);
            return;
        }
        boolean wasFrozen = z.isFrozenSolid();
        z.freezeFor(FREEZE_TICKS);
        if (!wasFrozen)
            System.out.println("A zombie in row " + (z.getRow() + 1) + " was frozen solid for "
                    + (FREEZE_TICKS / model.Game.TICKS_PER_SECOND) + " seconds.");
    }

    public static void explode(Game game, int col, int row, int radius, double dmg) {
        for (Zombie z : zombiesInArea(game, col, row, radius))
            z.takeDamage(dmg);
        removeDeadZombies(game);
    }

    public static void meltFrozenInRow(Game game, int row, int fromCol) {
        GameField field = game.getField();
        if (field == null) return;
        for (int c = Math.max(fromCol, 0); c < field.getCols(); c++) {
            Cell cell = field.getCell(c, row);
            if (cell != null && cell.getType() == CellType.FROZEN)
                cell.setType(CellType.NORMAL);
        }
    }

    public static void removeDeadZombies(Game game) {
        for (int i = game.getZombies().size() - 1; i >= 0; i--) {
            Zombie z = game.getZombies().get(i);
            if (z.isDead()) {
                game.getStats().recordKill(game.getCurrentTick(), z.getAlias());
                if (z.getCol() <= 0 && !hasActiveMower(game, z.getRow()))
                    game.getStats().recordKillAtColZeroNoMower();
                if (game.getLevel() instanceof model.level.TimedWar)
                    ((model.level.TimedWar) game.getLevel()).onZombieKilled();
                rollLoot(game, z);
                z.onDeath(game);
                game.getZombies().remove(i);
            }
        }
    }

    private static void rollLoot(Game game, model.entities.zombies.Zombie z) {
        if (game.getApp() == null) {
            return;
        }
        model.User u = game.getApp().getCurrentuser();
        if (u == null || RANDOM.nextDouble() >= dropChance(z)) {
            return;
        }
        double roll = RANDOM.nextDouble();
        if (roll < SPROUT_SHARE && game.getApp().getGreenhouse() != null
                && game.getApp().getGreenhouse().unlockNextPot()) {
            game.noteDrop("A sprout for the greenhouse!");
        } else if (roll < SPROUT_SHARE + GEM_SHARE) {
            u.setGems(u.getGems() + 1);
            game.noteDrop("A diamond!");
        } else {
            u.setCoins(u.getCoins() + COIN_DROP);
            game.noteDrop("+" + COIN_DROP + " coins");
        }
    }

    private static boolean hasActiveMower(Game game, int row) {
        if (game.getField() == null) return false;
        for (model.entities.Lawnmower lm : game.getField().getLawnmowers())
            if (lm.getLine() == row && lm.isIsactive()) return true;
        return false;
    }

    private static final double SPROUT_SHARE = 0.45d;
    private static final double GEM_SHARE = 0.15d;
    private static final int COIN_DROP = 50;

    private static double dropChance(model.entities.zombies.Zombie z) {
        model.entities.zombies.ZombieRecord record = z == null ? null
                : model.entities.zombies.ZombieData.of(z.getOrigin());
        double weight = record == null ? 100d : record.getWaveCost();
        return Math.min(0.45d, 0.08d + weight / 6000d);
    }

    public static void removePlant(Game game, Plant plant) {
        game.getStats().recordPlantLost();
        if (game.getLevel() instanceof model.level.LoveYourPlants)
            ((model.level.LoveYourPlants) game.getLevel()).onPlantLost();
        System.out.println("Plant " + plant.getType().getName() + " at ("
                + (plant.getCol() + 1) + ", " + (plant.getRow() + 1) + ") is destroyed.");
        game.getPlants().remove(plant);
        if (game.getField() != null) {
            Cell cell = game.getField().getCell(plant.getCol(), plant.getRow());
            if (cell != null) cell.getPlants().remove(plant);
        }
    }

    public static double torchwoodFactor(Game game, int row, int fromCol) {
        for (Plant p : game.getPlants()) {
            if (p.getRow() == row && p.getCol() >= fromCol && p.getType() == Plants.TORCHWOOD) {
                if (p instanceof model.entities.plants.types.Torchwood
                        && ((model.entities.plants.types.Torchwood) p).isBlueFlame())
                    return 3;
                return 2;
            }
        }
        return 1;
    }
}

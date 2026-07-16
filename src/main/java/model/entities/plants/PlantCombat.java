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

    public static void freeze(Zombie z) {
        if (z.getChapter() == model.ChapterType.FROSTBITE_CAVES) {
            slow(z);
            return;
        }
        z.setSpeed(0);
        z.setState(ZombieState.DISABLED);
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
                z.onDeath(game);
                game.getZombies().remove(i);
            }
        }
    }

    public static void removePlant(Game game, Plant plant) {
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

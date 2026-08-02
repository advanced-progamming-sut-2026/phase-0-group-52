package view;

import model.Game;
import model.GameField;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.Lawnmower;
import model.entities.plants.Plant;
import model.entities.plants.Plants;
import model.entities.zombies.ArmorType;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieState;

import java.util.List;
import java.util.Scanner;

public class GameMenu implements AppMenu {

    private controller.menu.GameMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new controller.menu.GameMenuController(model.App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line.split("\\s+"));
    }

    public void showPlanted(String name, int x, int y) {
        System.out.println(name + " planted at (" + x + ", " + y + ").");
    }

    public void showPlucked(String name, int x, int y) {
        System.out.println(name + " plucked from (" + x + ", " + y + ").");
    }

    public void showFed(String name, int x, int y, int remaining) {
        System.out.println("Plant food used on " + name + " at (" + x + ", " + y + "). "
                + remaining + " plant food(s) left.");
    }

    public void showCooldownsRemoved() {
        System.out.println("All cooldowns removed.");
    }

    public void showPlantFoodAdded(int count) {
        System.out.println("One plant food added. You have " + count + " plant food(s) now.");
    }

    public void showPlantFoodDrop(int count) {
        System.out.println("The glowing zombie dropeed a plant food; you have " + count + " plant foods now.");
    }

    public void showZombieDrop(String singular, int count, String plural) {
        System.out.println("A zombie dropeed a " + singular + "; you have " + count + " " + plural + " now.");
    }

    public void showMap(Game game) {
        GameField field = game.getField();
        int waveNo = game.getCurrentWaveIndex() + 1;
        System.out.println("Wave: " + waveNo + "/" + game.getWaves().size()
                + " | Sun: " + game.getSunAmount()
                + " | Plant food: " + game.getPlantFoodCount() + "/" + Game.MAX_PLANT_FOOD);
        if (game.getLevel() instanceof model.level.TimedWar) {
            model.level.TimedWar tw = (model.level.TimedWar) game.getLevel();
            System.out.println("[TIMED WAR]  Time left: "
                    + String.format("%.1f", tw.getRemainingTime() / (double) Game.TICKS_PER_SECOND)
                    + "s  |  Kills: " + tw.getKills());}
        int deadCol = -1;
        if (game.getLevel() instanceof model.level.DeadLine) {
            deadCol = ((model.level.DeadLine) game.getLevel()).getDeadlineCol();
            System.out.println("[DEAD LINE]  Do not let a zombie cross the '|' at column " + (deadCol + 1) + "!");}
        for (int r = 0; r < field.getRows(); r++) {
            StringBuilder sb = new StringBuilder();
            Lawnmower mower = field.getLawnmower(r);
            sb.append(mower != null && mower.isIsactive() ? "[M]" : "[ ]");
            for (int c = 0; c < field.getCols(); c++) {
                if (c == deadCol) sb.append(" |");
                Cell cell = field.getCell(c, r);
                char tile = tileChar(cell);
                char plant = '-';
                if (!cell.getPlants().isEmpty()) {
                    Plant top = cell.getPlants().get(cell.getPlants().size() - 1);
                    plant = Character.toUpperCase(top.getType().getName().charAt(0));}
                int zombieCount = 0;
                for (Zombie z : game.getZombies())
                    if (z.getRow() == r && z.getCol() == c) zombieCount++;
                char zc = zombieCount == 0 ? '-'
                        : (zombieCount == 1 ? 'Z' : (char) ('0' + Math.min(zombieCount, 9)));
                sb.append(' ').append(tile).append(plant).append(zc);}
            System.out.println(sb.toString());
        }
        System.out.println("Cell format: <tile><plant><zombies> " +
                " (Z = a zombie, digit = that many zombies) | [M] = lawnmower available");
        System.out.println("Tiles: . normal  T tombstone  $ sun-grave  & food-grave " +
                " ~ water  # frozen  ^ slip-up  v slip-down  _ low-ground  N necromancy");
        if (!game.getZombies().isEmpty()) {
            System.out.println("Zombies:");
            for (Zombie z : game.getZombies()) {
                System.out.println("  " + z.getDisplayName() + " | row " + (z.getRow() + 1)
                        + " | x=" + String.format("%.2f", z.getPosition().x)
                        + " | hp=" + (int) z.getHp()
                        + (z.getArmorHp() > 0 ? " | armor=" + (int) z.getArmorHp() : ""));
            }
        }
    }

    private char tileChar(Cell cell) {
        CellType type = cell.getType();

        if (type == CellType.TOMBSTONE && cell.getGraveBonus() != null)
            return cell.getGraveBonus().equals("sun") ? '$' : '&';
        switch (type) {
            case TOMBSTONE:     return 'T';
            case WATER:         return '~';
            case FROZEN:        return '#';
            case SLIPPERY_UP:   return '^';
            case SLIPPERY_DOWN: return 'v';
            case LOW_GROUND:    return '_';
            case NECROMANCY:    return 'N';
            default:            return '.';
        }
    }

    public void showPlantsStatus(List<Plants> types, Game game) {
        System.out.println("Plants status:");
        for (Plants type : types) {
            String state;
            if (!game.isCooldownsRemoved() && game.isOnCooldown(type))
                state = "recharging, ready in " + String.format("%.1f", game.getRemainingCooldown(type)) + "s";
            else if (game.getSunAmount() < type.getCost())
                state = "not enough sun";
            else
                state = "ready";
            System.out.println("  " + type.getName() + " | cost: " + type.getCost() + " sun | " + state);
        }
    }

    public void showTileStatus(Cell cell, List<Zombie> zombies, int x, int y) {
        System.out.println("Tile (" + x + ", " + y + "):");
        System.out.println("  Type: " + cell.getType()
                + (cell.getHp() > 0 ? " (hp: " + (int) cell.getHp() + ")" : ""));
        if (cell.getPlants().isEmpty()) {
            System.out.println("  Plants: none");
        } else {
            System.out.println("  Plants:");
            for (Plant p : cell.getPlants()) {
                Plants t = p.getType();
                System.out.println("    " + t.getName()
                        + " | category: " + t.getCategory()
                        + " | hp: " + (int) p.getHp()
                        + " | damage: " + t.getDamage()
                        + (p.isBoosted() ? " | boosted" : ""));
            }
        }
        if (zombies.isEmpty()) {
            System.out.println("  Zombies: none");
        } else {
            System.out.println("  Zombies:");
            for (Zombie z : zombies) {
                System.out.println("    " + z.getDisplayName()
                        + " | hp: " + (int) z.getHp()
                        + " | armor: " + z.getArmorType()+(z.getArmorHp() > 0 ? " (" + (int) z.getArmorHp() + ")" : "")
                        + " | x=" + String.format("%.2f", z.getPosition().x)
                        + " | state: " + z.getState());
            }
        }
    }

    public void showZombiesInfo(List<Zombie> zombies) {
        if (zombies.isEmpty()) {
            System.out.println("No zombies on the field.");
            return;
        }
        for (Zombie z : zombies) {
            System.out.println(z.getDisplayName() + ":");
            System.out.println("position: " + (z.getCol() + 1) + ", " + (z.getRow() + 1));
            System.out.println("health: " + (int) z.getHp());
            System.out.println("armor:");
            if (z.getArmorHp() > 0)
                System.out.println(armorName(z.getArmorType()) + ": " + (int) z.getArmorHp());
            System.out.println("effects:");
            if (z.isSlowed()) System.out.println("chilled");
            if (z.getState() == ZombieState.DISABLED) System.out.println("frozen");
        }
    }

    private String armorName(ArmorType type) {
        switch (type) {
            case CONEHEAD:   return "cone";
            case BUCKETHEAD: return "bucket";
            case BRICKHEAD:  return "block";
            case KNIGHT:     return "crown";
            case ICEBLOCK:   return "ice";
            default:         return "armor";
        }
    }

    public void showZombieSpawned(String name, int x, int y) {
        System.out.println(name + " spawned at (" + x + ", " + y + ").");
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}

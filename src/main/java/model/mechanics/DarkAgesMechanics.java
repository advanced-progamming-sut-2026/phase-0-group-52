package model.mechanics;

import model.ChapterType;
import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.PlantCombat;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

public class DarkAgesMechanics implements ChapterMechanics {

    private static final int RISEN_LEVEL = 2;
    private static final int MAX_GRAVES = 5;
    private static final int BACK_ROWS = 3;
    private static final int BACK_ODDS = 8;
    private static final int SUN_ODDS = 20;
    private static final int FOOD_ODDS = 10;

    private static final int GRAVE_SUN_BONUS = 50;

    public boolean isSkySunDisabled() { return true; }

    @Override
    public void onWaveStart(Game game) {
        spawnRandomGraves(game);
        necromancySpawns(game);
    }

    @Override
    public void onTick(Game game) {
        clearBrokenGraves(game);
        grantDestroyedGraveBonuses(game);
    }

    private void spawnRandomGraves(Game game) {
        GameField field = game.getField();
        if (field == null) {
            return;
        }
        int count = 1 + PlantCombat.RANDOM.nextInt(2);
        int tries = 0;
        while (count > 0 && tries < 100) {
            tries++;
            if (game.getTombstones().size() >= MAX_GRAVES) {
                return;
            }
            int c = graveColumn(field);
            int r = PlantCombat.RANDOM.nextInt(field.getRows());
            Cell cell = field.getCell(c, r);
            if (cell == null || cell.getType() != CellType.NORMAL || !cell.isEmpty()) continue;
            cell.setType(CellType.TOMBSTONE);
            model.entities.Tombstone stone = new model.entities.Tombstone(c, r);
            game.getTombstones().add(stone);
            String bonus = rollBonus();
            cell.setGraveBonus(bonus);
            stone.setBonus(bonus);
            boolean necromancy = PlantCombat.RANDOM.nextInt(100) < 45;
            cell.setNecromancy(necromancy);
            System.out.println("A grave rose at (" + (c + 1) + ", " + (r + 1) + ")"
                    + (bonus != null ? " containing " + bonus : "")
                    + (necromancy ? " [necromancy]" : "") + "!");
            count--;
        }
    }

    private int graveColumn(GameField field) {
        int cols = field.getCols();
        int safe = Math.max(1, cols - BACK_ROWS);
        if (PlantCombat.RANDOM.nextInt(100) < BACK_ODDS) {
            return safe + PlantCombat.RANDOM.nextInt(Math.max(1, cols - safe - 1));
        }
        return PlantCombat.RANDOM.nextInt(safe);
    }

    private String rollBonus() {
        int roll = PlantCombat.RANDOM.nextInt(100);
        if (roll < SUN_ODDS) {
            return "sun";
        }
        return roll < SUN_ODDS + FOOD_ODDS ? "plant food" : null;
    }

    private void necromancySpawns(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getCols(); c++) {
                Cell cell = field.getCell(c, r);
                if (cell != null && cell.isNecromancy() && cell.getType() == CellType.TOMBSTONE) {
                    game.getZombies().add(ZombieFactory.create(risen(game),
                            r, new Vec2(c, r), ChapterType.DARK_AGES, null));
                    game.getRisings().add(new Vec2(c, r));
                    util.Log.info("game", "Necromancy! A zombie claws out of the grave at ("
                            + (c + 1) + ", " + (r + 1) + ")!");
                    cell.setNecromancy(false);

                }
            }
        }
    }

    private void clearBrokenGraves(Game game) {
        java.util.Iterator<model.entities.Tombstone> gone =
                game.getTombstones().iterator();
        while (gone.hasNext()) {
            model.entities.Tombstone stone = gone.next();
            if (!stone.isDestroyed()) {
                continue;
            }
            Cell cell = game.getField() == null ? null
                    : game.getField().getCell(stone.getColumn(), stone.getRow());
            if (cell != null) {
                cell.setType(CellType.NORMAL);
            }
            gone.remove();
        }
    }

    private Zombies risen(Game game) {
        java.util.List<Zombies> pool =
                model.level.WavePlan.roster(ChapterType.DARK_AGES, RISEN_LEVEL);
        if (pool.isEmpty()) {
            return Zombies.ZOMBIE_DEFAULT;
        }
        return pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
    }

    private void grantDestroyedGraveBonuses(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getCols(); c++) {
                Cell cell = field.getCell(c, r);
                if (cell == null || cell.getGraveBonus() == null
                        || cell.getType() == CellType.TOMBSTONE) continue;
                if (cell.getGraveBonus().equals("sun")) {
                    game.addSun(GRAVE_SUN_BONUS);
                    System.out.println("The grave dropped " + GRAVE_SUN_BONUS + " sun!");
                } else {
                    game.setPlantFoodCount(game.getPlantFoodCount() + 1);
                    System.out.println("The grave dropped a plant food; you have "
                            + game.getPlantFoodCount() + " plant foods now.");
                }
                cell.setGraveBonus(null);
            }
        }
    }
}

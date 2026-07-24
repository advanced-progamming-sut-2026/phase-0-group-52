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

    private static final int GRAVE_SUN_BONUS = 50;

    public boolean isSkySunDisabled() {
        return true;
    }

    @Override
    public void onWaveStart(Game game) {
        spawnRandomGraves(game);
        necromancySpawns(game);
    }

    @Override
    public void onTick(Game game) {
        grantDestroyedGraveBonuses(game);
    }

    private void spawnRandomGraves(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        int count = 1 + PlantCombat.RANDOM.nextInt(2);
        int tries = 0;
        while (count > 0 && tries < 100) {
            tries++;
            int c = PlantCombat.RANDOM.nextInt(field.getCols());
            int r = PlantCombat.RANDOM.nextInt(field.getRows());
            Cell cell = field.getCell(c, r);
            if (cell == null || cell.getType() != CellType.NORMAL || !cell.isEmpty()) continue;
            cell.setType(CellType.TOMBSTONE);
            String bonus = null;
            if (PlantCombat.RANDOM.nextInt(100) < 40)
                bonus = PlantCombat.RANDOM.nextBoolean() ? "sun" : "plant food";
            cell.setGraveBonus(bonus);
            System.out.println("A grave rose at (" + (c + 1) + ", " + (r + 1) + ")"
                    + (bonus != null ? " containing " + bonus : "") + "!");
            count--;
        }
    }

    private void necromancySpawns(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getCols(); c++) {
                Cell cell = field.getCell(c, r);
                if (cell != null && cell.isNecromancy() && cell.getType() == CellType.TOMBSTONE) {
                    game.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DEFAULT,
                            r, new Vec2(c, r), ChapterType.DARK_AGES, null));
                    System.out.println("A zombie rose from the grave at (" + (c + 1) + ", " + (r + 1) + ")!");
                }
            }
        }
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

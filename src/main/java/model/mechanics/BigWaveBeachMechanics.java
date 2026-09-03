package model.mechanics;

import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.plants.PlantTag;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;

public class BigWaveBeachMechanics implements ChapterMechanics {

    private static final int TIDE_INTERVAL = 50;
    private static final int MAX_TIDE = 3;
    private static final int MIN_TIDE = 0;
    private static final int SURFACE_LEVEL = 2;
    private static final int LOW_GROUND_INTERVAL = 70;
    private static final int LOW_GROUND_COUNT = 2;

    private boolean started = false;
    private int tideLevel = MIN_TIDE;
    private int tideDir = 1;
    private int tideTimer = 0;
    private int lowGroundTimer = 0;
    private final List<int[]> lowGround = new ArrayList<int[]>();

    @Override
    public void onWaveStart(Game game) {

        emergeFromLowGround(game);
    }

    @Override
    public void onTick(Game game) {
        if (!started) {
            started = true;
            initTerrain(game);
        }
        if (++tideTimer >= TIDE_INTERVAL) {
            tideTimer = 0;
            stepTide(game);
        }
        if (++lowGroundTimer >= LOW_GROUND_INTERVAL) {
            lowGroundTimer = 0;
            emergeFromLowGround(game);
        }
    }

    private void initTerrain(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        int cols = field.getCols();
        int rows = field.getRows();
        int placed = 0, tries = 0;
        while (placed < LOW_GROUND_COUNT && tries < 50) {
            tries++;
            int c = 1 + PlantCombat.RANDOM.nextInt(Math.max(1, cols - MAX_TIDE - 1));
            int r = PlantCombat.RANDOM.nextInt(rows);
            Cell cell = field.getCell(c, r);
            if (cell == null || cell.getType() != CellType.NORMAL) continue;
            cell.setType(CellType.LOW_GROUND);
            lowGround.add(new int[]{c, r});
            placed++;
        }
        applyTide(game);
    }

    private void stepTide(Game game) {
        tideLevel += tideDir;
        if (tideLevel >= MAX_TIDE) {
            tideLevel = MAX_TIDE;
            tideDir = -1;
            util.Log.info("game", "A big wave rolls in! The rightmost " + tideLevel
                    + " columns are underwater.");
        } else if (tideLevel <= MIN_TIDE) {
            tideLevel = MIN_TIDE;
            tideDir = 1;
            util.Log.info("game", "Low tide - the shoreline is clear.");
        } else {
            util.Log.info("game", (tideDir > 0 ? "The tide rises" : "The tide falls")
                    + " - " + tideLevel + " column(s) underwater.");
        }
        applyTide(game);
    }

    private void applyTide(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        int cols = field.getCols();
        int rows = field.getRows();
        for (int col = cols - MAX_TIDE; col < cols; col++) {
            if (col < 0) continue;
            boolean water = col >= cols - tideLevel;
            for (int r = 0; r < rows; r++) {
                Cell cell = field.getCell(col, r);
                if (cell == null) continue;
                if (water) {
                    if (cell.getType() != CellType.WATER) {
                        washAwayLandPlants(game, cell, col, r);
                        cell.setType(CellType.WATER);
                    }
                } else if (cell.getType() == CellType.WATER) {
                    cell.setType(CellType.NORMAL);
                }
            }
        }
    }

    private void washAwayLandPlants(Game game, Cell cell, int col, int row) {
        if (cell.getPlants().isEmpty() || cell.hasLilyPad()) {
            return;
        }
        for (Plant p : cell.getPlants()) {
            if (p.getType().getTags().contains(PlantTag.WATER)) {
                return;
            }
        }
        for (Plant p : new ArrayList<Plant>(cell.getPlants())) game.getPlants().remove(p);
        cell.getPlants().clear();
        util.Log.info("game", "The tide washed away the plant at (" + (col + 1) + ", " + (row + 1) + ")!");
    }

    private void emergeFromLowGround(Game game) {
        GameField field = game.getField();
        if (field == null || lowGround.isEmpty()) return;
        if (PlantCombat.RANDOM.nextInt(100) >= 40) return;
        int[] tile = lowGround.get(PlantCombat.RANDOM.nextInt(lowGround.size()));
        int col = tile[0], row = tile[1];
        java.util.List<Zombies> pool = model.level.WavePlan.roster(
                model.ChapterType.BIG_WAVE_BEACH, SURFACE_LEVEL);
        Zombies kind = pool.isEmpty() ? Zombies.ZOMBIE_DEFAULT
                : pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
        Zombie z = ZombieFactory.create(kind, row, new Vec2(col, row),
                field.getChapter(), null);
        game.getZombies().add(z);
        util.Log.info("game", "A zombie surfaced from the low beach at (" + (col + 1) + ", " + (row + 1) + ")!");
    }
}

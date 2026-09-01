package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.PlantCombat;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class TombRaiser extends WalkingZombie {

    private static final double RAISE_INTERVAL = 6d;
    private static final int MAX_TOMBS = 4;

    private double raiseTimer;
    private int raised;

    public TombRaiser(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_TOMB_RAISER, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized() && raised < MAX_TOMBS && game.getField() != null) {
            raiseTimer += Game.SECONDS_PER_TICK;
            if (raiseTimer >= RAISE_INTERVAL) {
                raiseTimer = 0d;
                raiseTomb(game);
            }
        }
        super.onTick(game);
    }

    private void raiseTomb(Game game) {
        int columns = game.getField().getCols();
        int rows = game.getField().getRows();
        for (int attempt = 0; attempt < columns; attempt++) {
            int col = PlantCombat.RANDOM.nextInt(columns);
            int row = PlantCombat.RANDOM.nextInt(rows);
            Cell cell = game.getField().getCell(col, row);
            if (cell == null || cell.getType() != CellType.NORMAL) {
                continue;
            }
            if (!cell.getPlants().isEmpty()) {
                continue;
            }
            cell.setType(CellType.TOMBSTONE);
            raised++;
            System.out.println("A tombstone rises at (" + (col + 1) + ", " + (row + 1) + ")!");
            return;
        }
    }
}

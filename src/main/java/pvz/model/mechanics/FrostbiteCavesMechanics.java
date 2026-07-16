package pvz.model.mechanics;

import pvz.model.Game;
import pvz.model.GameField;
import pvz.model.entities.Cell;
import pvz.model.entities.CellType;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.PlantTag;
import pvz.model.entities.zombies.Zombie;
import pvz.model.entities.zombies.ZombieState;
import pvz.model.entities.zombies.types.DodoRider;

import java.util.HashMap;
import java.util.Map;

public class FrostbiteCavesMechanics implements ChapterMechanics {

    private static final double MELT_RATE = 60;

    private final Map<Zombie, Double> frozenZombies = new HashMap<Zombie, Double>();

    public void freezeZombieAtStart(Game game, Zombie zombie) {
        GameField field = game.getField();
        if (field != null) {
            Cell cell = field.getCell(zombie.getCol(), zombie.getRow());
            if (cell != null) cell.setType(CellType.FROZEN);
        }
        frozenZombies.put(zombie, zombie.getSpeed());
        zombie.setSpeed(0);
        zombie.setState(ZombieState.DISABLED);
    }

    @Override
    public void onWaveStart(Game game) {

        if (game.getField() == null || !PlantCombat.RANDOM.nextBoolean()) return;
        int rows = game.getField().getRows();
        int count = 1 + PlantCombat.RANDOM.nextInt(2);
        for (int i = 0; i < count; i++) {
            int row = PlantCombat.RANDOM.nextInt(rows);
            System.out.println("An icy wind hits row " + (row + 1) + "!");
            for (Plant p : game.getPlants()) {
                if (p.getRow() == row && !p.getType().getTags().contains(PlantTag.FIRE))
                    p.addFreezeLevel();
            }
        }
    }

    @Override
    public void onTick(Game game) {
        meltNearFire(game);
        releaseThawedZombies(game);
        applySlipperyTiles(game);
    }

    private void meltNearFire(Game game) {
        for (Plant p : game.getPlants()) {
            if (p.isFrozen() && p.getIceHp() > 0 && fireNear(game, p.getCol(), p.getRow()))
                p.damageIce(MELT_RATE);
        }
        GameField field = game.getField();
        if (field == null) return;
        for (int r = 0; r < field.getRows(); r++) {
            for (int c = 0; c < field.getCols(); c++) {
                Cell cell = field.getCell(c, r);
                if (cell != null && cell.getType() == CellType.FROZEN && fireNear(game, c, r)) {
                    cell.setHp(cell.getHp() - MELT_RATE);
                    if (cell.getHp() <= 0) cell.setType(CellType.NORMAL);
                }
            }
        }
    }

    private boolean fireNear(Game game, int col, int row) {
        for (Plant p : game.getPlants()) {
            if (p.getType().getTags().contains(PlantTag.FIRE)
                    && Math.abs(p.getCol() - col) <= 1 && Math.abs(p.getRow() - row) <= 1
                    && !p.isFrozen())
                return true;
        }
        return false;
    }

    private void releaseThawedZombies(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (Map.Entry<Zombie, Double> entry : new HashMap<Zombie, Double>(frozenZombies).entrySet()) {
            Zombie z = entry.getKey();
            Cell cell = field.getCell(z.getCol(), z.getRow());
            if (cell == null || cell.getType() != CellType.FROZEN) {
                z.setSpeed(entry.getValue());
                z.setState(ZombieState.WALKING);
                frozenZombies.remove(z);
            }
        }
    }

    private void applySlipperyTiles(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (Zombie z : game.getZombies()) {
            if (z instanceof DodoRider || z.getState() == ZombieState.DISABLED) continue;
            Cell cell = field.getCell(z.getCol(), z.getRow());
            if (cell == null) continue;
            if (cell.getType() == CellType.SLIPPERY_UP && z.getRow() > 0)
                z.setLine(z.getRow() - 1);
            else if (cell.getType() == CellType.SLIPPERY_DOWN && z.getRow() < field.getRows() - 1)
                z.setLine(z.getRow() + 1);
        }
    }
}

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
import model.entities.zombies.ZombieState;
import model.entities.zombies.Zombies;
import model.entities.zombies.types.DodoRider;

import java.util.HashMap;
import java.util.Map;

public class FrostbiteCavesMechanics implements ChapterMechanics {

    private static final double MELT_RATE = 60;
    private static final int START_FREEZE_TICKS = 8 * Game.TICKS_PER_SECOND;

    private final Map<Zombie, Double> frozenZombies = new HashMap<Zombie, Double>();
    private final Map<Zombie, Integer> startThaw = new HashMap<Zombie, Integer>();
    private final Map<Zombie, Integer> slipped = new HashMap<Zombie, Integer>();
    private final java.util.List<Integer> chilled = new java.util.ArrayList<Integer>();

    public java.util.List<Integer> takeChilledRows() {
        java.util.List<Integer> rows = new java.util.ArrayList<Integer>(chilled);
        chilled.clear();
        return rows;
    }
    private boolean started = false;

    private void thawEncased(Game game) {
        for (Zombie z : new java.util.ArrayList<Zombie>(game.getZombies())) {
            if (!z.isEncased()) {
                continue;
            }
            z.thawIce();
            if (!z.isEncased()) {
                Double speed = frozenZombies.remove(z);
                z.setSpeed(speed == null ? z.getOrigin().getSpeed() : speed.doubleValue());
                z.setState(ZombieState.WALKING);
            }
        }
    }

    public void freezeZombieAtStart(Game game, Zombie zombie) {
        frozenZombies.put(zombie, zombie.getSpeed());
        zombie.encaseInIce();
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
            chilled.add(Integer.valueOf(row));
            System.out.println("An icy wind hits row " + (row + 1) + "!");
            for (Plant p : game.getPlants()) {
                if (p.getRow() == row && !p.getType().getTags().contains(PlantTag.FIRE))
                    p.addFreezeLevel();
            }
        }
    }

    @Override
    public void onTick(Game game) {
        thawEncased(game);
        if (!started) {
            started = true;
            spawnFrozenAtStart(game);
        }
        meltNearFire(game);
        tickStartThaw(game);
        releaseThawedZombies(game);
        applySlipperyTiles(game);
    }

    private Zombies fromRoster(Game game) {
        java.util.List<Zombies> pool = new java.util.ArrayList<Zombies>();
        for (model.Wave wave : game.getWaves()) {
            for (Zombie z : wave.getZombies()) {
                if (!pool.contains(z.getOrigin())) {
                    pool.add(z.getOrigin());
                }
            }
        }
        return pool.isEmpty() ? Zombies.ZOMBIE_DEFAULT
                : pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
    }

    private void spawnFrozenAtStart(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        int rows = field.getRows();
        int startCol = field.getCols() - 1;
        int count = 1 + PlantCombat.RANDOM.nextInt(2);
        for (int i = 0; i < count; i++) {
            int row = PlantCombat.RANDOM.nextInt(rows);
            Zombie z = ZombieFactory.create(fromRoster(game), row, GameField.COLS,
                    field.getChapter());
            z.setPosition(new Vec2(startCol, row));
            game.getZombies().add(z);
            freezeZombieAtStart(game, z);
            startThaw.put(z, START_FREEZE_TICKS);
            System.out.println("A zombie is frozen solid in row " + (row + 1) + " as the level begins!");
        }
    }

    private void tickStartThaw(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        for (Map.Entry<Zombie, Integer> entry : new HashMap<Zombie, Integer>(startThaw).entrySet()) {
            Zombie z = entry.getKey();
            int left = entry.getValue() - 1;
            if (left <= 0 || !frozenZombies.containsKey(z)) {
                Cell cell = field.getCell(z.getCol(), z.getRow());
                if (cell != null && cell.getType() == CellType.FROZEN) cell.setType(CellType.NORMAL);
                startThaw.remove(z);
            } else {
                startThaw.put(z, left);
            }
        }
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
        if (field == null) {
            return;
        }
        for (Zombie z : game.getZombies()) {
            if (z.getState() == ZombieState.DISABLED) {
                continue;
            }
            Cell cell = field.getCell(z.getCol(), z.getRow());
            if (cell == null) {
                continue;
            }
            if (z instanceof DodoRider) {
                ((DodoRider) z).setGliding(cell.getType() == CellType.SLIPPERY_UP
                        || cell.getType() == CellType.SLIPPERY_DOWN);
                continue;
            }
            boolean icy = cell.getType() == CellType.SLIPPERY_UP
                    || cell.getType() == CellType.SLIPPERY_DOWN;
            Integer slidAt = slipped.get(z);
            if (!icy) {
                slipped.remove(z);
                continue;
            }
            if (slidAt != null && slidAt.intValue() == z.getCol()) {
                continue;
            }
            slipped.put(z, Integer.valueOf(z.getCol()));
            if (cell.getType() == CellType.SLIPPERY_UP && z.getRow() > 0) {
                z.setLine(z.getRow() - 1);
            } else if (cell.getType() == CellType.SLIPPERY_DOWN
                    && z.getRow() < field.getRows() - 1) {
                z.setLine(z.getRow() + 1);
            }
        }
    }
}

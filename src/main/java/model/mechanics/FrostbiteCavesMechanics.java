package model.mechanics;

import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.ZombieState;
import model.entities.zombies.Zombies;
import model.entities.zombies.types.DodoRider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrostbiteCavesMechanics implements ChapterMechanics {

    public static final int ICE_PATCHES_MIN = 2;
    public static final int ICE_PATCHES_MAX = 4;
    public static final int FROZEN_AT_START = 2;
    public static final int GUST_ODDS = 45;
    public static final double FIRE_REACH = 1d;

    private final Map<Zombie, Double> heldSpeeds = new HashMap<Zombie, Double>();
    private final Map<Zombie, Integer> slidOn = new HashMap<Zombie, Integer>();

    private boolean opened;

    @Override
    public void onWaveStart(Game game) {
        chillWind(game);
    }

    @Override
    public void onTick(Game game) {
        if (!opened) {
            opened = true;
            layIcePatches(game);
            encaseOpeningZombies(game);
        }
        fireThaws(game);
        meltHeldIce(game);
        releaseFreedZombies(game);
        slideOnIce(game);
    }

    private void layIcePatches(Game game) {
        GameField field = game.getField();
        if (field == null) {
            return;
        }
        int patches = ICE_PATCHES_MIN
                + PlantCombat.RANDOM.nextInt(ICE_PATCHES_MAX - ICE_PATCHES_MIN + 1);
        for (int i = 0; i < patches; i++) {
            int row = PlantCombat.RANDOM.nextInt(field.getRows());
            int column = 2 + PlantCombat.RANDOM.nextInt(Math.max(1, field.getCols() - 3));
            Cell cell = field.getCell(column, row);
            if (cell == null || cell.getType() != CellType.NORMAL) {
                continue;
            }
            boolean canRise = row > 0;
            boolean canFall = row < field.getRows() - 1;
            if (!canRise && !canFall) {
                continue;
            }
            boolean rising = canRise && (!canFall || PlantCombat.RANDOM.nextBoolean());
            cell.setType(rising ? CellType.SLIPPERY_UP : CellType.SLIPPERY_DOWN);
        }
    }

    private void encaseOpeningZombies(Game game) {
        GameField field = game.getField();
        if (field == null) {
            return;
        }
        List<Zombies> roster = model.level.WavePlan.roster(
                model.ChapterType.FROSTBITE_CAVES, 1);
        for (int i = 0; i < FROZEN_AT_START; i++) {
            int row = PlantCombat.RANDOM.nextInt(field.getRows());
            int column = field.getCols() - 2 - PlantCombat.RANDOM.nextInt(2);
            Zombies kind = roster.isEmpty() ? Zombies.ZOMBIE_DEFAULT
                    : roster.get(PlantCombat.RANDOM.nextInt(roster.size()));
            Zombie sleeper = ZombieFactory.create(kind, row, new Vec2(column, row),
                    model.ChapterType.FROSTBITE_CAVES, null);
            game.getZombies().add(sleeper);
            encase(sleeper);
        }
    }

    public void freezeZombieAtStart(Game game, Zombie zombie) {
        encase(zombie);
    }

    private void encase(Zombie zombie) {
        heldSpeeds.put(zombie, Double.valueOf(zombie.getSpeed()));
        zombie.encaseInIce();
        zombie.setSpeed(0);
        zombie.setState(ZombieState.DISABLED);
    }

    private void chillWind(Game game) {
        GameField field = game.getField();
        if (field == null || PlantCombat.RANDOM.nextInt(100) >= GUST_ODDS) {
            return;
        }
        int row = PlantCombat.RANDOM.nextInt(field.getRows());
        util.Log.info("game", "An icy wind sweeps row " + (row + 1) + "!");
        game.getGusts().add(Integer.valueOf(row));
        for (Plant plant : game.getPlants()) {
            if (plant.getRow() == row) {
                plant.addFreezeLevel();
            }
        }
    }

    private void fireThaws(Game game) {
        for (Plant plant : new ArrayList<Plant>(game.getPlants())) {
            if (plant.getFreezeLevel() > 0 && fireBeside(game, plant.getCol(), plant.getRow())) {
                plant.thawCompletely();
            }
        }
        for (Zombie zombie : new ArrayList<Zombie>(game.getZombies())) {
            if (zombie.isEncased() && fireBeside(game, zombie.getCol(), zombie.getRow())) {
                zombie.damageIce(Zombie.ICE_HP);
            }
        }
    }

    private boolean fireBeside(Game game, int column, int row) {
        for (Plant plant : game.getPlants()) {
            if (!plant.resistsCold() || plant.isFrozen()) {
                continue;
            }
            if (Math.abs(plant.getCol() - column) <= FIRE_REACH
                    && Math.abs(plant.getRow() - row) <= FIRE_REACH) {
                return true;
            }
        }
        return false;
    }

    private void meltHeldIce(Game game) {
        for (Zombie zombie : new ArrayList<Zombie>(game.getZombies())) {
            if (zombie.isEncased()) {
                zombie.thawIce();
            }
        }
    }

    private void releaseFreedZombies(Game game) {
        for (Map.Entry<Zombie, Double> entry
                : new HashMap<Zombie, Double>(heldSpeeds).entrySet()) {
            Zombie zombie = entry.getKey();
            if (zombie.isEncased()) {
                continue;
            }
            zombie.setSpeed(entry.getValue().doubleValue());
            zombie.setState(ZombieState.WALKING);
            heldSpeeds.remove(zombie);
        }
    }

    private void slideOnIce(Game game) {
        GameField field = game.getField();
        if (field == null) {
            return;
        }
        for (Zombie zombie : game.getZombies()) {
            if (zombie instanceof DodoRider) {
                glideOver(field, zombie);
                continue;
            }
            if (zombie.isEncased() || zombie.getState() == ZombieState.DISABLED) {
                continue;
            }
            Cell cell = field.getCell(zombie.getCol(), zombie.getRow());
            if (cell == null || !slippery(cell)) {
                slidOn.remove(zombie);
                continue;
            }
            Integer last = slidOn.get(zombie);
            int here = zombie.getCol() * field.getRows() + zombie.getRow();
            if (last != null && last.intValue() == here) {
                continue;
            }
            slidOn.put(zombie, Integer.valueOf(here));
            slide(field, zombie, cell.getType());
        }
    }

    private void glideOver(GameField field, Zombie zombie) {
        Cell cell = field.getCell(zombie.getCol(), zombie.getRow());
        ((DodoRider) zombie).setGliding(cell != null && slippery(cell));
    }

    private void slide(GameField field, Zombie zombie, CellType type) {
        if (type == CellType.SLIPPERY_UP && zombie.getRow() > 0) {
            zombie.setLine(zombie.getRow() - 1);
        } else if (type == CellType.SLIPPERY_DOWN
                && zombie.getRow() < field.getRows() - 1) {
            zombie.setLine(zombie.getRow() + 1);
        }
    }

    private static boolean slippery(Cell cell) {
        return cell.getType() == CellType.SLIPPERY_UP
                || cell.getType() == CellType.SLIPPERY_DOWN;
    }
}

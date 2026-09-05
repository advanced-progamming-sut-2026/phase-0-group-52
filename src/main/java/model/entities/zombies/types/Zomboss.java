package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieFactory;
import model.entities.zombies.Zombies;

import java.util.ArrayList;
import java.util.List;

public class Zomboss extends Zombie {

    public static final int SEGMENTS = 3;
    public static final double SEGMENT_HP = 2200d;
    public static final int MOVE_INTERVAL = 45;
    public static final int STUN_TICKS = 60;
    public static final int BURN_TICKS = 40;
    public static final int FIREBALLS = 3;
    public static final int GRAVES_PER_MISSILE = 2;
    public static final int SUMMON_COUNT = 3;
    public static final double CHARGE_REACH = 12d;
    public static final double PULL_STEP = 0.55d;

    private final ChapterType world;

    private int timer;
    private int stunned;
    private int segmentsLost;
    private double lastHp;
    private final List<Vec2> strikes = new ArrayList<Vec2>();

    public Zomboss(ChapterType world, int row, double column) {
        super(SEGMENT_HP * SEGMENTS, 0d, 0d, row, new Vec2(column, row),
                model.entities.zombies.ArmorType.DEFAULT, world, null,
                model.entities.zombies.ZombieState.WALKING, null);
        this.world = world;
        this.lastHp = getHp();
    }

    public ChapterType getWorld() {
        return world;
    }

    @Override
    public boolean occupiesRow(int row) {
        return row == getRow() || row == getRow() + 1;
    }

    @Override
    public int rowSpan() {
        return 2;
    }

    public double maxHp() {
        return SEGMENT_HP * SEGMENTS;
    }

    public int segmentsLeft() {
        return (int) Math.ceil(Math.max(0d, getHp()) / SEGMENT_HP);
    }

    public float healthFraction() {
        return (float) Math.max(0d, Math.min(1d, getHp() / maxHp()));
    }

    public boolean isStunned() {
        return stunned > 0;
    }

    public List<Vec2> takeStrikes() {
        List<Vec2> out = new ArrayList<Vec2>(strikes);
        strikes.clear();
        return out;
    }

    private boolean summons() {
        return world != ChapterType.FROSTBITE_CAVES;
    }

    private boolean roams() {
        return world != ChapterType.FROSTBITE_CAVES;
    }

    @Override
    public void onTick(Game game) {
        countSegment();
        if (stunned > 0) {
            stunned--;
            return;
        }
        timer++;
        if (timer < MOVE_INTERVAL) {
            return;
        }
        timer = 0;
        act(game);
    }

    private void countSegment() {
        int lostNow = (int) ((maxHp() - Math.max(0d, getHp())) / SEGMENT_HP);
        if (lostNow > segmentsLost && getHp() > 0d) {
            segmentsLost = lostNow;
            stunned = STUN_TICKS;
        }
        lastHp = getHp();
    }

    private void act(Game game) {
        int roll = PlantCombat.RANDOM.nextInt(100);
        if (roll < 30) {
            signature(game);
            return;
        }
        if (roll < 60) {
            sweep(game);
            return;
        }
        if (roll < 80 && summons()) {
            summon(game);
            return;
        }
        if (roams()) {
            roam(game);
            return;
        }
        signature(game);
    }

    private void signature(Game game) {
        switch (world) {
            case DARK_AGES:       fireballs(game); break;
            case FROSTBITE_CAVES: iceMissile(game); break;
            case BIG_WAVE_BEACH:  babySharks(game); break;
            default:              missile(game); break;
        }
    }

    private void sweep(Game game) {
        switch (world) {
            case DARK_AGES:       burnRows(game); break;
            case FROSTBITE_CAVES: chill(game); break;
            case BIG_WAVE_BEACH:  turbine(game); break;
            default:              charge(game); break;
        }
    }

    private void missile(Game game) {
        Vec2 target = randomCell(game);
        if (target == null) {
            return;
        }
        strikes.add(target);
        killPlantAt(game, (int) target.x, (int) target.y);
        for (int i = 0; i < GRAVES_PER_MISSILE; i++) {
            raiseGrave(game);
        }
    }

    private void iceMissile(Game game) {
        Vec2 target = randomCell(game);
        if (target == null) {
            return;
        }
        strikes.add(target);
        killPlantAt(game, (int) target.x, (int) target.y);
    }

    private void fireballs(Game game) {
        for (int i = 0; i < FIREBALLS; i++) {
            Vec2 target = randomCell(game);
            if (target == null) {
                continue;
            }
            strikes.add(target);
            int column = (int) target.x;
            int row = (int) target.y;
            killPlantAt(game, column, row);
            scorch(game, column, row);
            game.getZombies().add(ZombieFactory.create(Zombies.ZOMBIE_DARK_IMP_DRAGON,
                    row, column, world));
        }
    }

    private void burnRows(Game game) {
        if (game.getField() == null) {
            return;
        }
        for (int row = getRow(); row <= getRow() + 1; row++) {
            for (int column = 0; column < game.getField().getCols(); column++) {
                killPlantAt(game, column, row);
                scorch(game, column, row);
            }
        }
    }

    private void charge(Game game) {
        for (int row = getRow(); row <= getRow() + 1; row++) {
            for (Plant plant : new ArrayList<Plant>(game.getPlants())) {
                if (plant.getRow() == row) {
                    PlantCombat.removePlant(game, plant);
                }
            }
        }
    }

    private void chill(Game game) {
        if (game.getField() == null) {
            return;
        }
        int rows = game.getField().getRows();
        for (int i = 0; i < 2; i++) {
            int row = PlantCombat.RANDOM.nextInt(rows);
            if (!game.getGusts().contains(Integer.valueOf(row))) {
                game.getGusts().add(Integer.valueOf(row));
            }
            for (Plant plant : game.getPlants()) {
                if (plant.getRow() == row) {
                    plant.addFreezeLevel();
                }
            }
        }
    }

    private void turbine(Game game) {
        for (Plant plant : new ArrayList<Plant>(game.getPlants())) {
            if (!occupiesRow(plant.getRow())) {
                continue;
            }
            if (plant.getCol() >= getPosition().x - 1d) {
                PlantCombat.removePlant(game, plant);
            }
        }
        for (Zombie zombie : game.getZombies()) {
            if (zombie == this || !occupiesRow(zombie.getRow())) {
                continue;
            }
            zombie.setPosition(new Vec2(
                    Math.min(getPosition().x, zombie.getPosition().x + PULL_STEP),
                    zombie.getPosition().y));
        }
    }

    private void babySharks(Game game) {
        for (int i = 0; i < FIREBALLS; i++) {
            Plant prey = randomPlant(game, true);
            if (prey == null) {
                prey = randomPlant(game, false);
            }
            if (prey == null) {
                return;
            }
            strikes.add(new Vec2(prey.getCol(), prey.getRow()));
            PlantCombat.removePlant(game, prey);
        }
    }

    private void summon(Game game) {
        List<Zombies> pool = model.level.WavePlan.roster(world, 2);
        if (pool.isEmpty() || game.getField() == null) {
            return;
        }
        for (int i = 0; i < SUMMON_COUNT; i++) {
            Zombies type = pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
            int row = PlantCombat.RANDOM.nextInt(game.getField().getRows());
            game.getZombies().add(ZombieFactory.create(type, row,
                    game.getField().getCols() - 1, world));
        }
    }

    private void roam(Game game) {
        if (game.getField() == null) {
            return;
        }
        int top = game.getField().getRows() - rowSpan();
        int wanted = PlantCombat.RANDOM.nextInt(Math.max(1, top + 1));
        setLine(wanted);
        setPosition(new Vec2(getPosition().x, wanted));
    }

    public void freezeColumn(Game game) {
        if (game.getField() == null) {
            return;
        }
        int column = PlantCombat.RANDOM.nextInt(game.getField().getCols());
        for (int row = 0; row < game.getField().getRows(); row++) {
            Zombie frozen = ZombieFactory.create(Zombies.ZOMBIE_ICE_AGE_TROGLOBITE,
                    row, column, world);
            frozen.encaseInIce();
            game.getZombies().add(frozen);
        }
    }

    private void scorch(Game game, int column, int row) {
        Cell cell = game.getField() == null ? null : game.getField().getCell(column, row);
        if (cell != null && cell.getType() != CellType.WATER) {
            cell.burn(game.getCurrentTick() + BURN_TICKS);
        }
    }

    private void raiseGrave(Game game) {
        if (game.getField() == null) {
            return;
        }
        for (int attempt = 0; attempt < 20; attempt++) {
            int column = 2 + PlantCombat.RANDOM.nextInt(
                    Math.max(1, game.getField().getCols() - 2));
            int row = PlantCombat.RANDOM.nextInt(game.getField().getRows());
            Cell cell = game.getField().getCell(column, row);
            if (cell == null || cell.getType() != CellType.NORMAL || !cell.isEmpty()) {
                continue;
            }
            cell.setType(CellType.TOMBSTONE);
            game.getTombstones().add(new model.entities.Tombstone(column, row));
            return;
        }
    }

    private void killPlantAt(Game game, int column, int row) {
        for (Plant plant : new ArrayList<Plant>(game.getPlants())) {
            if (plant.getCol() == column && plant.getRow() == row) {
                PlantCombat.removePlant(game, plant);
            }
        }
    }

    private Plant randomPlant(Game game, boolean onWater) {
        List<Plant> pool = new ArrayList<Plant>();
        for (Plant plant : game.getPlants()) {
            Cell cell = game.getField() == null ? null
                    : game.getField().getCell(plant.getCol(), plant.getRow());
            boolean wet = cell != null && cell.getType().isWater();
            if (wet == onWater) {
                pool.add(plant);
            }
        }
        return pool.isEmpty() ? null
                : pool.get(PlantCombat.RANDOM.nextInt(pool.size()));
    }

    private Vec2 randomCell(Game game) {
        if (game.getField() == null) {
            return null;
        }
        return new Vec2(PlantCombat.RANDOM.nextInt(game.getField().getCols()),
                PlantCombat.RANDOM.nextInt(game.getField().getRows()));
    }

    public double lastKnownHp() {
        return lastHp;
    }
}

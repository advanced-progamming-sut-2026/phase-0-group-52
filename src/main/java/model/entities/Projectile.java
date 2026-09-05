package model.entities;

import model.Game;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

public class Projectile {

    public enum Kind { PEA, FROST, FIRE, GOO, SPORE, ORB, STAR, LOB }

    public static Kind kindOf(Plants source) {
        if (source == null) {
            return Kind.PEA;
        }
        switch (source) {
            case SNOW_PEA:        return Kind.FROST;
            case FIRE_PEASHOOTER: return Kind.FIRE;
            case GOO_PEASHOOTER:  return Kind.GOO;
            case CITRON:
            case BOWLING_BULB:    return Kind.ORB;
            case STARFRUIT:
            case ROTOBAGA:        return Kind.STAR;
            case PUFF_SHROOM:
            case SEA_SHROOM:      return Kind.SPORE;
            case CABBAGE_PULT:
            case KERNEL_PULT:
            case MELON_PULT:
            case WINTER_MELON:
            case PEPPER_PULT:     return Kind.LOB;
            default:              return Kind.PEA;
        }
    }

    public static final double SPEED = 0.34;
    public static final double HIT_RANGE = 0.45;
    public static final double FRONT_EDGE = 0.3;
    public static final int BURN_TICKS = 20;
    public static final int POISON_TICKS = 40;
    public static final double BOUNCE_SLOPE = 0.16;
    public static final double BUTTER_CHANCE = 0.25;
    public static final double OFF_LAWN = 12.0;
    public static final double LANE_EASE = 0.3;
    public static final double LANE_SETTLED = 0.02;
    public static final String FED = "fed";
    public static final String BULB = "bulb";
    public static final double SPORE_RANGE = 3.5;
    public static final int ORB_PIERCE = 3;

    private final Kind kind;
    private final Plants source;
    private int row;
    private final double damage;
    private final int direction;
    private final String variant;
    private final String port;

    private final java.util.Set<Zombie> struck = new java.util.HashSet<Zombie>();

    private double column;
    private double travelled;
    private double lane;
    private double drift;
    private boolean bounces;
    private double pace = 1d;
    private boolean spent;

    public Projectile(Kind kind, Plants source, int row, double column,
            double damage, int direction) {
        this(kind, source, row, column, damage, direction,
                model.entities.plants.Muzzle.MAIN, "");
    }

    public Projectile(Kind kind, Plants source, int row, double column,
            double damage, int direction, String port, String variant) {
        this.port = port == null ? model.entities.plants.Muzzle.MAIN : port;
        this.variant = variant == null ? "" : variant;
        this.kind = kind;
        this.source = source;
        this.row = row;
        this.column = column;
        this.damage = damage;
        this.direction = direction >= 0 ? 1 : -1;
        this.lane = row;
    }

    public Kind getKind() {
        return kind;
    }

    public Plants getSource() {
        return source;
    }

    public String getVariant() {
        return variant;
    }

    public int getDirection() {
        return direction;
    }

    public String getPort() {
        return port;
    }

    public int getRow() {
        return row;
    }

    public double getColumn() {
        return column;
    }

    public boolean isLobbed() {
        return kind == Kind.LOB;
    }

    public double getTravelled() {
        return travelled;
    }

    public double laneProgress() {
        double gap = Math.abs(row - lane);
        return gap <= LANE_SETTLED ? 1d : Math.max(0d, 1d - gap);
    }

    public double getLane() {
        return lane;
    }

    public Projectile from(int row) {
        this.lane = row;
        return this;
    }

    public Projectile bouncing(boolean value) {
        this.bounces = value;
        if (value && drift == 0d) {
            this.drift = PlantCombat.RANDOM.nextBoolean() ? BOUNCE_SLOPE : -BOUNCE_SLOPE;
        }
        return this;
    }

    public Projectile hasten(double factor) {
        this.pace = factor;
        return this;
    }

    public double getPace() {
        return pace;
    }

    public Projectile drifting(double rowsPerTick) {
        this.drift = rowsPerTick;
        return this;
    }

    public boolean isDiagonal() {
        return drift != 0d;
    }

    public boolean isSpent() {
        return spent;
    }

    public void advance(Game game) {
        if (spent) {
            return;
        }
        column += SPEED * pace * direction;
        if (drift != 0d) {
            lane += drift;
            if (bounces && (lane < 0d || lane > model.GameField.ROWS - 1)) {
                drift = -drift;
                lane += 2d * drift;
            }
            row = (int) Math.round(Math.max(0d,
                    Math.min(model.GameField.ROWS - 1d, lane)));
        } else if (Math.abs(row - lane) > LANE_SETTLED) {
            lane += (row - lane) * LANE_EASE;
        } else {
            lane = row;
        }
        travelled += SPEED;
        if (column < -OFF_LAWN || column > OFF_LAWN || travelled > range()) {
            spent = true;
            return;
        }
        if (smashGrave(game)) {
            spent = true;
            return;
        }
        Zombie hit = firstHit(game);
        if (hit != null) {
            strike(game, hit);
            struck.add(hit);
            if (struck.size() >= pierce()) {
                spent = true;
            }
        }
    }

    private double range() {
        return kind == Kind.SPORE ? SPORE_RANGE : Double.MAX_VALUE;
    }

    private int pierce() {
        return kind == Kind.ORB && FED.equals(variant) ? ORB_PIERCE : 1;
    }

    private boolean smashGrave(Game game) {
        model.GameField field = game.getField();
        if (field == null || direction < 0) {
            return false;
        }
        int at = (int) Math.floor(column);
        if (at < 0 || at >= field.getCols()) {
            return false;
        }
        Cell cell = field.getCell(at, row);
        if (cell == null || cell.getType() != CellType.TOMBSTONE) {
            return false;
        }
        Tombstone stone = game.tombstoneAt(at, row);
        if (stone != null) {
            stone.takeDamage(damage);
        }
        return true;
    }

    private Zombie firstHit(Game game) {
        for (Zombie zombie : game.getZombies()) {
            if (!zombie.occupiesRow(row) || struck.contains(zombie)) {
                continue;
            }
            if (kind == Kind.LOB
                    && zombie instanceof model.entities.zombies.types.ParasolZombie) {
                continue;
            }
            if (zombie.isSubmerged(game)) {
                continue;
            }
            if (source == Plants.CABBAGE_PULT && FED.equals(variant)
                    && model.entities.plants.types.CabbagePult.tooBigToRain(zombie)) {
                continue;
            }
            double front = zombie.getPosition().x - FRONT_EDGE * direction;
            if (Math.abs(front - column) <= HIT_RANGE) {
                return zombie;
            }
        }
        return null;
    }

    private void splash(Game game, Zombie centre) {
        if (source == null || !source.getTags().contains(
                model.entities.plants.PlantTag.AOE)) {
            return;
        }
        for (Zombie other : game.getZombies()) {
            if (other == centre || other.isDead() || other.getRow() != row) {
                continue;
            }
            if (Math.abs(other.getPosition().x - centre.getPosition().x) <= 1d) {
                other.takeDamage(damage / 2d);
                if (source == Plants.WINTER_MELON) {
                    PlantCombat.slow(other);
                }
            }
        }
    }

    private void strike(Game game, Zombie zombie) {
        if (zombie.isEncased()) {
            zombie.damageIce(damage);
            PlantCombat.removeDeadZombies(game);
            return;
        }
        zombie.takeDamage(damage);
        if (kind == Kind.FROST || source == Plants.WINTER_MELON) {
            PlantCombat.slow(zombie);
        }
        if (kind == Kind.GOO) {
            PlantCombat.slow(zombie);
            zombie.poisonFor(POISON_TICKS);
        }
        if (kind == Kind.FIRE) {
            zombie.burnFor(BURN_TICKS);
        }
        if (source == Plants.KERNEL_PULT
                && PlantCombat.RANDOM.nextDouble() < BUTTER_CHANCE) {
            PlantCombat.slow(zombie);
        }
        splash(game, zombie);
        PlantCombat.removeDeadZombies(game);
    }
}

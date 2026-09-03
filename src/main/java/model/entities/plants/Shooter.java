package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class Shooter extends Plant {

    private static final int FOOD_VOLLEYS = 10;
    private static final double BURST_RUSH = 6d;
    private static final double FOOD_STRETCH = 1.5d;
    private static final double FED_PACE = 1.8d;
    private static final double DEFAULT_ATTACK = 0.6d;

    private int burstLeft;
    private final java.util.Set<String> firedPorts = new java.util.HashSet<String>();
    private final java.util.List<Muzzle> pending = new java.util.ArrayList<Muzzle>();
    private boolean cycling;
    private double cycleClock;

    public Shooter(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) {
            return;
        }
        if (burstLeft > 0 && !isFed()) {
            burstLeft = 0;
            cycling = false;
            pending.clear();
        }
        if (cycling) {
            advanceCycle(game);
            return;
        }
        if (burstLeft > 0) {
            markActedFor(attackSpan());
            beginCycle(game);
            advanceCycle(game);
            return;
        }
        actionTimer += model.Game.SECONDS_PER_TICK;
        if (actionTimer >= rechargeTime()) {
            actionTimer = 0d;
            shoot(game);
        }
    }

    protected double rechargeTime() {
        double interval = getActionInterval();
        return interval <= 0d ? 1d : interval;
    }

    private double cycleSpan() {
        double span = attackSpan();
        return isBursting() ? Math.max(model.Game.SECONDS_PER_TICK, span / BURST_RUSH) : span;
    }

    protected double attackSpan() {
        PlantRecord record = PlantData.record(getType());
        if (record == null || record.getAnimations() == null) {
            return DEFAULT_ATTACK;
        }
        java.util.Map<String, Double> clips = record.getAnimations().getClips();
        for (String name : new String[] {"attack", "special_stage1", "special"}) {
            Double seconds = clips.get(name);
            if (seconds != null && seconds.doubleValue() > 0d) {
                return seconds.doubleValue();
            }
        }
        return DEFAULT_ATTACK;
    }

    protected void shoot(Game game) {
        if (!hasTarget(game)) {
            return;
        }
        markActedFor(attackSpan());
        beginCycle(game);
        advanceCycle(game);
    }

    private void beginCycle(Game game) {
        cycling = true;
        cycleClock = 0d;
        firedPorts.clear();
        pending.clear();
        for (Muzzle muzzle : portsFor(shotVariant())) {
            if (aims(game, muzzle)) {
                pending.add(muzzle);
            }
        }
    }

    private void advanceCycle(Game game) {
        double span = cycleSpan();
        java.util.Iterator<Muzzle> waiting = pending.iterator();
        while (waiting.hasNext()) {
            Muzzle muzzle = waiting.next();
            if (muzzle.frameIn(getType(), shotVariant()) * span <= cycleClock) {
                fireFrom(game, muzzle);
                waiting.remove();
            }
        }
        cycleClock += model.Game.SECONDS_PER_TICK;
        if (cycleClock < span) {
            return;
        }
        for (Muzzle late : pending) {
            fireFrom(game, late);
        }
        cycling = false;
        actionTimer = 0d;
        pending.clear();
        if (burstLeft > 0) {
            burstLeft--;
        }
    }

    public java.util.List<Muzzle> portsFor(String state) {
        return ports();
    }

    protected boolean aims(Game game, Muzzle muzzle) {
        for (Muzzle allowed : portsFor(shotVariant())) {
            if (allowed.getName().equals(muzzle.getName())) {
                return true;
            }
        }
        return false;
    }

    public java.util.List<Muzzle> ports() {
        return java.util.Collections.singletonList(Muzzle.forward());
    }

    protected boolean hasTarget(Game game) {
        for (Muzzle muzzle : portsFor(shotVariant())) {
            int row = rowOf(game, muzzle);
            if (row < 0) {
                continue;
            }
            boolean seen = muzzle.getDirection() > 0
                    ? PlantCombat.frontmostAhead(game, row, getCol()) != null
                            || graveAhead(game, row)
                    : PlantCombat.nearestBehind(game, row, getCol()) != null;
            if (seen) {
                return true;
            }
        }
        return false;
    }

    private boolean graveAhead(Game game, int row) {
        model.GameField field = game.getField();
        if (field == null) {
            return false;
        }
        for (int c = getCol() + 1; c < field.getCols(); c++) {
            model.entities.Cell cell = field.getCell(c, row);
            if (cell != null && cell.getType() == model.entities.CellType.TOMBSTONE) {
                return true;
            }
        }
        return false;
    }

    protected void fire(Game game) {
        firedPorts.clear();
        for (Muzzle muzzle : portsFor(shotVariant())) {
            fireFrom(game, muzzle);
        }
    }

    public java.util.Set<String> firedPorts() {
        return firedPorts;
    }

    protected void fireFrom(Game game, Muzzle muzzle) {
        int row = rowOf(game, muzzle);
        if (row < 0) {
            return;
        }
        firedPorts.add(muzzle.getName());
        model.entities.Projectile shot =
                launch(game, row, muzzle.getDirection(), muzzle.getName());
        shot.from(getRow());
        if (isBursting()) {
            shot.hasten(foodPace());
        }
    }

    protected int rowOf(Game game, Muzzle muzzle) {
        int row = getRow() + muzzle.getRowOffset();
        if (game.getField() == null) {
            return row == getRow() ? row : -1;
        }
        return row >= 0 && row < game.getField().getRows() ? row : -1;
    }

    protected model.entities.Projectile launch(Game game, int row, int direction) {
        return launch(game, row, direction, Muzzle.MAIN);
    }

    protected model.entities.Projectile launch(Game game, int row, int direction, String port) {
        model.entities.Projectile shot = new model.entities.Projectile(bolt(), getType(), row,
                getCol() + 0.5 * direction, shotDamage(game), direction, port, shotVariant());
        shot.from(getRow());
        game.getProjectiles().add(shot);
        return shot;
    }

    @Override
    public void onPlantFood(Game game) {
        markFedFor(foodSeconds());
        cycling = false;
        pending.clear();
        firedPorts.clear();
        burstLeft = foodVolleys();
        onFoodBurst(game);
    }

    protected double foodSeconds() {
        return FED_SHOW * FOOD_STRETCH;
    }

    protected double foodPace() {
        return FED_PACE;
    }

    protected int foodVolleys() {
        return FOOD_VOLLEYS;
    }

    protected void onFoodBurst(Game game) {
    }

    protected double foodDamageFactor() {
        return 1d;
    }

    protected boolean isBursting() {
        return burstLeft > 0;
    }

    protected String shotVariant() {
        return isBursting() ? model.entities.Projectile.FED : "";
    }

    protected model.entities.Projectile.Kind bolt() {
        return model.entities.Projectile.kindOf(getType());
    }

    protected double shotDamage(Game game) {
        double dmg = getAttackdamage();
        if (getType().getTags().contains(PlantTag.PEA)) {
            dmg *= PlantCombat.torchwoodFactor(game, getRow(), getCol());
        }
        if (burstLeft > 0) {
            dmg *= foodDamageFactor();
        }
        return dmg;
    }
}

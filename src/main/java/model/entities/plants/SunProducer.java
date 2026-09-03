package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.Sun;

public class SunProducer extends Plant {

    private static final int GROWN_AFTER = 3;
    private static final int ORB = 25;
    private static final double SPREAD = 1.4d;
    private static final double DROP_MIN = 0.25d;
    private static final double DROP_SPAN = 0.3d;

    private static final double ORB_GAP = 0.12d;
    private static final int GOLD_ORBS = 5;
    private static final int GOLD_TOTAL = 375;
    private static final double GOLD_FALLBACK = 1.2d;

    private double burstTimer;
    private int pendingOrbs;
    private double orbTimer;
    private int productions = 0;

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        spillOrbs(game);
        if (getType() == Plants.GOLD_BLOOM) {
            goldBloom(game);
            return;
        }
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 24;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            productions++;
            markActed();
            for (int i = 0; i < sunCount(); i++) {
                if (!game.lawnIsLitteredWithSun()) {
                    game.getSuns().add(new Sun(sunAmount(), scatter()));
                }
            }
            System.out.println("plant " + getType().getName() + " produced a sun at ("
                    + (getCol() + 1) + ", " + (getRow() + 1) + ")");
        }
    }

    private void goldBloom(Game game) {
        markActed();
        burstTimer += model.Game.SECONDS_PER_TICK;
        if (burstTimer < burstDelay()) {
            return;
        }
        for (int i = 0; i < GOLD_ORBS; i++) {
            game.getSuns().add(new Sun(GOLD_TOTAL / GOLD_ORBS, scatter()));
        }
        System.out.println("plant Gold Bloom burst " + GOLD_TOTAL + " sun in "
                + GOLD_ORBS + " orbs at (" + (getCol() + 1) + ", " + (getRow() + 1)
                + ") and vanished.");
        setHp(0);
        PlantCombat.removePlant(game, this);
    }

    private double burstDelay() {
        PlantRecord record = PlantData.record(getType());
        if (record == null || record.getAnimations() == null) {
            return GOLD_FALLBACK;
        }
        double clip = record.getAnimations().clipDuration("attack");
        return clip > 0d ? clip : GOLD_FALLBACK;
    }

    private void spillOrbs(Game game) {
        if (pendingOrbs <= 0) {
            return;
        }
        orbTimer += model.Game.SECONDS_PER_TICK;
        while (pendingOrbs > 0 && orbTimer >= ORB_GAP) {
            orbTimer -= ORB_GAP;
            pendingOrbs--;
            markActed();
            game.getSuns().add(new Sun(ORB, scatter()));
        }
    }

    private Vec2 scatter() {
        double dx = (PlantCombat.RANDOM.nextDouble() - 0.5d) * SPREAD;
        double dy = DROP_MIN + PlantCombat.RANDOM.nextDouble() * DROP_SPAN;
        return new Vec2(getCol() + dx, getRow() + dy);
    }

    private int sunCount() {
        return getType() == Plants.TWIN_SUNFLOWER ? 2 : 1;
    }

    private int sunAmount() {
        switch (getType()) {
            case TWIN_SUNFLOWER:   return 50;
            case PRIMAL_SUNFLOWER: return 75;
            case SUNFLOWER:        return 50;
            case SUN_SHROOM:       return productions <= 1 ? 25 : (productions == 2 ? 50 : 75);
            default:               return 25;
        }
    }

    @Override
    public int growthStage() {
        return getType() == Plants.SUN_SHROOM && productions >= GROWN_AFTER ? 2 : 1;
    }

    @Override
    public void onPlantFood(Game game) {
        super.onPlantFood(game);
        int burst = plantFoodSun();
        pendingOrbs += burst / ORB;
        orbTimer = 0d;
        if (getType() == Plants.SUN_SHROOM) {
            productions = Math.max(productions, GROWN_AFTER);
        }
        System.out.println("plant " + getType().getName() + " burst " + burst
                + " sun from plant food.");
    }

    private int plantFoodSun() {
        switch (getType()) {
            case TWIN_SUNFLOWER:   return 250;
            case PRIMAL_SUNFLOWER: return 225;
            case SUN_SHROOM:       return 175;
            case GOLD_BLOOM:       return GOLD_TOTAL;
            default:               return 150;
        }
    }
}

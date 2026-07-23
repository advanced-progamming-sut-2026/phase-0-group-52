package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.Sun;

public class SunProducer extends Plant {

    private int productions = 0;

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        if (getType() == Plants.GOLD_BLOOM) {
            game.getSuns().add(new Sun(375, new Vec2(getCol(), getRow())));
            System.out.println("plant Gold Bloom burst 375 sun at ("
                    + (getCol() + 1) + ", " + (getRow() + 1) + ") and vanished.");
            setHp(0);
            PlantCombat.removePlant(game, this);
            return;
        }
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 24;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            productions++;
            game.getSuns().add(new Sun(sunAmount(), new Vec2(getCol(), getRow())));
            System.out.println("plant " + getType().getName() + " produced a sun at ("
                    + (getCol() + 1) + ", " + (getRow() + 1) + ")");
        }
    }

    private int sunAmount() {
        switch (getType()) {
            case TWIN_SUNFLOWER:   return 100;
            case PRIMAL_SUNFLOWER: return 75;
            case SUNFLOWER:        return 50;
            case SUN_SHROOM:       return productions <= 1 ? 25 : (productions == 2 ? 50 : 75);
            default:               return 25;
        }
    }

    @Override
    public void onPlantFood(Game game) {
        game.getSuns().add(new Sun(150, new Vec2(getCol(), getRow())));
    }
}

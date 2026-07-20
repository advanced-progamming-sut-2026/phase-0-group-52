package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.Sun;

public class SunProducer extends Plant {

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += 1;
        double interval = getType().getActionInterval();
        if (interval <= 0) interval = 24;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            game.getSuns().add(new Sun(sunAmount(), new Vec2(getCol(), getRow())));
        }
    }

    private int sunAmount() {
        switch (getType()) {
            case TWIN_SUNFLOWER:   return 100;
            case PRIMAL_SUNFLOWER: return 75;
            case SUNFLOWER:        return 50;
            default:               return 25;
        }
    }

    @Override
    public void onPlantFood(Game game) {
        game.getSuns().add(new Sun(150, new Vec2(getCol(), getRow())));
    }
}

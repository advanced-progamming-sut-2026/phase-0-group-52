package model.entities.plants;

import model.Game;
import model.Vec2;

public class Mint extends Plant {

    private static final double LIFESPAN = 5;

    private double lifeTimer = 0;

    public Mint(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onPlanted(Game game) {
        for (int i = 0; i < game.getPlants().size(); i++) {
            Plant p = game.getPlants().get(i);
            if (p != this && p.getType().getCategory() == getType().getCategory())
                p.onPlantFood(game);
        }
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        lifeTimer += 1;
        if (lifeTimer >= LIFESPAN) {
            setHp(0);
            PlantCombat.removePlant(game, this);
        }
    }
}

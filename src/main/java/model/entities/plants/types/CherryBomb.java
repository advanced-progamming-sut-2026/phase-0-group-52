package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

public class CherryBomb extends Explosive {

    public CherryBomb(Vec2 position) {
        super(Plants.CHERRY_BOMB, position);
    }

    @Override
    public void onPlanted(Game game) {
        PlantCombat.explode(game, getCol(), getRow(), 1, getAttackdamage());
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

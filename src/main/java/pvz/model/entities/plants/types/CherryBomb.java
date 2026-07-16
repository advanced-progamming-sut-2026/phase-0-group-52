package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;

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

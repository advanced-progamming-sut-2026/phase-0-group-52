package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

public class Jalapeno extends Explosive {

    public Jalapeno(Vec2 position) {
        super(Plants.JALAPENO, position);
    }

    @Override
    public void onPlanted(Game game) {
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow()))
            z.takeDamage(getAttackdamage());
        PlantCombat.meltFrozenInRow(game, getRow(), 0);
        PlantCombat.removeDeadZombies(game);
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

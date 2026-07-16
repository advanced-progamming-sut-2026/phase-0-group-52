package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

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

package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Wallnut;
import pvz.model.entities.zombies.Zombie;

public class Endurian extends Wallnut {

    private double reflectDamage;

    public Endurian(Vec2 position) {
        super(Plants.ENDURIAN, position);
        this.reflectDamage = getAttackdamage();
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;

        for (Zombie z : PlantCombat.zombiesOnCell(game, getCol(), getRow()))
            z.takeDamage(reflectDamage);
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {
        setHp(getHp() + 1000);
        reflectDamage += 5;
    }
}

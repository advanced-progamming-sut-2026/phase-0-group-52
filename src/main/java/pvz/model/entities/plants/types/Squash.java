package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

import java.util.List;

public class Squash extends Explosive {

    public Squash(Vec2 position) {
        super(Plants.SQUASH, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        for (Zombie z : PlantCombat.zombiesInRow(game, getRow())) {
            if (Math.abs(z.getPosition().x - getCol()) <= 1) {
                z.takeDamage(getAttackdamage());
                PlantCombat.removeDeadZombies(game);
                setHp(0);
                PlantCombat.removePlant(game, this);
                return;
            }
        }
    }

    @Override
    public void onPlantFood(Game game) {
        for (int i = 0; i < 2; i++) {
            List<Zombie> all = game.getZombies();
            if (all.isEmpty()) break;
            all.get(PlantCombat.RANDOM.nextInt(all.size())).takeDamage(getAttackdamage());
            PlantCombat.removeDeadZombies(game);
        }
    }
}

package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

import java.util.List;

public class Grapeshot extends Explosive {

    private static final int BOUNCES = 6;
    private static final double BOUNCE_DAMAGE = 300;

    public Grapeshot(Vec2 position) {
        super(Plants.GRAPESHOT, position);
    }

    @Override
    public void onPlanted(Game game) {
        PlantCombat.explode(game, getCol(), getRow(), 1, getAttackdamage());
        for (int i = 0; i < BOUNCES; i++) {
            List<Zombie> all = game.getZombies();
            if (all.isEmpty()) break;
            all.get(PlantCombat.RANDOM.nextInt(all.size())).takeDamage(BOUNCE_DAMAGE);
            PlantCombat.removeDeadZombies(game);
        }
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

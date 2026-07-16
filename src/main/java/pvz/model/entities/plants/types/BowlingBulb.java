package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

import java.util.List;

public class BowlingBulb extends Shooter {

    private static final double[] DAMAGE = {40, 120, 180};
    private static final double[] DELAY = {2, 5, 10};

    private int nextBulb = 0;

    public BowlingBulb(Vec2 position) {
        super(Plants.BOWLING_BULB, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += 1;
        if (actionTimer < DELAY[nextBulb]) return;
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        actionTimer = 0;
        target.takeDamage(DAMAGE[nextBulb]);
        nextBulb = (nextBulb + 1) % DAMAGE.length;
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {

        for (int i = 0; i < 3; i++) {
            List<Zombie> all = game.getZombies();
            if (all.isEmpty()) break;
            Zombie target = all.get(PlantCombat.RANDOM.nextInt(all.size()));
            PlantCombat.explode(game, target.getCol(), target.getRow(), 1, DAMAGE[2]);
        }
    }
}

package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.Shooter;
import pvz.model.entities.zombies.Zombie;

public class PeaPod extends Shooter {

    public static final int MAX_HEADS = 5;

    private int heads = 1;

    public PeaPod(Vec2 position) {
        super(Plants.PEA_POD, position);
    }

    public int getHeads() { return heads; }

    public boolean addHead() {
        if (heads >= MAX_HEADS) return false;
        heads++;
        return true;
    }

    @Override
    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        target.takeDamage(heads * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    @Override
    public void onPlantFood(Game game) {

        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target != null) target.takeDamage(heads * 20 * shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }
}

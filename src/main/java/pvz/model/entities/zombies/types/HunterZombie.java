package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class HunterZombie extends WalkingZombie {

    private static final double THROW_INTERVAL = 3;

    private double throwTimer = 0;

    public HunterZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_ICE_AGE_HUNTER, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized()) {
            throwTimer += 1;
            if (throwTimer >= THROW_INTERVAL) {
                throwTimer = 0;
                throwIce(game);
            }
        }
        super.onTick(game);
    }

    private void throwIce(Game game) {
        Plant nearest = null;
        double bestDist = Double.MAX_VALUE;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow() || p.isFrozen()) continue;
            double dist = Math.abs(getPosition().x - p.getCol());
            if (dist < bestDist) {
                bestDist = dist;
                nearest = p;
            }
        }
        if (nearest != null) nearest.addFreezeLevel();
    }
}

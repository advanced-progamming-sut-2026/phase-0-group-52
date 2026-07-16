package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class OctopusZombie extends WalkingZombie {

    private static final double THROW_INTERVAL = 5;

    private double throwTimer = 0;

    public OctopusZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_BEACH_OCTOPUS, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized()) {
            throwTimer += 1;
            if (throwTimer >= THROW_INTERVAL) {
                throwTimer = 0;
                throwOctopus(game);
            }
        }
        super.onTick(game);
    }

    private void throwOctopus(Game game) {
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
        if (nearest != null) {

            nearest.addFreezeLevel();
            nearest.addFreezeLevel();
            nearest.addFreezeLevel();
        }
    }
}

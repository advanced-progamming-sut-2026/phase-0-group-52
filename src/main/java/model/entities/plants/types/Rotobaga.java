package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Projectile;
import model.entities.plants.Muzzle;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;
import model.entities.zombies.Zombie;

import java.util.Arrays;
import java.util.List;

public class Rotobaga extends Shooter {

    private static final int VOLLEYS = 16;
    private static final double SLOPE = 0.09d;

    public Rotobaga(Vec2 position) {
        super(Plants.ROTOBAGA, position);
    }

    @Override
    public List<Muzzle> ports() {
        return Arrays.asList(
                new Muzzle("up", -1, 1),
                new Muzzle("up_back", -1, -1),
                new Muzzle("down", 1, 1),
                new Muzzle("down_back", 1, -1));
    }

    @Override
    protected boolean aims(Game game, Muzzle muzzle) {
        if (isBursting()) {
            return true;
        }
        int row = rowOf(game, muzzle);
        if (row < 0) {
            return false;
        }
        Zombie seen = muzzle.getDirection() > 0
                ? PlantCombat.frontmostAhead(game, row, getCol())
                : PlantCombat.nearestBehind(game, row, getCol());
        return seen != null;
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        super.fireFrom(game, muzzle);
        java.util.List<Projectile> flying = game.getProjectiles();
        if (flying.isEmpty()) {
            return;
        }
        Projectile shot = flying.get(flying.size() - 1);
        shot.from(getRow());
        shot.drifting(muzzle.getRowOffset() * SLOPE * muzzle.getDirection());
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }
}

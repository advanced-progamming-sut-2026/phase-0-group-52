package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Projectile;
import model.entities.plants.Muzzle;
import model.entities.plants.Plants;
import model.entities.plants.Shooter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Starfruit extends Shooter {

    private static final int VOLLEYS = 16;
    private static final String FED_ONLY = "back";
    private static final double SLOPE = 0.09d;

    private int sweep;

    public Starfruit(Vec2 position) {
        super(Plants.STARFRUIT, position);
    }

    @Override
    public List<Muzzle> ports() {
        return Arrays.asList(
                new Muzzle("front", 0, 1),
                new Muzzle("up", -1, 1),
                new Muzzle("down", 1, 1),
                new Muzzle("back_down", 1, -1),
                new Muzzle(FED_ONLY, 0, -1));
    }

    @Override
    public List<Muzzle> portsFor(String state) {
        if (Projectile.FED.equals(state)) {
            return ports();
        }
        List<Muzzle> plain = new ArrayList<Muzzle>();
        for (Muzzle muzzle : ports()) {
            if (!FED_ONLY.equals(muzzle.getName())) {
                plain.add(muzzle);
            }
        }
        return plain;
    }

    @Override
    protected boolean aims(Game game, Muzzle muzzle) {
        if (isBursting()) {
            return true;
        }
        if (FED_ONLY.equals(muzzle.getName())) {
            return false;
        }
        int row = rowOf(game, muzzle);
        if (row < 0) {
            return false;
        }
        return muzzle.getDirection() > 0
                ? model.entities.plants.PlantCombat.frontmostAhead(game, row, getCol()) != null
                : model.entities.plants.PlantCombat.nearestBehind(game, row, getCol()) != null;
    }

    @Override
    protected void fireFrom(Game game, Muzzle muzzle) {
        super.fireFrom(game, muzzle);
        java.util.List<model.entities.Projectile> flying = game.getProjectiles();
        if (flying.isEmpty()) {
            return;
        }
        model.entities.Projectile shot = flying.get(flying.size() - 1);
        shot.from(getRow());
        shot.drifting(muzzle.getRowOffset() * SLOPE * muzzle.getDirection());
    }

    @Override
    protected void fire(Game game) {
        if (!isBursting()) {
            super.fire(game);
            return;
        }
        sweep++;
        for (Muzzle muzzle : ports()) {
            int spin = muzzle.getRowOffset() + (sweep % 3) - 1;
            fireFrom(game, new Muzzle(muzzle.getName(), spin, muzzle.getDirection(),
                    muzzle.getFrame()));
        }
    }

    @Override
    protected int foodVolleys() {
        return VOLLEYS;
    }
}

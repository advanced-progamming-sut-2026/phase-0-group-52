package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class RaZombie extends WalkingZombie {

    private static final int MAX_STOLEN = 250;
    private static final double STEAL_INTERVAL = 2;
    private static final double REACH = 1.6;

    private int stolenSun = 0;
    private double stealTimer = 0;
    private boolean absorbing;
    private model.entities.Sun claimed;

    public RaZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_RA, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        absorbing = false;
        if (!isHypnotized() && stolenSun < MAX_STOLEN) {
            stealTimer += Game.SECONDS_PER_TICK;
            if (stealTimer >= STEAL_INTERVAL) {
                stealTimer = 0;
                drinkFromLawn(game);
            }
        }
        super.onTick(game);
    }

    private boolean drinkFromLawn(Game game) {
        if (claimed != null && !game.getSuns().contains(claimed)) {
            claimed = null;
        }
        if (claimed == null) {
            claimed = chooseSun(game);
            return claimed != null;
        }
        if (Math.abs(claimed.getPosition().x - getPosition().x) > REACH) {
            return true;
        }
        game.getSuns().remove(claimed);
        int taken = Math.min(claimed.getAmount(), MAX_STOLEN - stolenSun);
        stolenSun += taken;
        claimed = null;
        absorbing = true;
        System.out.println("Zombie Ra absorbed " + taken + " sun from the lawn!");
        return true;
    }

    private model.entities.Sun chooseSun(Game game) {
        model.entities.Sun best = null;
        double closest = Double.MAX_VALUE;
        for (model.entities.Sun sun : game.getSuns()) {
            if (sun.isFalling() || isClaimedByAnother(game, sun)) {
                continue;
            }
            double gap = Math.abs(sun.getPosition().x - getPosition().x);
            if (gap < closest) {
                closest = gap;
                best = sun;
            }
        }
        return best;
    }

    private boolean isClaimedByAnother(Game game, model.entities.Sun sun) {
        for (model.entities.zombies.Zombie other : game.getZombies()) {
            if (other != this && other instanceof RaZombie
                    && ((RaZombie) other).claimed == sun) {
                return true;
            }
        }
        return false;
    }

    public boolean isAbsorbing() {
        return absorbing;
    }

    @Override
    public void onDeath(Game game) {
        if (stolenSun > 0) {
            game.addSun(stolenSun);
            System.out.println("Zombie Ra died and dropped " + stolenSun + " sun back.");
        }
    }
}

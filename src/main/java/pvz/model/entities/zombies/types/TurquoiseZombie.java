package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

import java.util.ArrayList;

public class TurquoiseZombie extends WalkingZombie {

    private static final int STEAL_PER_SECOND = 25;
    private static final double STEAL_DURATION = 5;
    private static final int LASER_RANGE = 4;

    private double stealTimer = 0;
    private int stolenSun = 0;
    private boolean lasered = false;

    public TurquoiseZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_CRYSTAL_SKULL, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!lasered && !isHypnotized() && seesPlant(game)) {
            if (stealTimer < STEAL_DURATION) {
                stealTimer += 1;
                int take = Math.min(STEAL_PER_SECOND, game.getSunAmount());
                game.setSunAmount(game.getSunAmount() - take);
                stolenSun += take;
                return;
            }
            fireLaser(game);
            lasered = true;
            return;
        }
        super.onTick(game);
    }

    private boolean seesPlant(Game game) {
        for (Plant p : game.getPlants()) {
            if (Math.abs(p.getRow() - getRow()) <= LASER_RANGE
                    && Math.abs(p.getCol() - getCol()) <= LASER_RANGE)
                return true;
        }
        return false;
    }

    private void fireLaser(Game game) {
        for (int c = getCol() - LASER_RANGE; c < getCol(); c++) {
            for (Plant p : new ArrayList<Plant>(game.getPlantsAt(c, getRow()))) {
                p.setHp(0);
                p.onDeath(game);
                PlantCombat.removePlant(game, p);
            }
        }
    }

    @Override
    public void onDeath(Game game) {
        game.addSun(stolenSun / 2);
    }
}

package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

import java.util.ArrayList;

public class JalapenoZombie extends WalkingZombie {

    private static final double FUSE_TIME = 10;

    private double fuseTimer = 0;
    private boolean exploded = false;

    public JalapenoZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_JALAPENO_HEAD, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!exploded && !isHypnotized()) {
            fuseTimer += 1;
            if (fuseTimer >= FUSE_TIME) {
                exploded = true;
                for (Plant p : new ArrayList<Plant>(game.getPlants())) {
                    if (p.getRow() != getRow()) continue;
                    p.setHp(0);
                    p.onDeath(game);
                    PlantCombat.removePlant(game, p);
                }
                setHp(0);
                return;
            }
        }
        super.onTick(game);
    }
}

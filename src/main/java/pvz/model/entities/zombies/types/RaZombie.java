package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class RaZombie extends WalkingZombie {

    private static final int SUN_PER_ORB = 25;
    private static final int MAX_STOLEN = 250;

    private int stolenSun = 0;

    public RaZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_RA, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {

        if (!isHypnotized() && stolenSun < MAX_STOLEN && !game.getSuns().isEmpty()) {
            game.getSuns().remove(game.getSuns().size() - 1);
            stolenSun += SUN_PER_ORB;
        }
        super.onTick(game);
    }

    @Override
    public void onDeath(Game game) {
        game.addSun(stolenSun);
    }
}

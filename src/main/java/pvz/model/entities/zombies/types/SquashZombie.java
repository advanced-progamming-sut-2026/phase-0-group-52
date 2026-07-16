package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class SquashZombie extends WalkingZombie {

    public SquashZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_SQUASH_HEAD, line, position, chapter, type);
    }

    @Override
    protected void eat(Game game, Plant target) {
        crush(game, target);
        setHp(0);
    }
}

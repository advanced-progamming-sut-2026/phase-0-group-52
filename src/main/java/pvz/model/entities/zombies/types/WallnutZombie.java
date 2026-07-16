package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class WallnutZombie extends WalkingZombie {

    public WallnutZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_WALLNUT_HEAD, line, position, chapter, type);
    }
}

package model.entities.zombies.types;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class WallnutZombie extends WalkingZombie {

    public WallnutZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_WALLNUT_HEAD, line, position, chapter, type);
    }
}

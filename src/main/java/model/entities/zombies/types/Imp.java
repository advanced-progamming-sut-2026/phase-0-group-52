package model.entities.zombies.types;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class Imp extends WalkingZombie {

    public Imp(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_IMP, line, position, chapter, type);
    }
}

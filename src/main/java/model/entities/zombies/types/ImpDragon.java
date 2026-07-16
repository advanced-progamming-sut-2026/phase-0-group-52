package model.entities.zombies.types;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class ImpDragon extends WalkingZombie {

    public ImpDragon(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_DARK_IMP_DRAGON, line, position, chapter, type);
    }

    public boolean isFireImmune() { return true; }
}

package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class ImpDragon extends WalkingZombie {

    public ImpDragon(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_DARK_IMP_DRAGON, line, position, chapter, type);
    }

    public boolean isFireImmune() { return true; }
}

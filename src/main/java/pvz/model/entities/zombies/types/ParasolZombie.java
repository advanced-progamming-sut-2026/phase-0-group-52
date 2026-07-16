package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class ParasolZombie extends WalkingZombie {

    public ParasolZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_LOST_CITY_JANE, line, position, chapter, type);
    }

    public boolean deflectsLobbers() { return true; }
}

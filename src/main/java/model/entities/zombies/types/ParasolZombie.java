package model.entities.zombies.types;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class ParasolZombie extends WalkingZombie {

    public ParasolZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_LOST_CITY_JANE, line, position, chapter, type);
    }

    public boolean deflectsLobbers() {
        return true;
    }
}

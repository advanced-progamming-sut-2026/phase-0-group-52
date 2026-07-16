package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class Juggler extends WalkingZombie {

    private boolean spinning = false;

    public Juggler(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_DARK_JUGGLER, line, position, chapter, type);
    }

    public boolean isSpinning() { return spinning; }

    public void onProjectileIncoming() {
        if (!spinning) {
            spinning = true;
            setSpeed(getSpeed() * 2);
        }
    }

    public void stopSpinning() {
        if (spinning) {
            spinning = false;
            setSpeed(getSpeed() / 2);
        }
    }
}

package model.entities.zombies.types;

import model.ChapterType;
import model.Vec2;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

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

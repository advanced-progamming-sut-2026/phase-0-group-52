package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class ProspectorZombie extends WalkingZombie {

    private static final double FUSE_TIME = 10;

    private double fuseTimer = 0;
    private boolean dynamiteDoused = false;
    private boolean reversed = false;

    public ProspectorZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_PROSPECTOR, line, position, chapter, type);
    }

    public boolean isReversed() { return reversed; }

    public void douseDynamite() { dynamiteDoused = true; }

    @Override
    public void onTick(Game game) {
        if (!reversed && !dynamiteDoused) {
            fuseTimer += 1;
            if (fuseTimer >= FUSE_TIME) {
                reversed = true;
                getPosition().x = 0;
                return;
            }
        }
        super.onTick(game);
    }

    @Override
    public void move(Game game) {
        if (reversed) getPosition().x += getSpeed();
        else super.move(game);
    }
}

package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class NewspaperZombie extends WalkingZombie {

    private static final double NEWSPAPER_HP = 190;
    private static final double RAGE_FACTOR = 3;

    private boolean enraged = false;

    public NewspaperZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_NEWSPAPER, line, position, chapter, type);
        setArmorHp(NEWSPAPER_HP);
    }

    public boolean isEnraged() { return enraged; }

    @Override
    public void onTick(Game game) {
        if (!enraged && getArmorHp() <= 0) {
            enraged = true;
            setSpeed(getSpeed() * RAGE_FACTOR);
            setDamage(getDamage() * RAGE_FACTOR);
        }
        super.onTick(game);
    }
}

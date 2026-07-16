package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.zombies.Zombie;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class ArcadeZombie extends WalkingZombie {

    private static final double MACHINE_HP = 1100;

    public ArcadeZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_ARCADE, line, position, chapter, type);
        setArmorHp(MACHINE_HP);
    }

    public boolean hasMachine() { return getArmorHp() > 0; }

    @Override
    public void onTick(Game game) {
        if (hasMachine()) {
            for (Zombie z : game.getZombies()) {
                if (z != this && z.isHypnotized() && z.getRow() == getRow()
                        && Math.abs(z.getPosition().x - getPosition().x) <= 0.5)
                    z.setHp(0);
            }
        }
        super.onTick(game);
    }

    @Override
    protected void eat(Game game, Plant target) {
        if (hasMachine()) crush(game, target);
        else super.eat(game, target);
    }
}

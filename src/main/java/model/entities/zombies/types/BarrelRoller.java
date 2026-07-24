package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class BarrelRoller extends WalkingZombie {

    private static final double BARREL_HP = 1100;

    private boolean hadBarrel = true;

    public BarrelRoller(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_BARREL_ROLLER, line, position, chapter, type);
        setArmorHp(BARREL_HP);
    }

    public boolean hasBarrel() {
        return hadBarrel && getArmorHp() > 0;
    }

    @Override
    public void onTick(Game game) {

        if (hadBarrel && getArmorHp() <= 0) {
            hadBarrel = false;
            double x = getPosition().x;
            game.getZombies().add(new Imp(getRow(), new Vec2(x, getRow()), getChapter(), getType()));
            game.getZombies().add(new Imp(getRow(), new Vec2(x + 0.5, getRow()), getChapter(), getType()));
        }
        if (hasBarrel()) {

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
        if (hasBarrel()) crush(game, target);
        else super.eat(game, target);
    }
}

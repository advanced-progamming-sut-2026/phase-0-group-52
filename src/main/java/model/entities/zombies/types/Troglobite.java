package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.zombies.ArmorType;
import model.entities.zombies.Zombie;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class Troglobite extends WalkingZombie {

    private static final double ICE_HP = 470;

    public Troglobite(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_ICE_AGE_TROGLOBITE, line, position, chapter, type);
        setArmorType(ArmorType.ICEBLOCK);
        setArmorHp(ICE_HP);
    }

    public boolean hasIceBlock() {
        return getArmorHp() > 0;
    }

    @Override
    public void onTick(Game game) {
        if (hasIceBlock()) {
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
        if (hasIceBlock()) crush(game, target);
        else super.eat(game, target);
    }
}

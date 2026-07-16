package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class Gargantuar extends WalkingZombie {

    private boolean impThrown = false;

    public Gargantuar(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_GARGANTUAR, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!impThrown && getHp() <= Zombies.ZOMBIE_GARGANTUAR.getHp() / 2) {
            impThrown = true;

            game.getZombies().add(new Imp(getRow(), new Vec2(2, getRow()), getChapter(), getType()));
        }
        super.onTick(game);
    }

    @Override
    protected void eat(Game game, Plant target) {
        crush(game, target);
    }
}

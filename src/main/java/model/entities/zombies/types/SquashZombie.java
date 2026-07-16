package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class SquashZombie extends WalkingZombie {

    public SquashZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_SQUASH_HEAD, line, position, chapter, type);
    }

    @Override
    protected void eat(Game game, Plant target) {
        crush(game, target);
        setHp(0);
    }
}

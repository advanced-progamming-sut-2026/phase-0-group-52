package pvz.model.entities.zombies;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;

public class BasicZombie extends Zombie {

    public BasicZombie(Zombies data, int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
                data.getArmor(), chapter, type, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {

        move(game);
    }
}

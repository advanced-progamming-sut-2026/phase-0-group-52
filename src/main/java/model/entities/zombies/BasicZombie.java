package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;

public class BasicZombie extends Zombie {

    public BasicZombie(Zombies data, int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, type, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        java.util.ArrayList<Plant> here = game.getPlantsAt(getCol(), getRow());
        if (!here.isEmpty()) {
            setState(ZombieState.ATTACKING);
            Plant target = here.get(here.size() - 1);
            target.takeDamage(getDamage() * model.Game.SECONDS_PER_TICK);
            if (target.isDead()) {
                target.onDeath(game);
                model.entities.plants.PlantCombat.removePlant(game, target);
            }
        } else {
            setState(ZombieState.WALKING);
            move(game);
        }
    }
}

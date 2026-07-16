package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.zombies.Zombie;
import pvz.model.entities.zombies.ZombieState;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

import java.util.ArrayList;

public abstract class WalkingZombie extends Zombie {

    protected WalkingZombie(Zombies data, int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
                data.getArmor(), chapter, type, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        if (getState() == ZombieState.DISABLED) return;
        if (isHypnotized()) {
            hypnotizedTick(game);
            return;
        }
        Plant target = topPlantHere(game);
        if (target != null) {
            setState(ZombieState.ATTACKING);
            eat(game, target);
        } else {
            setState(ZombieState.WALKING);
            move(game);
        }
    }

    protected Plant topPlantHere(Game game) {
        ArrayList<Plant> plants = game.getPlantsAt(getCol(), getRow());
        return plants.isEmpty() ? null : plants.get(plants.size() - 1);
    }

    protected void eat(Game game, Plant target) {
        target.takeDamage(getDamage());
        if (target.isDead()) {
            target.onDeath(game);
            PlantCombat.removePlant(game, target);
        }
    }

    protected void crush(Game game, Plant target) {
        target.setHp(0);
        target.onDeath(game);
        PlantCombat.removePlant(game, target);
    }

    protected void hypnotizedTick(Game game) {
        for (Zombie z : game.getZombies()) {
            if (z != this && !z.isHypnotized() && z.getRow() == getRow()
                    && Math.abs(z.getPosition().x - getPosition().x) <= 0.5) {
                z.takeDamage(getDamage() > 0 ? getDamage() : 100);
                return;
            }
        }
        getPosition().x += getSpeed();
    }
}

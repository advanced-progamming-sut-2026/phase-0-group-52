package model.entities.zombies.types;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;
import model.entities.plants.PlantCombat;
import model.entities.zombies.ZombieType;
import model.entities.zombies.Zombies;

public class Juggler extends WalkingZombie {

    private static final double THROW_INTERVAL = 2;
    private static final double REFLECT_DAMAGE = 40;

    private double throwTimer = 0;

    public Juggler(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_DARK_JUGGLER, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized()) {
            throwTimer += Game.SECONDS_PER_TICK;
            if (throwTimer >= THROW_INTERVAL) {
                throwTimer = 0;
                throwProjectile(game);
            }
        }
        super.onTick(game);
    }

    private void throwProjectile(Game game) {
        Plant target = null;
        double bestX = -1;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow() || p.isFrozen()) continue;
            if (p.getCol() >= getPosition().x) continue;
            if (p.getCol() > bestX) {
                bestX = p.getCol();
                target = p;
            }
        }
        if (target == null) return;
        target.takeDamage(REFLECT_DAMAGE);
        if (target.isDead()) {
            target.onDeath(game);
            PlantCombat.removePlant(game, target);
        }
    }
}

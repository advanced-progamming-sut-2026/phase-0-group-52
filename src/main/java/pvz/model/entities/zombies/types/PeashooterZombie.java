package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class PeashooterZombie extends WalkingZombie {

    private static final double SHOT_INTERVAL = 1.5;
    private static final double SHOT_DAMAGE = 20;

    private double shotTimer = 0;

    public PeashooterZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_PEASHOOTER_HEAD, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        if (!isHypnotized()) {
            shotTimer += 1;
            while (shotTimer >= SHOT_INTERVAL) {
                shotTimer -= SHOT_INTERVAL;
                shoot(game);
            }
        }
        super.onTick(game);
    }

    private void shoot(Game game) {
        Plant target = null;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow() || p.getCol() > getPosition().x) continue;
            if (target == null || p.getCol() > target.getCol()) target = p;
        }
        if (target == null) return;
        target.takeDamage(SHOT_DAMAGE);
        if (target.isDead()) {
            target.onDeath(game);
            PlantCombat.removePlant(game, target);
        }
    }
}

package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.zombies.Zombie;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

public class PianistZombie extends WalkingZombie {

    private static final double SHUFFLE_INTERVAL = 4;

    private double shuffleTimer = 0;

    public PianistZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_PIANO, line, position, chapter, type);
    }

    @Override
    public void onTick(Game game) {
        shuffleTimer += 1;
        if (shuffleTimer >= SHUFFLE_INTERVAL) {
            shuffleTimer = 0;
            shuffleZombies(game);
        }
        super.onTick(game);
    }

    @Override
    protected void eat(Game game, Plant target) {
        crush(game, target);
    }

    private void shuffleZombies(Game game) {
        int rows = (game.getField() != null) ? game.getField().getRows() : 5;
        for (Zombie z : game.getZombies()) {
            if (z == this) continue;
            int target = z.getRow() + (pvz.model.entities.plants.PlantCombat.RANDOM.nextBoolean() ? 1 : -1);
            if (target < 0) target = 1;
            if (target >= rows) target = rows - 2;
            z.setLine(target);
        }
    }
}

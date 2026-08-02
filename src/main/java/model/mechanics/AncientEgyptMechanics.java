package model.mechanics;

import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.plants.PlantCombat;
import model.entities.zombies.Zombie;

public class AncientEgyptMechanics implements ChapterMechanics {

    @Override
    public void onWaveStart(Game game) {
        if (game.getField() == null) return;
        if (game.getCurrentWaveIndex() < game.getWaves().size() - 1) return;
        int spawnCol = GameField.COLS;
        boolean any = false;
        for (Zombie z : game.getZombies()) {
            if (z.getPosition().x >= spawnCol - 0.5 && PlantCombat.RANDOM.nextBoolean()) {
                int advance = 1 + PlantCombat.RANDOM.nextInt(4);
                double newX = Math.max(1, z.getPosition().x - advance);
                z.setPosition(new Vec2(newX, z.getRow()));
                any = true;
            }
        }
        if (any) System.out.println("A sandstorm sweeps the lawn! Some zombies surge in ahead of the pack!");
    }

    @Override
    public void onTick(Game game) {
    }
}

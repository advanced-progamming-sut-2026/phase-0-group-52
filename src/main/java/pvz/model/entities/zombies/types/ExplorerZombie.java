package pvz.model.entities.zombies.types;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.zombies.ZombieType;
import pvz.model.entities.zombies.Zombies;

import java.util.ArrayList;

public class ExplorerZombie extends WalkingZombie {

    private boolean torchLit = true;

    public ExplorerZombie(int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(Zombies.ZOMBIE_EXPLORER, line, position, chapter, type);
    }

    public boolean isTorchLit() { return torchLit; }

    public void douseTorch() { torchLit = false; }

    public void lightTorch() { torchLit = true; }

    @Override
    public void onTick(Game game) {
        if (torchLit && !isHypnotized()) {

            for (int c = getCol() - 1; c <= getCol(); c++) {
                for (Plant p : new ArrayList<Plant>(game.getPlantsAt(c, getRow()))) {
                    p.setHp(0);
                    p.onDeath(game);
                    PlantCombat.removePlant(game, p);
                }
            }
        }
        super.onTick(game);
    }
}

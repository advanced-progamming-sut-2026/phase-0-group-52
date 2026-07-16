package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.CellType;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.List;

public class TangleKelp extends Explosive {

    public TangleKelp(Vec2 position) {
        super(Plants.TANGLE_KELP, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        List<Zombie> here = PlantCombat.zombiesOnCell(game, getCol(), getRow());
        if (here.isEmpty()) return;
        here.get(0).setHp(0);
        PlantCombat.removeDeadZombies(game);
        setHp(0);
        PlantCombat.removePlant(game, this);
    }

    @Override
    public void onPlantFood(Game game) {

        List<Zombie> inWater = new ArrayList<Zombie>();
        for (Zombie z : game.getZombies()) {
            if (game.getField() != null
                    && game.getField().getCell(z.getCol(), z.getRow()) != null
                    && game.getField().getCell(z.getCol(), z.getRow()).getType() == CellType.WATER)
                inWater.add(z);
        }
        for (int i = 0; i < 3 && !inWater.isEmpty(); i++) {
            Zombie victim = inWater.remove(PlantCombat.RANDOM.nextInt(inWater.size()));
            victim.setHp(0);
        }
        PlantCombat.removeDeadZombies(game);
    }
}

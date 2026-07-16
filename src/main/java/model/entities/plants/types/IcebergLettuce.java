package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;
import model.entities.zombies.Zombie;

import java.util.List;

public class IcebergLettuce extends Explosive {

    public IcebergLettuce(Vec2 position) {
        super(Plants.ICEBERG_LETTUCE, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        List<Zombie> here = PlantCombat.zombiesOnCell(game, getCol(), getRow());
        if (here.isEmpty()) return;
        PlantCombat.freeze(here.get(0));
        setHp(0);
        PlantCombat.removePlant(game, this);
    }

    @Override
    public void onPlantFood(Game game) {
        for (Zombie z : game.getZombies())
            PlantCombat.freeze(z);
    }
}

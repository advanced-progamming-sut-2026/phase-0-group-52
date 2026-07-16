package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.zombies.Zombie;

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

package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.Vec2;
import pvz.model.entities.Cell;
import pvz.model.entities.CellType;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;

public class GraveBuster extends Explosive {

    public GraveBuster(Vec2 position) {
        super(Plants.GRAVE_BUSTER, position);
    }

    @Override
    public void onPlanted(Game game) {
        if (game.getField() != null) {
            Cell cell = game.getField().getCell(getCol(), getRow());
            if (cell != null && cell.getType() == CellType.TOMBSTONE)
                cell.setType(CellType.NORMAL);
        }
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

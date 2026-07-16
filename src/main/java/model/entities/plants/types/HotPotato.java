package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

public class HotPotato extends Explosive {

    public HotPotato(Vec2 position) {
        super(Plants.HOT_POTATO, position);
    }

    @Override
    public void onPlanted(Game game) {
        if (game.getField() != null) {
            Cell cell = game.getField().getCell(getCol(), getRow());
            if (cell != null && cell.getType() == CellType.FROZEN)
                cell.setType(CellType.NORMAL);
        }
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.CellType;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

public class GraveBuster extends Explosive {

    public GraveBuster(Vec2 position) {
        super(Plants.GRAVE_BUSTER, position);
    }

    @Override
    public void onPlanted(Game game) {
        if (game.getField() != null) {
            Cell cell = game.getField().getCell(getCol(), getRow());
            if (cell != null && cell.getType() == CellType.TOMBSTONE) {
                model.entities.Tombstone stone = game.tombstoneAt(getCol(), getRow());
                if (stone != null) {
                    stone.shatter();
                }
                cell.setType(CellType.NORMAL);
            }
        }
        setHp(0);
        PlantCombat.removePlant(game, this);
    }
}

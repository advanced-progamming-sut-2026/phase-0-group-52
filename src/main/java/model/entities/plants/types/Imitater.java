package model.entities.plants.types;

import model.Game;
import model.Vec2;
import model.entities.Cell;
import model.entities.plants.Modifier;
import model.entities.plants.Plant;
import model.entities.plants.PlantFactory;
import model.entities.plants.Plants;

public class Imitater extends Modifier {

    private static final Plants FALLBACK = Plants.PEASHOOTER;

    private Plants copiedType;

    public Imitater(Vec2 position) {
        super(Plants.IMITATER, position);
    }

    public Plants getCopiedType() {
        return copiedType;
    }

    public void setCopiedType(Plants copiedType) {
        this.copiedType = copiedType;
    }

    @Override
    public void onPlanted(Game game) {
        super.onPlanted(game);
        become(game);
    }

    private void become(Game game) {
        Plants wanted = copiedType == null ? FALLBACK : copiedType;
        if (wanted == Plants.IMITATER || game.getField() == null) {
            return;
        }
        Cell cell = game.getField().getCell(getCol(), getRow());
        if (cell == null) {
            return;
        }
        Plant copy = PlantFactory.create(wanted, getPosition());
        cell.getPlants().remove(this);
        game.getPlants().remove(this);
        cell.getPlants().add(copy);
        game.getPlants().add(copy);
        copy.onPlanted(game);
    }
}

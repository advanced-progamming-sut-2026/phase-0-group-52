package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.GameField;
import pvz.model.Vec2;
import pvz.model.entities.Cell;
import pvz.model.entities.CellType;
import pvz.model.entities.plants.Modifier;
import pvz.model.entities.plants.Plants;

public class LilyPad extends Modifier {

    public LilyPad(Vec2 position) {
        super(Plants.LILY_PAD, position);
    }

    @Override
    public void onPlantFood(Game game) {
        GameField field = game.getField();
        if (field == null) return;
        int copies = 0;
        for (int r = 0; r < field.getRows() && copies < 3; r++) {
            for (int c = 0; c < field.getCols() && copies < 3; c++) {
                Cell cell = field.getCell(c, r);
                if (cell != null && cell.getType() == CellType.WATER && cell.isEmpty()) {
                    LilyPad copy = new LilyPad(new Vec2(c, r));
                    cell.getPlants().add(copy);
                    game.getPlants().add(copy);
                    copies++;
                }
            }
        }
    }
}

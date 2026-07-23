package model.entities.plants.types;

import model.Game;
import model.GameField;
import model.Vec2;
import model.entities.Cell;
import model.entities.plants.Explosive;
import model.entities.plants.PlantCombat;
import model.entities.plants.Plants;

public class PrimalPotatoMine extends Explosive {

    private static final double ARM_TIME = 5;

    private double armTimer = 0;

    public PrimalPotatoMine(Vec2 position) {
        super(Plants.PRIMAL_POTATO_MINE, position);
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        if (armTimer < ARM_TIME) {
            armTimer += model.Game.SECONDS_PER_TICK;
            return;
        }
        if (!PlantCombat.zombiesOnCell(game, getCol(), getRow()).isEmpty()) {
            PlantCombat.explode(game, getCol(), getRow(), 1, getAttackdamage());
            setHp(0);
            PlantCombat.removePlant(game, this);
        }
    }

    @Override
    public void onPlantFood(Game game) {
        armTimer = ARM_TIME;
        GameField field = game.getField();
        if (field == null) return;
        int count = 2, tries = 0;
        while (count > 0 && tries < 100) {
            tries++;
            int c = PlantCombat.RANDOM.nextInt(field.getCols());
            int r = PlantCombat.RANDOM.nextInt(field.getRows());
            Cell cell = field.getCell(c, r);
            if (cell == null || !cell.isPlantable() || !cell.isEmpty()) continue;
            PrimalPotatoMine clone = new PrimalPotatoMine(new Vec2(c, r));
            clone.armTimer = ARM_TIME;
            cell.getPlants().add(clone);
            game.getPlants().add(clone);
            count--;
        }
    }
}

package pvz.model.entities.plants.types;

import pvz.model.Game;
import pvz.model.GameField;
import pvz.model.Vec2;
import pvz.model.entities.Cell;
import pvz.model.entities.plants.Explosive;
import pvz.model.entities.plants.PlantCombat;
import pvz.model.entities.plants.Plants;

public class PotatoMine extends Explosive {

    protected double armTime = 15;
    private double armTimer = 0;

    public PotatoMine(Vec2 position) {
        super(Plants.POTATO_MINE, position);
    }

    protected boolean isArmed() { return armTimer >= armTime; }

    protected int blastRadius() { return 0; }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        if (!isArmed()) {
            armTimer += 1;
            return;
        }
        if (!PlantCombat.zombiesOnCell(game, getCol(), getRow()).isEmpty()) {
            PlantCombat.explode(game, getCol(), getRow(), blastRadius(), getAttackdamage());
            setHp(0);
            PlantCombat.removePlant(game, this);
        }
    }

    @Override
    public void onPlantFood(Game game) {
        armTimer = armTime;
        spawnClones(game, 2);
    }

    protected void spawnClones(Game game, int count) {
        GameField field = game.getField();
        if (field == null) return;
        int tries = 0;
        while (count > 0 && tries < 100) {
            tries++;
            int c = PlantCombat.RANDOM.nextInt(field.getCols());
            int r = PlantCombat.RANDOM.nextInt(field.getRows());
            Cell cell = field.getCell(c, r);
            if (cell == null || !cell.isPlantable() || !cell.isEmpty()) continue;
            PotatoMine clone = new PotatoMine(new Vec2(c, r));
            clone.armTimer = clone.armTime;
            cell.getPlants().add(clone);
            game.getPlants().add(clone);
            count--;
        }
    }
}

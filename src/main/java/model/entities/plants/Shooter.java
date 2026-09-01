package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

public class Shooter extends Plant {

    public Shooter(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        if (isFrozen()) return;
        actionTimer += model.Game.SECONDS_PER_TICK;
        double interval = getActionInterval();
        if (interval <= 0) interval = 1;
        while (actionTimer >= interval) {
            actionTimer -= interval;
            shoot(game);
        }
    }

    protected void shoot(Game game) {
        Zombie target = PlantCombat.frontmostAhead(game, getRow(), getCol());
        if (target == null) return;
        int graveCol = tombstoneAhead(game, getRow(), getCol(), target.getPosition().x);
        if (graveCol >= 0) {
            model.entities.Tombstone stone = game.tombstoneAt(graveCol, getRow());
            if (stone != null) {
                stone.takeDamage(shotDamage(game));
                if (stone.isDestroyed()) {
                    System.out.println("A grave at (" + (graveCol + 1) + ", " + (getRow() + 1)
                            + ") was destroyed by plant fire.");
                }
            }
            return;
        }
        target.takeDamage(shotDamage(game));
        PlantCombat.removeDeadZombies(game);
    }

    private int tombstoneAhead(Game game, int row, int fromCol, double targetX) {
        model.GameField field = game.getField();
        if (field == null) return -1;
        for (int c = fromCol + 1; c < field.getCols(); c++) {
            model.entities.Cell cell = field.getCell(c, row);
            if (cell != null && cell.getType() == model.entities.CellType.TOMBSTONE)
                return c < targetX ? c : -1;
        }
        return -1;
    }

    protected double shotDamage(Game game) {
        double dmg = getAttackdamage();
        if (getType().getTags().contains(PlantTag.PEA))
            dmg *= PlantCombat.torchwoodFactor(game, getRow(), getCol());
        return dmg;
    }
}

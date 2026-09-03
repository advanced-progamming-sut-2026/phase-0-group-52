package model.entities.plants;

import model.Game;
import model.Vec2;

public class Lobber extends Shooter {

    public Lobber(Plants type, Vec2 position) {
        super(type, position);
    }

    @Override
    protected model.entities.Projectile.Kind bolt() {
        return model.entities.Projectile.Kind.LOB;
    }

    @Override
    protected void shoot(Game game) {
        if (getType() == Plants.PEPPER_PULT && game.getField() != null) {
            PlantCombat.meltFrozenInRow(game, getRow(), 0);
        }
        super.shoot(game);
    }
}

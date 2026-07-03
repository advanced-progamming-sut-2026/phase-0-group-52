package model.entities.plants;

import model.Game;
import model.Vec2;

/** نعناع (mint). دسته‌بندی تیم. */
public class Mint extends Plant {

    public Mint(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (تیم): رفتار این دسته را پیاده کن.
    }
}

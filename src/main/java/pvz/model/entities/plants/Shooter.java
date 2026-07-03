package model.entities.plants;

import model.Game;
import model.Vec2;

/** شلیک‌کننده‌ی مستقیم (مثل Peashooter). دسته‌بندی تیم. */
public class Shooter extends Plant {

    public Shooter(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (تیم): رفتار این دسته را پیاده کن.
    }
}

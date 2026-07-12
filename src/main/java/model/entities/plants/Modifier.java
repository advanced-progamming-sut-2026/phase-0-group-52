package model.entities.plants;

import model.Game;
import model.Vec2;

/** گیاه پشتیبان (مثل Torchwood). دسته‌بندی تیم. */
public class Modifier extends Plant {

    public Modifier(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (تیم): رفتار این دسته را پیاده کن.
    }
}

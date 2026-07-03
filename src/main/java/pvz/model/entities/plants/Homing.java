package model.entities.plants;

import model.Game;
import model.Vec2;

/** گیاه ردیاب (مثل Cat-tail). دسته‌بندی کاربر. */
public class Homing extends Plant {

    public Homing(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (کاربر): روی هر زامبی در هر جای نقشه قفل کن و پرتابه‌ی ردیاب شلیک کن.
    }
}

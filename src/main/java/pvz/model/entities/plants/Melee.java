package model.entities.plants;

import model.Game;
import model.Vec2;

/** مبارز تن‌به‌تن (مثل Bonk Choy). دسته‌بندی کاربر. */
public class Melee extends Plant {

    public Melee(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (کاربر): به زامبیِ نزدیک (همان خانه یا خانه‌ی مجاور جلو) ضربه‌ی فیزیکی بزن.
    }
}

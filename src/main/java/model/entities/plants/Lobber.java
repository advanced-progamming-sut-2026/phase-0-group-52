package model.entities.plants;

import model.Game;
import model.Vec2;

/** پرتاب‌کننده‌ی هوایی (مثل Cabbage-pult). دسته‌بندی کاربر. */
public class Lobber extends Plant {

    public Lobber(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (کاربر): هر actionInterval تیک، روی نزدیک‌ترین زامبیِ همان ردیف پرتابه فرود بیاور.
        // پرتابه‌ها موانع (سنگ قبر) را نادیده می‌گیرند.
    }
}

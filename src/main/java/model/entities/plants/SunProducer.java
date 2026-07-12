package model.entities.plants;

import model.Game;
import model.Vec2;

/** تولیدکننده‌ی خورشید (مثل Sunflower). دسته‌بندی کاربر. */
public class SunProducer extends Plant {

    public SunProducer(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (کاربر): هر actionInterval تیک یک خورشید روی گیاه تولید کن.
        // تا برداشت نشود، تولید بعدی شروع نمی‌شود.
    }

    @Override
    public void onPlantFood(Game game) {
        // TODO (کاربر): تولید فوری خورشید (مثلا Sunflower → ۱۵۰).
    }
}

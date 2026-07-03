package model.entities.plants;

import model.Game;
import model.Vec2;

/** گیاه نفوذکننده (مثل Cactus). دسته‌بندی تیم. */
public class StrikeThrough extends Plant {

    public StrikeThrough(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // TODO (تیم): رفتار این دسته را پیاده کن.
    }
}

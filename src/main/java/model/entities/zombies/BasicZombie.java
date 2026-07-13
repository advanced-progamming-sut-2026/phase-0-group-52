package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;

/**
 * زامبیِ پایه: فقط راه می‌رود و جلوترین گیاهِ ردیف را می‌خورد.
 * پوششِ زامبی معمولی و انواع زره‌دار (مخروطی/سطلی/بلوکی/شوالیه) با تنظیم armorHp. دسته‌بندی کاربر.
 */
public class BasicZombie extends Zombie {

    public BasicZombie(Zombies data, int line, Vec2 position, ChapterType chapter, ZombieType type) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, type, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        Plant target = frontPlant(game);
        if (target != null) {
            setState(ZombieState.ATTACKING);
            target.takeDamage(getDamage() / Game.TICKS_PER_SECOND); // eatDPS در هر تیک
        } else {
            setState(ZombieState.WALKING);
            move(game);
        }
    }
}

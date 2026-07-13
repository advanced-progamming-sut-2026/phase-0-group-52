package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;

/** فوتبالیست: با سرعتِ یورش می‌دود، اولین گیاه را می‌کوبد (۱۵۰۰)، سپس با سرعتِ عادی می‌خورد/می‌رود. */
public class AllStarZombie extends Zombie {

    private static final double SMASH_DAMAGE = 1500;
    private static final double CHARGE_MULTIPLIER = 2.0;   // = 1 / RunningSpeedScale(0.5)

    private final double normalSpeed;   // سرعت بعد از یورش
    private boolean charging = true;

    public AllStarZombie(Zombies data, int line, Vec2 position, ChapterType chapter) {
        super(data.getHp(), data.getSpeed() * CHARGE_MULTIPLIER, data.getEatDPS(), line, position,
            data.getArmor(), chapter, null, ZombieState.WALKING, null);
        this.normalSpeed = data.getSpeed();   // سرعتِ عادی را نگه می‌دارد
    }

    @Override
    public void onTick(Game game) {
        if (charging) {
            Plant target = frontPlant(game);
            if (target != null) {                 // به اولین گیاه رسید → کوبش
                setState(ZombieState.SPECIAL);
                target.takeDamage(SMASH_DAMAGE);
                charging = false;
                setSpeed(normalSpeed);            // بعد از کوبش، سرعت عادی
            } else {
                move(game);                       // یورش با سرعتِ بالا (۲×)
            }
            return;
        }
        walkOrEat(game);
    }
}

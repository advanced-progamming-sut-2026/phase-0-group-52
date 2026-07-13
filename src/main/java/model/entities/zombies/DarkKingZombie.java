package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;

/** پادشاه: هر ۲.۵ ثانیه به یک زامبیِ بی‌زرهِ اطرافش (محدوده‌ی ۴×۳) زره می‌دهد. خودش هم راه می‌رود/می‌خورد. */
public class DarkKingZombie extends Zombie {

    private static final int AREA_X = 4;
    private static final int AREA_Y = 3;
    private double knightTimer = 0;

    public DarkKingZombie(Zombies data, int line, Vec2 position, ChapterType chapter) {
        super(data.getHp(), data.getSpeed(), data.getEatDPS(), line, position,
            data.getArmor(), chapter, null, ZombieState.WALKING, null);
    }

    @Override
    public void onTick(Game game) {
        knightTimer += 1;
        if (knightTimer >= Game.secondsToTicks(2.5)) {
            knightTimer = 0;
            knightNearby(game);
        }
        walkOrEat(game);
    }

    /** به یک زامبیِ بی‌زرهِ داخل محدوده زره می‌دهد (یکی در هر بار). */
    private void knightNearby(Game game) {
        for (Zombie z : game.getZombies()) {
            if (z == this || z.isDead() || z.getArmorHp() > 0) continue;
            if (Math.abs(z.getCol() - getCol()) <= AREA_X && Math.abs(z.getRow() - getRow()) <= AREA_Y) {
                z.setArmorHp(370);
                z.setArmorType(ArmorType.CONEHEAD);
                System.out.println("The Dark King knighted a zombie at (" + z.getCol() + ", " + z.getRow() + ")");
                return;
            }
        }
    }
}

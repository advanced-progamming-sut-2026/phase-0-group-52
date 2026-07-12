package model.entities.zombies;

import model.ChapterType;
import model.Game;
import model.Vec2;
import model.entities.plants.Plant;

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

    /** جلوترین گیاهِ خانه‌ای که زامبی روی آن است (همان که جلویش را گرفته). */
    private Plant frontPlant(Game game) {
        Plant closest = null;
        double zx = getPosition().x;            // مختصات دقیقِ (اعشاری) زامبی
        double best = Double.MAX_VALUE;
        for (Plant p : game.getPlants()) {
            if (p.getRow() != getRow()) continue;
            double dist = zx - p.getCol();      // فاصله تا گیاهِ چپ‌تر یا هم‌محل
            if (dist >= 0 && dist < best) {
                best = dist;
                closest = p;
            }
        }
        // فقط وقتی بخور که واقعاً رسیده باشی (داخل همان خانه) — جلوی «خوردن از دور» را می‌گیرد
        return (best < 1.0) ? closest : null;
    }

}

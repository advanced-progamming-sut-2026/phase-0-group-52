package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

/** مبارز تن‌به‌تن: Bonk Choy / Phat Beet / Chomper / Wasabi Whip / Kiwibeast. دسته‌بندی کاربر. */
public class Melee extends Plant {

    private double digestTimer = 0;   // فقط Chomper: زمانِ هضم
    private int stage = 1;            // فقط Kiwibeast
    private double growthTimer = 0;

    public Melee(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        // Chomper در حال هضم: کاری نمی‌کند
        if (digestTimer > 0) {
            digestTimer -= 1;
            return;
        }

        // Chomper: بدون interval — به‌محضِ رسیدنِ زامبی می‌بلعد و ۴۰ث هضم می‌کند
        if (getType() == Plants.CHOMPER) {
            Zombie t = frontZombie(game);
            if (t != null) {
                t.takeDirectDamage(9999);
                digestTimer = Game.secondsToTicks(40);
            }
            return;
        }

        if (getType() == Plants.KIWIBEAST) {
            grow();
        }

        double interval = Game.secondsToTicks(getType().getActionInterval());
        if (actionTimer < interval) {
            actionTimer += 1;
        }
        if (actionTimer < interval) {
            return;
        }

        boolean acted = false;
        switch (getType()) {
            case PHAT_BEET:
                acted = damageArea(game, getAttackdamage()); // ۱۵ در ۳×۳
                break;
            case KIWIBEAST:
                acted = damageArea(game, stageDamage());     // ۱۵/۳۰/۴۵ در ۳×۳
                break;
            default: // BONK_CHOY, WASABI_WHIP (جلو و عقب)
                acted = hitFrontAndBack(game, getAttackdamage());
                break;
        }

        if (acted) {
            actionTimer = 0;
        }
    }



    @Override
    public void onPlantFood(Game game) {
        damageArea(game, getAttackdamage() * 3); // ساده‌سازی: ضربه‌ی قویِ مساحتی
    }

    // ---------- کمکی ----------

    /** نزدیک‌ترین زامبیِ زنده‌ی جلو (خانه‌ی خودش یا جلویی). */
    private Zombie frontZombie(Game game) {
        Zombie best = null;
        double bx = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getRow() != getRow()) continue;
            if (z.getPosition().x < getCol()) continue;
            if (z.getPosition().x < bx) { bx = z.getPosition().x; best = z; }
        }
        return (best != null && best.getPosition().x - getCol() < 2) ? best : null;
    }

    /** ضربه به زامبی‌های خانه‌ی جلو، خود، و عقب. */
    private boolean hitFrontAndBack(Game game, double dmg) {
        boolean hit = false;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getRow() != getRow()) continue;
            int dc = z.getCol() - getCol();
            if (dc >= -1 && dc <= 1) { z.takeDamage(dmg); hit = true; }
        }
        return hit;
    }

    /** آسیب مساحتیِ ۳×۳ حولِ خودِ گیاه؛ true اگر به کسی خورد. */
    private boolean damageArea(Game game, double dmg) {
        boolean hit = false;
        for (Zombie z : game.getZombies()) {
            if (z.isDead()) continue;
            if (Math.abs(z.getCol() - getCol()) <= 1 && Math.abs(z.getRow() - getRow()) <= 1) {
                z.takeDamage(dmg); hit = true;
            }
        }
        return hit;
    }

    private int stageDamage() { return stage == 1 ? 15 : stage == 2 ? 30 : 45; }

    private void grow() {
        if (stage >= 3) return;
        growthTimer += 1;
        if (stage == 1 && growthTimer >= Game.secondsToTicks(24)) stage = 2;
        else if (stage == 2 && growthTimer >= Game.secondsToTicks(96)) stage = 3;
    }
}

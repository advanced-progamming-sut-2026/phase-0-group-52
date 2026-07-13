package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.Zombie;

/** پرتاب‌کننده‌ی هوایی: Cabbage / Kernel / Melon / Winter Melon / Pepper. دسته‌بندی کاربر. */
public class Lobber extends Plant {

    private boolean butterNext = false; // فقط Kernel-pult: تناوبِ ذرت/کره

    public Lobber(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        double interval = Game.secondsToTicks(getType().getActionInterval());
        if (actionTimer < interval) {
            actionTimer += 1;
        }
        if (actionTimer < interval) {
            return;
        }

        Zombie target = nearestInRow(game);
        if (target == null) {
            return; // هدفی نیست؛ آماده بمان
        }

        switch (getType()) {
            case KERNEL_PULT:
                if (butterNext) {
                    // کره: آسیب + توقف موقت
                    target.takeDamageFromLobber(40);
                    target.applyStun(Game.secondsToTicks(3));
                } else {
                    // ذرت
                    target.takeDamageFromLobber(getAttackdamage()); // 20
                }
                butterNext = !butterNext;
                break;

            case MELON_PULT:
            case PEPPER_PULT:
                // آسیب مساحتیِ ۳×۳
                damageArea(game, target, getAttackdamage());
                break;

            case WINTER_MELON:
                // AoE + کندکننده
                damageArea(game, target, getAttackdamage());
                slowArea(game, target);
                break;

            default:
                // Cabbage-pult: تک‌هدف
                target.takeDamageFromLobber(getAttackdamage()); // 40
                break;
        }

        actionTimer = 0;
    }

    @Override
    public void onPlantFood(Game game) {
        // ساده‌سازی: پرتاب سنگین به همه‌ی زامبی‌های هم‌ردیف
        for (Zombie z : game.getZombies()) {
            if (!z.isDead() && z.getRow() == getRow()) {
                z.takeDamageFromLobber(getAttackdamage() * 2);
            }
        }
    }

    // ---------- کمکی ----------

    /** نزدیک‌ترین زامبیِ زنده‌ی هم‌ردیف که جلوی گیاه است (موانع نادیده). */
    private Zombie nearestInRow(Game game) {
        Zombie best = null;
        double bestX = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getRow() != getRow()) continue;
            if (z.getPosition().x < getCol()) continue;       // فقط جلوی گیاه (سمت راست)
            if (z.getPosition().x < bestX) {
                bestX = z.getPosition().x;
                best = z;
            }
        }
        return best;
    }

    /** آسیب مساحتیِ ۳×۳ حول خانه‌ی برخورد. */
    private void damageArea(Game game, Zombie center, double dmg) {
        int cc = center.getCol(), cr = center.getRow();
        for (Zombie z : game.getZombies()) {
            if (z.isDead()) continue;
            if (Math.abs(z.getCol() - cc) <= 1 && Math.abs(z.getRow() - cr) <= 1) {
                z.takeDamageFromLobber(dmg);
            }
        }
    }

    /** کندکردنِ زامبی‌های ۳×۳ حول برخورد (Winter Melon). */
    private void slowArea(Game game, Zombie center) {
        int cc = center.getCol(), cr = center.getRow();
        for (Zombie z : game.getZombies()) {
            if (z.isDead()) continue;
            if (Math.abs(z.getCol() - cc) <= 1 && Math.abs(z.getRow() - cr) <= 1) {
                z.applySlow(Game.secondsToTicks(5), 0.5); // ۵ث با نصفِ سرعت
            }
        }
    }
}

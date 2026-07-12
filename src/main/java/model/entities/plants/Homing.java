package model.entities.plants;

import model.Game;
import model.Vec2;
import model.entities.zombies.ArmorType;
import model.entities.zombies.Zombie;

/** گیاه ردیاب: Cat-tail / Electric Blueberry / Caulipower / Magnet-shroom. دسته‌بندی کاربر. */
public class Homing extends Plant {

    public Homing(Plants type, Vec2 position) {
        super(type, type.getBaseHP(), type.getCost(), position, type.getDamage());
    }

    @Override
    public void onTick(Game game) {
        double interval = Game.secondsToTicks(getType().getActionInterval());
        if (actionTimer < interval) actionTimer += 1;
        if (actionTimer < interval) return;

        // Magnet-shroom آسیب نمی‌زند؛ فلزِ سرِ زامبی را می‌رباید
        if (getType() == Plants.MAGNET_SHROOM) {
            if (disarmNearestMetal(game)) actionTimer = 0;
            return;
        }

        // بقیه: به نزدیک‌ترین زامبیِ نقشه شلیک هدف‌دار
        Zombie target = nearestAnywhere(game);
        if (target != null) {
            target.takeDamage(getAttackdamage()); // Cat-tail 15 | Blueberry 5000 | Caulipower نابودی
            actionTimer = 0;
        }
    }

    @Override
    public void onPlantFood(Game game) {
        switch (getType()) {
            case ELECTRIC_BLUEBERRY:
                destroyNearest(game, 3);            // نابودی ۳ زامبی (با takeDirectDamage)
                break;

            case MAGNET_SHROOM:
                for (int i = 0; i < 3; i++) {
                    disarmNearestMetal(game);       // ۳ بار خلع‌سلاح فلز
                }
                break;

            default:  // Cat-tail و Caulipower
                Zombie z = nearestAnywhere(game);
                if (z != null) {
                    if (getType() == Plants.CAULIPOWER) {
                        // نابودی فوری (هیپنوتیزم در فاز ۱ ساده‌سازی شده)
                        z.takeDirectDamage(9999);
                    } else {
                        // Cat-tail: آسیب معمولی
                        z.takeDamage(getAttackdamage());
                    }
                }
                break;
        }
    }



    // ---------- کمکی ----------

    /** نزدیک‌ترین زامبیِ زنده در کل نقشه. */
    private Zombie nearestAnywhere(Game game) {
        Zombie best = null;
        double bestDist = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead()) continue;
            double dx = z.getPosition().x - getCol();
            double dy = z.getRow() - getRow();
            double d = dx * dx + dy * dy;
            if (d < bestDist) { bestDist = d; best = z; }
        }
        return best;
    }

    /** Magnet: نزدیک‌ترین زامبیِ زره‌فلزی (سطل) را خلع‌سلاح می‌کند؛ true اگر کاری انجام شد. */
    private boolean disarmNearestMetal(Game game) {
        Zombie best = null;
        double bestDist = Double.MAX_VALUE;
        for (Zombie z : game.getZombies()) {
            if (z.isDead() || z.getArmorHp() <= 0 || !isMetal(z.getArmorType())) continue;
            double dx = z.getPosition().x - getCol();
            double dy = z.getRow() - getRow();
            double d = dx * dx + dy * dy;
            if (d < bestDist) { bestDist = d; best = z; }
        }
        if (best == null) return false;
        best.setArmorHp(0);
        best.setArmorType(ArmorType.DEFAULT);
        return true;
    }

    private boolean isMetal(ArmorType t) {
        return t == ArmorType.BUCKETHEAD; // فلزی (سطل)؛ کلاه‌خودِ فوتبالیست هم اگر بعداً افزوده شد
    }

    /** نابودی n زامبیِ نزدیک (برای غذای گیاهِ Electric Blueberry). */
    private void destroyNearest(Game game, int n) {
        for (int i = 0; i < n; i++) {
            Zombie z = nearestAnywhere(game);
            if (z == null) break;
            z.takeDirectDamage(9999); // زره را رد می‌کند، کاملاً نابود
        }
    }
}

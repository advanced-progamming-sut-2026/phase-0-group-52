package model.entities.zombies;

import model.ChapterType;
import model.Vec2;

/** بر اساس نوعِ زامبی، زیرکلاسِ درست را می‌سازد و زره‌اش را مقداردهی می‌کند. */
public final class ZombieFactory {

    private ZombieFactory() {}

    public static Zombie create(Zombies data, int lane, int spawnCol, ChapterType chapter) {
        Vec2 pos = new Vec2(spawnCol, lane);

        Zombie z;
        switch (data) {
            // ---- ۵ زامبیِ خاص ----
            case ZOMBIE_MODERN_ALL_STAR:   // فوتبالیست
                z = new AllStarZombie(data, lane, pos, chapter);
                break;
            case ZOMBIE_TOMB_RAISER:       // قبرساز
                z = new TombRaiserZombie(data, lane, pos, chapter);
                break;
            case ZOMBIE_DARK_KING:         // پادشاه
                z = new DarkKingZombie(data, lane, pos, chapter);
                break;
            case ZOMBIE_BEACH_FISHERMAN:   // ماهیگیر
                z = new FishermanZombie(data, lane, pos, chapter);
                break;
            case ZOMBIE_BEACH_SNORKEL:     // غواص
                z = new SnorkelZombie(data, lane, pos, chapter);
                break;

            case ZOMBIE_BEACH_SNORKEL -> new SnorkelZombie(data, lane, pos, chapter);

            // ---- راه‌رونده‌های ساده (معمولی + همه‌ی زره‌دارها) ----
            // ZOMBIE_DEFAULT، ZOMBIE_ARMOR1(مخروطی)، ZOMBIE_ARMOR2(سطلی)،
            // ZOMBIE_ARMOR4(بلوکی)، ZOMBIE_DARK_ARMOR3(شوالیه)
            default:
                z = new BasicZombie(data, lane, pos, chapter, ZombieType.NORMAL);
                break;
        }

        z.setArmorHp(Zombie.armorHpFor(data.getArmor())); // زره از روی نوعش پر می‌شود
        return z;
    }
}

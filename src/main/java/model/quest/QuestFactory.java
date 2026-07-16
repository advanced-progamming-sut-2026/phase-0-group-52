package model.quest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ساختِ لیستِ کوئستِ یک کاربر و تخصیصِ «متغیر»ها، و ریستِ روزانه.
 *
 * <p>مقادیرِ متغیر فعلاً ثابت‌اند (برای تعیّن و تست‌پذیری)؛ در صورتِ نیاز به تنوعِ روزانه می‌توان
 * در {@link #assignVariable} تصادفی‌سازی کرد.</p>
 */
public final class QuestFactory {

    private QuestFactory() {}

    /** لیستِ کاملِ ۱۹ کوئست برای یک کاربرِ جدید (با متغیرهای تخصیص‌یافته و تاریخِ امروز). */
    public static QuestState buildDefault(String username) {
        List<QuestProgress> list = new ArrayList<>();
        for (QuestDef def : QuestDef.values()) {
            QuestProgress qp = new QuestProgress(def);
            assignVariable(qp);
            list.add(qp);
        }
        return new QuestState(username, LocalDate.now().toString(), list);
    }

    /** تخصیصِ «متغیر»ِ نمونه به کوئست‌های متغیر‌دار. */
    public static void assignVariable(QuestProgress qp) {
        switch (qp.getDef()) {
            case DAILY_SUN:
                qp.setVarInt(3000);
                qp.setTarget(3000);
                break;
            case CHAPTER_HUNTER:
                qp.setVarStr("ANCIENT_EGYPT");
                break;
            case PLANT_PRO:
                qp.setVarStr("PEASHOOTER");
                break;
            case THRIFTY_HERBIVORE:
                qp.setVarInt(3);
                break;
            case FAMILY_KILL:
            case BLOOM_LIMITS:
                qp.setVarStr("SHOOTER");
                break;
            case ONE_COLUMN_LESS:
                qp.setVarInt(4);
                break;
            case DEFENSELESS_ROW:
                qp.setVarInt(2);
                break;
            case DEFENSELESS_CROSS:
                qp.setVarInt(2);
                break;
            default:
                // بدونِ متغیر
                break;
        }
    }

    /** ریستِ روزانه: کوئست‌های DAILY صفر و متغیرشان دوباره تخصیص می‌شود؛ MAIN/EPIC دست‌نخورده می‌مانند. */
    public static void resetDaily(QuestState state) {
        for (QuestProgress qp : state.getQuests()) {
            if (qp.getDef().isDaily()) {
                qp.setProgress(0);
                qp.setCompleted(false);
                qp.setClaimed(false);
                assignVariable(qp);
            }
        }
        state.setLastResetDate(LocalDate.now().toString());
    }
}

package model.quest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class QuestFactory {

    private static final int[] MOWER_TARGETS = {10, 20, 30, 40, 50};

    private QuestFactory() {}

    public static QuestState buildDefault(String username) {
        List<QuestProgress> list = new ArrayList<>();
        for (QuestDef def : QuestDef.values()) {
            QuestProgress qp = new QuestProgress(def);
            assignVariable(qp);
            list.add(qp);
        }
        return new QuestState(username, LocalDate.now().toString(), list);
    }

    public static void assignVariable(QuestProgress qp) {
        switch (qp.getDef()) {
            case MOWER_TIME:
                qp.setVarInt(MOWER_TARGETS[new java.util.Random().nextInt(MOWER_TARGETS.length)]);
                qp.setTarget(qp.getVarInt());
                break;
            case DAILY_SUN:
                qp.setVarInt(3000);
                qp.setTarget(3000);
                break;
            case CHAPTER_HUNTER:
                qp.setVarStr("ANCIENT_EGYPT");
                break;
            case CHAPTER_HUNTER_ICEAGE:
                qp.setVarStr("FROSTBITE_CAVES");
                break;
            case CHAPTER_HUNTER_BEACH:
                qp.setVarStr("BIG_WAVE_BEACH");
                break;
            case CHAPTER_HUNTER_DARK:
                qp.setVarStr("DARK_AGES");
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

                break;
        }
    }

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

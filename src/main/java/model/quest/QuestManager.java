package model.quest;

import model.ChapterType;
import model.Game;
import model.GameStats;
import model.User;
import model.entities.plants.PlantTag;
import model.entities.plants.Plants;
import model.entities.plants.PlantsCategory;

import java.util.List;


public class QuestManager {

    private static final int THIRTY_SECONDS_TICKS = 300;   // ۳۰ث × ۱۰ تیک
    private static final int MAX_DIFFICULTY = 5;

    private final RewardService reward = new RewardService();

    public void onLevelEnd(User user, List<QuestProgress> quests, Game game, boolean won) {
        GameStats stats = game.getStats();
        for (QuestProgress qp : quests) {
            if (qp.isCompleted()) {
                continue;
            }
            if (evaluate(qp, game, stats, user, won)) {
                qp.setCompleted(true);
                reward.grant(user, qp);
                qp.setClaimed(true);   // پاداشِ خودکار در لحظه‌ی تکمیل
                if (qp.getDef().isDaily()) {
                    user.setQuestDailyNum(user.getQuestDailyNum() + 1);
                } else {
                    user.setQuestNonDailyNum(user.getQuestNonDailyNum() + 1);
                }
            }
        }
    }

    private boolean evaluate(QuestProgress qp, Game game, GameStats stats, User user, boolean won) {
        switch (qp.getDef()) {
            case DAILY_SUN:                 // مجموعِ خورشیدِ برداشت‌شده در طولِ روز
                qp.setProgress(qp.getProgress() + stats.getSunCollected());
                return qp.getProgress() >= qp.getTarget();

            case CHAPTER_HUNTER:            // ۵۰ کشتنِ تجمعی از فصلِ مشخص
                if (chapterMatches(game, qp.getVarStr())) {
                    qp.setProgress(qp.getProgress() + stats.getZombiesKilled());
                }
                return qp.getProgress() >= qp.getTarget();

            case PLANT_PRO:                 // ۱۰ کشتن فقط با گیاهِ مشخص
                return onlyPlantTypeUsed(stats, Plants.valueOf(qp.getVarStr()))
                    && stats.getZombiesKilled() >= qp.getTarget();

            case ONLY_CACTUS:               // ۱۰ کشتن فقط با کاکتوس
                return onlyPlantTypeUsed(stats, Plants.CACTUS)
                    && stats.getZombiesKilled() >= qp.getTarget();

            case THRIFTY_HERBIVORE:         // برد با از دست دادنِ حداکثر n گیاه
                return won && stats.getPlantsLost() <= qp.getVarInt();

            case DEFENSE_MASTER:            // برد دقیقاً با صفر خورشید
                return won && stats.getFinalSun() == 0;

            case QUICK_KILLS:               // ۱۰ کشتن در ۳۰ثِ اولِ موج
                return stats.killsWithinTicksOfFirstWave(THIRTY_SECONDS_TICKS) >= qp.getTarget();

            case DEMOLITION_PRO:            // ۳ گیاهِ انفجاری در یک مرحله
                return countCategory(stats, PlantsCategory.EXPLOSIVE) >= qp.getTarget();

            case SYMMETRY:                  // باغچه‌ی نهاییِ متقارن
                return won && isBoardSymmetric(game);

            case FAMILY_KILL:               // فقط با یک خانواده‌ی گیاهی
                return won && onlyFamilyUsed(stats, qp.getVarStr()) && stats.getZombiesKilled() > 0;

            case BLOOM_LIMITS:              // برد بدونِ استفاده از یک خانواده
                return won && familyNotUsed(stats, qp.getVarStr());

            case NIGHT_OR_DAY:              // بردِ مرحله‌ی روز فقط با گیاهانِ شب/قارچ
                return won && isDayLevel(game) && allPlantsNightTagged(stats);

            case WIN_STREAK:                // ۵ بردِ متوالی با بیشترین سختی
                if (won && user.getDifficultyLevel() == MAX_DIFFICULTY) {
                    qp.setProgress(qp.getProgress() + 1);
                } else if (!won) {
                    qp.setProgress(0);
                }
                return qp.getProgress() >= qp.getTarget();

            case ALMOST_WON:                // ۱۰ کشتن در ستون‌صفرِ ردیفِ بدونِ چمن‌زن (تجمعیِ روزانه)
                qp.setProgress(qp.getProgress() + stats.getKillsAtColZeroNoMower());
                return qp.getProgress() >= qp.getTarget();

            case OCD:                       // بردِ نامتقارن (به‌جز ردیفِ وسط)
                return won && !isBoardSymmetric(game);

            case CLOUDY_DAY:                // برد فقط با ۳ گیاهِ خورشیدزا
                return won && allCategory(stats, PlantsCategory.SUN_PRODUCER)
                    && countCategory(stats, PlantsCategory.SUN_PRODUCER) == 3;

            case ONE_COLUMN_LESS:           // برد بدونِ کاشت در ستونِ n
                return won && columnEmpty(stats, qp.getVarInt());

            case DEFENSELESS_ROW:           // برد بدونِ کاشت در سطرِ n
                return won && rowEmpty(stats, qp.getVarInt());

            case DEFENSELESS_CROSS:         // برد با ستون و سطرِ n خالی
                return won && columnEmpty(stats, qp.getVarInt()) && rowEmpty(stats, qp.getVarInt());

            default:
                return false;
        }
    }

    // ======================================================================

    private boolean chapterMatches(Game game, String chapterName) {
        return chapterName != null && game.getField() != null
            && game.getField().getChapter().name().equalsIgnoreCase(chapterName);
    }

    private boolean isDayLevel(Game game) {
        return game.getField() == null || game.getField().getChapter() != ChapterType.DARK_AGES;
    }

    private boolean onlyPlantTypeUsed(GameStats s, Plants type) {
        if (s.getPlantsPlanted().isEmpty()) {
            return false;
        }
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getType() != type) {
                return false;
            }
        }
        return true;
    }

    private boolean onlyFamilyUsed(GameStats s, String family) {
        if (s.getPlantsPlanted().isEmpty() || family == null) {
            return false;
        }
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (!p.getType().getCategory().name().equalsIgnoreCase(family)) {
                return false;
            }
        }
        return true;
    }

    private boolean familyNotUsed(GameStats s, String family) {
        if (family == null) {
            return true;
        }
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getType().getCategory().name().equalsIgnoreCase(family)) {
                return false;
            }
        }
        return true;
    }

    private int countCategory(GameStats s, PlantsCategory cat) {
        int count = 0;
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getType().getCategory() == cat) {
                count++;
            }
        }
        return count;
    }

    private boolean allCategory(GameStats s, PlantsCategory cat) {
        if (s.getPlantsPlanted().isEmpty()) {
            return false;
        }
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getType().getCategory() != cat) {
                return false;
            }
        }
        return true;
    }

    private boolean allPlantsNightTagged(GameStats s) {
        if (s.getPlantsPlanted().isEmpty()) {
            return false;
        }
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            List<PlantTag> tags = p.getType().getTags();
            if (!tags.contains(PlantTag.SHROOM) && !tags.contains(PlantTag.NIGHT)) {
                return false;
            }
        }
        return true;
    }

    private boolean columnEmpty(GameStats s, int col) {
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getCol() == col) {
                return false;
            }
        }
        return true;
    }

    private boolean rowEmpty(GameStats s, int row) {
        for (GameStats.PlantPlacement p : s.getPlantsPlanted()) {
            if (p.getRow() == row) {
                return false;
            }
        }
        return true;
    }


    private boolean isBoardSymmetric(Game game) {
        int rows = game.getField().getRows();
        int mid = rows / 2;
        var plants = game.getPlants();
        for (var p : plants) {
            if (p.getRow() == mid) {
                continue;
            }
            int mirror = rows - 1 - p.getRow();
            boolean found = false;
            for (var q : plants) {
                if (q.getCol() == p.getCol() && q.getRow() == mirror && q.getType() == p.getType()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}

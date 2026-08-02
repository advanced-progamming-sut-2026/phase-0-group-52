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

    private static final int THIRTY_SECONDS_TICKS = 300;
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
                qp.setClaimed(true);
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
            case DAILY_SUN:
                qp.setProgress(qp.getProgress() + stats.getSunCollected());
                return qp.getProgress() >= qp.getTarget();

            case CHAPTER_HUNTER:
                if (chapterMatches(game, qp.getVarStr())) {
                    qp.setProgress(qp.getProgress() + stats.getZombiesKilled());
                }
                return qp.getProgress() >= qp.getTarget();
            case PLANT_PRO:
                return onlyPlantTypeUsed(stats, Plants.valueOf(qp.getVarStr()))
                    && stats.getZombiesKilled() >= qp.getTarget();
            case ONLY_CACTUS:
                return onlyPlantTypeUsed(stats, Plants.CACTUS)
                    && stats.getZombiesKilled() >= qp.getTarget();
            case THRIFTY_HERBIVORE:
                return won && stats.getPlantsLost() <= qp.getVarInt();
            case DEFENSE_MASTER:
                return won && stats.getFinalSun() == 0;
            case QUICK_KILLS:
                return stats.killsWithinTicksOfFirstWave(THIRTY_SECONDS_TICKS) >= qp.getTarget();
            case DEMOLITION_PRO:
                return countCategory(stats, PlantsCategory.EXPLOSIVE) >= qp.getTarget();
            case SYMMETRY:
                return won && isBoardSymmetric(game);
            case FAMILY_KILL:
                return won && onlyFamilyUsed(stats, qp.getVarStr()) && stats.getZombiesKilled() > 0;
            case BLOOM_LIMITS:
                return won && familyNotUsed(stats, qp.getVarStr());
            case NIGHT_OR_DAY:
                return won && isDayLevel(game) && allPlantsNightTagged(stats);
            case WIN_STREAK:
                if (won && user.getDifficultyLevel() == MAX_DIFFICULTY) {
                    qp.setProgress(qp.getProgress() + 1);
                } else if (!won) {
                    qp.setProgress(0);
                }
                return qp.getProgress() >= qp.getTarget();
            case ALMOST_WON:
                qp.setProgress(qp.getProgress() + stats.getKillsAtColZeroNoMower());
                return qp.getProgress() >= qp.getTarget();
            case OCD:
                return won && !isBoardSymmetric(game);
            case CLOUDY_DAY:
                return won && allCategory(stats, PlantsCategory.SUN_PRODUCER)
                    && countCategory(stats, PlantsCategory.SUN_PRODUCER) == 3;
            case ONE_COLUMN_LESS:
                return won && columnEmpty(stats, qp.getVarInt());
            case DEFENSELESS_ROW:
                return won && rowEmpty(stats, qp.getVarInt());
            case DEFENSELESS_CROSS:
                return won && columnEmpty(stats, qp.getVarInt()) && rowEmpty(stats, qp.getVarInt());
            default: return false;}
    }

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
        java.util.List<model.entities.plants.Plant> plants = game.getPlants();
        for (model.entities.plants.Plant p : plants) {
            if (p.getRow() == mid) {
                continue;
            }
            int mirror = rows - 1 - p.getRow();
            boolean found = false;
            for (model.entities.plants.Plant q : plants) {
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

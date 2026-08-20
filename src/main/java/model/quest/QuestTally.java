package model.quest;

import model.User;

public final class QuestTally {

    private QuestTally() {
    }

    public static void record(User user, QuestCategory category) {
        if (user == null || category == null) {
            return;
        }
        if (category == QuestCategory.DAILY) {
            user.setQuestDailyNum(user.getQuestDailyNum() + 1);
            return;
        }
        user.setQuestNonDailyNum(user.getQuestNonDailyNum() + 1);
        if (category == QuestCategory.MAIN) {
            user.setQuestMainNum(user.getQuestMainNum() + 1);
        } else {
            user.setQuestEpicNum(user.getQuestEpicNum() + 1);
        }
    }

    public static int finished(User user, QuestCategory category) {
        if (user == null || category == null) {
            return 0;
        }
        if (category == QuestCategory.DAILY) {
            return user.getQuestDailyNum();
        }
        if (category == QuestCategory.MAIN) {
            return user.getQuestMainNum();
        }
        return user.getQuestEpicNum();
    }

    public static int total(User user) {
        if (user == null) {
            return 0;
        }
        return user.getQuestDailyNum() + user.getQuestNonDailyNum();
    }
}

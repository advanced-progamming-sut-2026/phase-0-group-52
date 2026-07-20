package model.quest;

import static model.quest.QuestCategory.DAILY;
import static model.quest.QuestCategory.EPIC;
import static model.quest.QuestCategory.MAIN;
import static model.quest.QuestPriorities.CRITICAL;
import static model.quest.QuestPriorities.HIGH;
import static model.quest.QuestPriorities.LOW;
import static model.quest.QuestPriorities.MEDIUM;
import static model.quest.RewardType.COIN;
import static model.quest.RewardType.GEM;
import static model.quest.RewardType.PLANT_UNLOCK;
import static model.quest.RewardType.SEED_PACKET;

public enum QuestDef {
    DAILY_SUN         ("Daily Sun Collector",        DAILY, MEDIUM,   COIN,        0,    3000),
    CHAPTER_HUNTER    ("Chapter Hunter",             MAIN,  HIGH,     SEED_PACKET, 10,   50),
    PLANT_PRO         ("Professional Plant User",    DAILY, HIGH,     PLANT_UNLOCK, 1,   10),
    ONLY_CACTUS       ("Only Cactus",               DAILY, HIGH,     GEM,         20,   10),
    THRIFTY_HERBIVORE ("Thrifty Herbivore",          MAIN,  HIGH,     SEED_PACKET, 20,   1),
    DEFENSE_MASTER    ("Defense Master",             EPIC,  CRITICAL, GEM,         200,  1),
    QUICK_KILLS       ("Quick Kills",                MAIN,  MEDIUM,   COIN,        500,  10),
    DEMOLITION_PRO    ("Demolition Pro",             DAILY, LOW,      COIN,        100,  3),
    SYMMETRY          ("Symmetry",                   DAILY, HIGH,     COIN,        500,  1),
    FAMILY_KILL       ("Family Kill",                DAILY, MEDIUM,   COIN,        1000, 1),
    BLOOM_LIMITS      ("Bloom in Limits",            DAILY, HIGH,     GEM,         100,  1),
    NIGHT_OR_DAY      ("Night or Day",               EPIC,  HIGH,     GEM,         20,   1),
    WIN_STREAK        ("Win Streak",                 DAILY, MEDIUM,   COIN,        5000, 5),
    ALMOST_WON        ("Almost Won",                 DAILY, MEDIUM,   COIN,        300,  10),
    OCD               ("No OCD",                     DAILY, MEDIUM,   COIN,        800,  1),
    CLOUDY_DAY        ("Cloudy Day",                 DAILY, HIGH,     GEM,         10,   1),
    ONE_COLUMN_LESS   ("One Column Less",            DAILY, HIGH,     GEM,         10,   1),
    DEFENSELESS_ROW   ("Defenseless Row",            DAILY, HIGH,     GEM,         20,   1),
    DEFENSELESS_CROSS ("Defenseless Cross",          DAILY, HIGH,     GEM,         25,   1);

    private final String displayName;
    private final QuestCategory category;
    private final QuestPriorities priority;
    private final RewardType rewardType;
    private final int rewardAmount;
    private final int target;

    QuestDef(String displayName, QuestCategory category, QuestPriorities priority,
             RewardType rewardType, int rewardAmount, int target) {
        this.displayName = displayName;
        this.category = category;
        this.priority = priority;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.target = target;
    }

    public String getDisplayName() { return displayName; }
    public QuestCategory getCategory() { return category; }
    public QuestPriorities getPriority() { return priority; }
    public RewardType getRewardType() { return rewardType; }
    public int getRewardAmount() { return rewardAmount; }
    public int getTarget() { return target; }

    public boolean isDaily() { return category == DAILY; }
}

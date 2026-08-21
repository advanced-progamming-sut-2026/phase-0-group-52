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
    DAILY_SUN("Daily Sun Collector", "Collect {t} sun in a single day.",
            DAILY, MEDIUM, COIN, 0, 3000, "FALLFESTIVAL"),
    CHAPTER_HUNTER("Egypt Hunter", "Defeat 50 zombies from {v}.",
            MAIN, HIGH, SEED_PACKET, 10, 50, "EGYPT"),
    CHAPTER_HUNTER_ICEAGE("Frostbite Hunter", "Defeat 50 zombies from {v}.",
            MAIN, HIGH, SEED_PACKET, 10, 50, "FROSTBITECAVES"),
    CHAPTER_HUNTER_BEACH("Big Wave Hunter", "Defeat 50 zombies from {v}.",
            MAIN, HIGH, SEED_PACKET, 10, 50, "BIGWAVEBEACH"),
    CHAPTER_HUNTER_DARK("Dark Ages Hunter", "Defeat 50 zombies from {v}.",
            MAIN, HIGH, SEED_PACKET, 10, 50, "DARKAGES"),
    PLANT_PRO("Professional Plant User", "Kill ten zombies using only {v}.",
            DAILY, HIGH, PLANT_UNLOCK, 1, 10, "PLANT"),
    ONLY_CACTUS("Only Cactus", "Kill ten zombies using only Cactus.",
            DAILY, HIGH, GEM, 20, 10, "WILDWEST"),
    THRIFTY_HERBIVORE("Thrifty Herbivore", "Win a level without losing more than {n} plants.",
            MAIN, HIGH, SEED_PACKET, 20, 1, "PREMIUMSEEDS"),
    DEFENSE_MASTER("Defense Master", "Finish a level with exactly zero sun left.",
            EPIC, CRITICAL, GEM, 200, 1, "KNOCKBACK"),
    QUICK_KILLS("Quick Kills", "Kill 10 zombies within 30 seconds of the first wave.",
            MAIN, MEDIUM, COIN, 500, 10, "ELECTROCUTE"),
    DEMOLITION_PRO("Demolition Pro", "Use 3 explosive plants in a single level.",
            DAILY, LOW, COIN, 100, 3, "LAWNOFDOOM"),
    SYMMETRY("Symmetry", "Win with a perfectly symmetric garden.",
            DAILY, HIGH, COIN, 500, 1, "MODERN"),
    FAMILY_KILL("Family Kill", "Kill zombies using only {v} plants.",
            DAILY, MEDIUM, COIN, 1000, 1, "GARGANTUAR"),
    BLOOM_LIMITS("Bloom in Limits", "Win without planting a single {v} plant.",
            DAILY, HIGH, GEM, 100, 1, "MINTS"),
    NIGHT_OR_DAY("Night or Day", "Finish a daytime level using only night plants.",
            EPIC, HIGH, GEM, 20, 1, "LOTD"),
    WIN_STREAK("Win Streak", "Win 5 levels in a row on the hardest difficulty.",
            DAILY, MEDIUM, COIN, 5000, 5, "ARENA"),
    ALMOST_WON("Almost Won", "Kill 10 zombies in the first column of a row with no mower.",
            DAILY, MEDIUM, COIN, 300, 10, "ZOMBIE"),
    OCD("No OCD", "Win with no symmetry in the garden at all, middle row aside.",
            DAILY, MEDIUM, COIN, 800, 1, "RIFT"),
    CLOUDY_DAY("Cloudy Day", "Win a level using only 3 sun-producing plants.",
            DAILY, HIGH, GEM, 10, 1, "REFRIGERATE"),
    ONE_COLUMN_LESS("One Column Less", "Win without planting anything in column {n}.",
            DAILY, HIGH, GEM, 10, 1, "EXPANSIONLEVEL"),
    DEFENSELESS_ROW("Defenseless Row", "Win without planting anything in row {n}.",
            DAILY, HIGH, GEM, 20, 1, "STUNNED"),
    DEFENSELESS_CROSS("Defenseless Cross", "Win with row {n} and column {n} both empty.",
            DAILY, HIGH, GEM, 25, 1, "JURASSICMARSH"),
    MOWER_TIME("Mower Time", "Kill at least {t} zombies with lawnmowers.",
            EPIC, MEDIUM, GEM, 0, 10, "POWERUPS");

    private final String displayName;
    private final String description;
    private final QuestCategory category;
    private final QuestPriorities priority;
    private final RewardType rewardType;
    private final int rewardAmount;
    private final int target;
    private final String iconName;

    QuestDef(String displayName, String description, QuestCategory category,
             QuestPriorities priority, RewardType rewardType, int rewardAmount,
             int target, String iconName) {
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.rewardType = rewardType;
        this.rewardAmount = rewardAmount;
        this.target = target;
        this.iconName = iconName;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public QuestCategory getCategory() { return category; }
    public QuestPriorities getPriority() { return priority; }
    public RewardType getRewardType() { return rewardType; }
    public int getRewardAmount() { return rewardAmount; }
    public int getTarget() { return target; }
    public String getIconName() { return iconName; }

    public boolean isDaily() { return category == DAILY; }
}

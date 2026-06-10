package pvz.model.quest;

public enum Quests {
    AFTAB_GIRE_ROOZANEH(QuestCategory.DAILY, QuestPriorities.MEDIUM);

    private QuestCategory category;
    private QuestPriorities priority;

    Quests(QuestCategory category, QuestPriorities priority) {
        this.category = category;
        this.priority = priority;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public QuestPriorities getPriority() {
        return priority;
    }
}

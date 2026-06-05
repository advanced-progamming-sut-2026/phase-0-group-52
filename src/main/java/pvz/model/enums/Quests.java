package pvz.model.enums;

import pvz.model.enums.*;

public enum Quests {
    AFTABGIR_ROOZANEH("aftab gir roozaneh",QuestCategory.DAILY,Priority.MEDIUM);

    private String name;
    private QuestCategory category;
    private Priority priority;

    Quests(String name, QuestCategory category, Priority priority) {
        this.name = name;
        this.category = category;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public QuestCategory getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }
}

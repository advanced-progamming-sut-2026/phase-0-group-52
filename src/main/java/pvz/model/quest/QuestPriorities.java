package pvz.model.quest;

public enum QuestPriorities {
    CRITICAL(3), HIGH(2), MEDIUM(1) ,LOW(0);

    private int prioritynum;

    QuestPriorities(int prioritynum) {
        this.prioritynum = prioritynum;
    }

    public int getPrioritynum() {
        return prioritynum;
    }
}

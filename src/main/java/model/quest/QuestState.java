package model.quest;

import java.util.ArrayList;
import java.util.List;

public class QuestState {

    private String username;
    private String lastResetDate;
    private List<QuestProgress> quests = new ArrayList<>();

    public QuestState() {
    }

    public QuestState(String username, String lastResetDate, List<QuestProgress> quests) {
        this.username = username;
        this.lastResetDate = lastResetDate;
        this.quests = (quests != null) ? quests : new ArrayList<>();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLastResetDate() { return lastResetDate; }
    public void setLastResetDate(String lastResetDate) { this.lastResetDate = lastResetDate; }

    public List<QuestProgress> getQuests() { return quests; }
    public void setQuests(List<QuestProgress> quests) { this.quests = quests; }
}

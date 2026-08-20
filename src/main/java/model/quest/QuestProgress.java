package model.quest;

public class QuestProgress {

    private QuestDef def;
    private double progress;
    private int target;
    private boolean completed;
    private boolean claimed;
    private boolean pinned;
    private int varInt;
    private String varStr;

    public QuestProgress() {
    }

    public QuestProgress(QuestDef def) {
        this.def = def;
        this.target = def.getTarget();
    }

    public QuestDef getDef() { return def; }
    public void setDef(QuestDef def) { this.def = def; }

    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }

    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public boolean isClaimed() { return claimed; }
    public void setClaimed(boolean claimed) { this.claimed = claimed; }

    public int getVarInt() { return varInt; }
    public void setVarInt(int varInt) { this.varInt = varInt; }

    public String getVarStr() { return varStr; }
    public void setVarStr(String varStr) { this.varStr = varStr; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
}

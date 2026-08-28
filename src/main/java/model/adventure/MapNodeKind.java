package model.adventure;

public enum MapNodeKind {
    LEVEL, SPECIAL, ZOMBOSS, PLANT, TROPHY;

    public boolean isPlayable() {
        return this == LEVEL || this == SPECIAL || this == ZOMBOSS;
    }
}

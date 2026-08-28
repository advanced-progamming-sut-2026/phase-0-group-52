package model.adventure;

import model.level.SpecialLevel;

public final class MapNode {

    private final MapNodeKind kind;
    private final int index;
    private final int levelNumber;
    private final int slot;
    private final SpecialLevel special;

    private MapNode(MapNodeKind kind, int index, int levelNumber, int slot, SpecialLevel special) {
        this.kind = kind;
        this.index = index;
        this.levelNumber = levelNumber;
        this.slot = slot;
        this.special = special;
    }

    static MapNode level(int index, int levelNumber) {
        return new MapNode(MapNodeKind.LEVEL, index, levelNumber, -1, null);
    }

    static MapNode special(int index, int levelNumber, SpecialLevel type) {
        return new MapNode(MapNodeKind.SPECIAL, index, levelNumber, -1, type);
    }

    static MapNode zomboss(int index, int levelNumber) {
        return new MapNode(MapNodeKind.ZOMBOSS, index, levelNumber, -1, null);
    }

    static MapNode plant(int index, int slot) {
        return new MapNode(MapNodeKind.PLANT, index, 0, slot, null);
    }

    static MapNode trophy(int index) {
        return new MapNode(MapNodeKind.TROPHY, index, 0, -1, null);
    }

    public MapNodeKind getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public int getSlot() {
        return slot;
    }

    public SpecialLevel getSpecial() {
        return special;
    }

    public String getLabel() {
        if (kind == MapNodeKind.SPECIAL && special != null) {
            return special.getDisplayName();
        }
        if (kind == MapNodeKind.ZOMBOSS) {
            return "Zomboss";
        }
        return "";
    }
}

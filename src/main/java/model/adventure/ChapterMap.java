package model.adventure;

import model.ChapterType;
import model.level.SpecialLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ChapterMap {

    public static final int PLANT_ISLANDS = 10;

    private static final int[] SEGMENTS = {3, 3, 4};

    private static final Map<ChapterType, List<MapNode>> CACHE =
            new EnumMap<ChapterType, List<MapNode>>(ChapterType.class);

    private ChapterMap() {}

    public static List<MapNode> of(ChapterType chapter) {
        if (chapter == null) {
            return Collections.emptyList();
        }
        List<MapNode> cached = CACHE.get(chapter);
        if (cached == null) {
            cached = build(chapter);
            CACHE.put(chapter, cached);
        }
        return cached;
    }

    public static int levelRequiredFor(int slot) {
        int seen = 0;
        for (int segment = 0; segment < SEGMENTS.length; segment++) {
            seen += SEGMENTS[segment];
            if (slot < seen) {
                return segment + 1;
            }
        }
        return SEGMENTS.length;
    }

    public static int slotsUnlockedBy(int clearedLevels) {
        int total = 0;
        for (int segment = 0; segment < SEGMENTS.length && segment < clearedLevels; segment++) {
            total += SEGMENTS[segment];
        }
        return total;
    }

    public static boolean isSlotOpen(int clearedLevels, int slot) {
        return slot >= 0 && slot < PLANT_ISLANDS && clearedLevels >= levelRequiredFor(slot);
    }

    public static boolean isLevelPlayable(int clearedLevels, int levelNumber) {
        return levelNumber >= 1 && levelNumber <= ChapterType.LEVELS_PER_CHAPTER
                && levelNumber <= clearedLevels + 1;
    }

    public static int clearedLevels(int lastChapter, int lastLevel, ChapterType chapter) {
        if (chapter == null) {
            return 0;
        }
        int reachedChapter = Math.max(1, lastChapter);
        int reachedLevel = Math.max(1, lastLevel);
        int index = chapter.number();
        if (index < reachedChapter) {
            return ChapterType.LEVELS_PER_CHAPTER;
        }
        if (index > reachedChapter) {
            return 0;
        }
        return Math.min(ChapterType.LEVELS_PER_CHAPTER, reachedLevel - 1);
    }

    private static List<MapNode> build(ChapterType chapter) {
        SpecialLevel[] specials = SpecialLevel.of(chapter);
        List<MapNode> nodes = new ArrayList<MapNode>();
        int slot = 0;
        nodes.add(MapNode.level(nodes.size(), 1));
        for (int segment = 0; segment < SEGMENTS.length; segment++) {
            for (int i = 0; i < SEGMENTS[segment]; i++) {
                nodes.add(MapNode.plant(nodes.size(), slot++));
            }
            int levelNumber = segment + 2;
            if (levelNumber < ChapterType.LEVELS_PER_CHAPTER && segment < specials.length) {
                nodes.add(MapNode.special(nodes.size(), levelNumber, specials[segment]));
            }
        }
        nodes.add(MapNode.zomboss(nodes.size(), ChapterType.LEVELS_PER_CHAPTER));
        nodes.add(MapNode.trophy(nodes.size()));
        return Collections.unmodifiableList(nodes);
    }
}

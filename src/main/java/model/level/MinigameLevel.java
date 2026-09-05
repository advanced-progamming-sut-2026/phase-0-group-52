package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;

public abstract class MinigameLevel extends Level {

    private final MinigameType kind;

    public MinigameLevel(MinigameType kind, int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants) {
        super(levelnumber, chaptertype, allowedplants, null);
        this.kind = kind;
    }

    public MinigameType getKind() {
        return kind;
    }

    public boolean usesChapterMechanics() {
        return true;
    }

    @Override
    public String objective() {
        return kind.getBlurb();
    }

    @Override
    public String objectiveTag() {
        return kind.getTag();
    }
}

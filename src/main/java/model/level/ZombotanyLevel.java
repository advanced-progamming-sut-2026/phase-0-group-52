package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class ZombotanyLevel extends MinigameLevel {

    public ZombotanyLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants) {
        super(MinigameType.ZOMBOTANY, levelnumber, chaptertype, allowedplants);
    }
}

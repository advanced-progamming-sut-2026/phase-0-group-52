package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.entities.plants.Plants;

import java.util.ArrayList;

public abstract class Level extends AttackPattern {
    private int levelnumber;
    private ChapterType chaptertype;
    private ArrayList<Plants> allowedplants;
    private AttackPattern attackPattern;

    public Level(int levelnumber, ChapterType chaptertype, ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        this.levelnumber = levelnumber;
        this.chaptertype = chaptertype;
        this.allowedplants = allowedplants;
        this.attackPattern = attackPattern;
    }
}

package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.Plants;

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

    public int getLevelnumber() { return levelnumber; }
    public ChapterType getChaptertype() { return chaptertype; }
    public ArrayList<Plants> getAllowedplants() { return allowedplants; }
    public AttackPattern getAttackPattern() { return attackPattern; }

    public boolean isSkySunEnabled() { return true; }

    public boolean isPlantAllowed(Plants type) {
        return allowedplants == null || allowedplants.isEmpty() || allowedplants.contains(type);
    }

    public void onTick(Game game) {}

    public void onWaveStart(Game game) {}

    public String checkDefeat(Game game) { return null; }

    public String checkVictory(Game game) { return null; }
}

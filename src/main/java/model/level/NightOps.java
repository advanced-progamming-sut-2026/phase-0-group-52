package model.level;

import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class NightOps extends Level {

    public NightOps(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    @Override
    public String objective() {
        return "No sun falls from the sky - survive on what you grow.";
    }

    @Override
    public String objectiveTag() {
        return "NIGHT OPS";
    }

    @Override
    public boolean isSkySunEnabled() { return false; }
}

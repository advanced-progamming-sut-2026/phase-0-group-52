package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.entities.plants.Plants;

import java.util.ArrayList;

public class NightOps extends Level {

    public NightOps(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    @Override
    public boolean isSkySunEnabled() { return false; }
}

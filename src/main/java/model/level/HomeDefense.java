package model.level;

import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class HomeDefense extends Level {

    public HomeDefense(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }
}

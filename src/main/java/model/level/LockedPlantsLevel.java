package model.level;

import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LockedPlantsLevel extends Level {

    private final Set<Plants> lockedPlants = new HashSet<Plants>();

    public LockedPlantsLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public static Set<Plants> defaultLocked() {
        return new java.util.LinkedHashSet<Plants>(java.util.Arrays.asList(
                Plants.CHERRY_BOMB, Plants.JALAPENO, Plants.SQUASH,
                Plants.REPEATER, Plants.WINTER_MELON));
    }

    public Set<Plants> getLockedPlants() { return lockedPlants; }

    public void lockPlant(Plants plant) { lockedPlants.add(plant); }

    @Override
    public boolean isPlantAllowed(Plants type) {
        return !lockedPlants.contains(type) && super.isPlantAllowed(type);
    }
}

package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.Plant;
import model.entities.plants.Plants;

import java.util.ArrayList;
import java.util.List;

public class SaveOurSeeds extends Level {

    private final List<Plant> protectedPlants = new ArrayList<Plant>();

    public SaveOurSeeds(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public List<Plant> getProtectedPlants() { return protectedPlants; }

    public void protectPlant(Plant plant) { protectedPlants.add(plant); }

    @Override
    public String objective() {
        return "Keep every endangered plant alive.";
    }

    @Override
    public String objectiveTag() {
        return "SAVE OUR SEEDS";
    }

    @Override
    public String checkDefeat(Game game) {
        for (Plant p : protectedPlants)
            if (p.isDead() || !game.getPlants().contains(p))
                return "A protected plant was lost. You lose!";
        return null;
    }
}

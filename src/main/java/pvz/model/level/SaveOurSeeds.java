package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.entities.plants.Plant;
import pvz.model.entities.plants.Plants;

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
    public String checkDefeat(Game game) {
        for (Plant p : protectedPlants)
            if (p.isDead() || !game.getPlants().contains(p))
                return "A protected plant was lost. You lose!";
        return null;
    }
}

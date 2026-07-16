package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.Game;
import pvz.model.entities.plants.Plants;

import java.util.ArrayList;

public class LoveYourPlants extends Level {

    private int maxLostPlants = 5;
    private int lostPlants = 0;

    public LoveYourPlants(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public int getMaxLostPlants() { return maxLostPlants; }
    public void setMaxLostPlants(int maxLostPlants) { this.maxLostPlants = maxLostPlants; }
    public int getLostPlants() { return lostPlants; }

    public void onPlantLost() { lostPlants++; }

    @Override
    public String checkDefeat(Game game) {
        if (lostPlants >= maxLostPlants)
            return "You lost " + lostPlants + " plants. You lose!";
        return null;
    }
}

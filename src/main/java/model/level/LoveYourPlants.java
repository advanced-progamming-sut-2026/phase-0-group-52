package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.Plants;

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
    public String objective() {
        return "Lose no more than " + maxLostPlants + " plants.";
    }

    @Override
    public String objectiveTag() {
        return "LOVE YOUR PLANTS";
    }

    @Override
    public String checkDefeat(Game game) {
        if (lostPlants >= maxLostPlants)
            return "You lost " + lostPlants + " plants. You lose!";
        return null;
    }
}

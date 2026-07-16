package pvz.model.level;

import pvz.model.ChapterType;
import pvz.model.entities.plants.Plants;
import pvz.model.entities.plants.PlantsCategory;

import java.util.ArrayList;

public class PlantWhatYouGet extends Level {

    private int startingSun = 500;
    private boolean wavesStarted = false;

    public PlantWhatYouGet(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public int getStartingSun() { return startingSun; }
    public void setStartingSun(int startingSun) { this.startingSun = startingSun; }

    public boolean isWavesStarted() { return wavesStarted; }
    public void startWaves() { wavesStarted = true; }

    @Override
    public boolean isSkySunEnabled() { return false; }

    @Override
    public boolean isPlantAllowed(Plants type) {
        return type.getCategory() != PlantsCategory.SUN_PRODUCER && super.isPlantAllowed(type);
    }
}

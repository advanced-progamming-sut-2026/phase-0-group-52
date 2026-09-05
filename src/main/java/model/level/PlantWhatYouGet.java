package model.level;

import model.ChapterType;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class PlantWhatYouGet extends ConveyorBeltLevel {

    private boolean wavesStarted = false;
    private int waveStartTick = 0;

    public PlantWhatYouGet(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public boolean isWavesStarted() { return wavesStarted; }

    public void startWaves(int tick) {
        wavesStarted = true;
        waveStartTick = tick;
    }

    public int getWaveStartTick() { return waveStartTick; }

    @Override
    public String objective() {
        return "Plant everything the belt gives you, then start the waves.";
    }

    @Override
    public String objectiveTag() {
        return "PLANT WHAT YOU GET";
    }

    @Override
    public boolean areWavesHeld() { return !wavesStarted; }

    @Override
    public boolean manualWaves() { return true; }

    @Override
    public boolean isSkySunEnabled() { return false; }
}

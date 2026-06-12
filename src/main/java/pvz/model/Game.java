package pvz.model;

import pvz.model.entities.plants.Plant;
import pvz.model.level.Level;

import java.util.ArrayList;

public class Game {
    private App app;
    private Chapter chapter;
    private Level level;
    private GameField field;
    private int sunamount;
    private ArrayList<Plant> plants;
    private ArrayList<Wave> waves;

    public Game(App app, Chapter chapter, Level level, GameField field, int sunamount,
                ArrayList<Plant> plants, ArrayList<Wave> waves, int time) {
        this.app = app;
        this.chapter = chapter;
        this.level = level;
        this.field = field;
        this.sunamount = sunamount;
        this.plants = plants;
        this.waves = waves;
    }

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public GameField getField() {
        return field;
    }

    public void setField(GameField field) {
        this.field = field;
    }

    public int getSunamount() {
        return sunamount;
    }

    public void setSunamount(int sunamount) {
        this.sunamount = sunamount;
    }

    public ArrayList<Plant> getPlants() {
        return plants;
    }

    public void setPlants(ArrayList<Plant> plants) {
        this.plants = plants;
    }

    public ArrayList<Wave> getWaves() {
        return waves;
    }

    public void setWaves(ArrayList<Wave> waves) {
        this.waves = waves;
    }
}

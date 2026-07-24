package model.level;

import model.ChapterType;
import model.Game;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class TimedWar extends Level {

    private double duration = 60;
    private int targetKills = 0;
    private int targetSun = 0;
    private double elapsed = 0;
    private int kills = 0;

    public TimedWar(int levelnumber, ChapterType chaptertype,
                    ArrayList<Plants> allowedplants, AttackPattern attackPattern) {
        super(levelnumber, chaptertype, allowedplants, attackPattern);
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setTargetKills(int targetKills) {
        this.targetKills = targetKills;
    }

    public void setTargetSun(int targetSun) {
        this.targetSun = targetSun;
    }

    public double getRemainingTime() {
        return Math.max(0, duration - elapsed);
    }

    public int getKills() {
        return kills;
    }

    public void onZombieKilled() {
        kills++;
    }

    @Override
    public void onTick(Game game) {
        elapsed += 1;
    }

    @Override
    public String checkVictory(Game game) {
        if (targetKills > 0 && kills >= targetKills)
            return "You destroyed " + kills + " zombies in time. You win!";
        if (targetSun > 0 && game.getSunAmount() >= targetSun)
            return "You produced " + game.getSunAmount() + " sun in time. You win!";
        return null;
    }

    @Override
    public String checkDefeat(Game game) {
        if (elapsed >= duration && checkVictory(game) == null)
            return "Time is up. You lose!";
        return null;
    }
}

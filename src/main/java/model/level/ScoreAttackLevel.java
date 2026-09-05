package model.level;

import minigame.MinigameType;
import model.ChapterType;
import model.Game;
import model.entities.plants.Plants;

import java.util.ArrayList;

public class ScoreAttackLevel extends MinigameLevel {

    public static final int POINTS_PER_KILL = 25;
    public static final int POINTS_PER_SUN = 1;
    public static final int SURVIVAL_BONUS = 5;
    public static final int SURVIVAL_EVERY = 50;

    private final int targetScore;

    private int score;
    private int countedKills;
    private int countedSun;

    public ScoreAttackLevel(int levelnumber, ChapterType chaptertype,
            ArrayList<Plants> allowedplants, int targetScore) {
        super(MinigameType.SCORE, levelnumber, chaptertype, allowedplants);
        this.targetScore = targetScore;
    }

    public int getScore() {
        return score;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public float progress() {
        return targetScore <= 0 ? 0f
                : Math.min(1f, score / (float) targetScore);
    }

    @Override
    public String objective() {
        return "Score " + targetScore + " points before they reach the house.";
    }

    @Override
    public void onTick(Game game) {
        int kills = game.getStats().getZombiesKilled();
        if (kills > countedKills) {
            score += (kills - countedKills) * POINTS_PER_KILL;
            countedKills = kills;
        }
        int sun = game.getStats().getSunCollected();
        if (sun > countedSun) {
            score += (sun - countedSun) / 25 * POINTS_PER_SUN;
            countedSun = sun;
        }
        if (game.getCurrentTick() % SURVIVAL_EVERY == 0) {
            score += SURVIVAL_BONUS;
        }
    }

    @Override
    public String checkVictory(Game game) {
        return score >= targetScore
                ? "You scored " + score + " points. You win!" : null;
    }
}

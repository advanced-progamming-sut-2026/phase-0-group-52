package pvz.model.mechanics;

import pvz.model.User;

public class MeowPointTracker {

    public static final int MULTI_KILL_BONUS = 100;
    public static final int FAST_KILL_BONUS = 50;
    public static final int SIMULTANEOUS_KILL_BONUS = 75;
    public static final int FAST_WAVE_BONUS = 200;
    public static final int PERFECT_DEFENSE_BONUS = 500;

    private int points;

    public void onMultiKillWithOneShot(int zombiesKilled) {
        if (zombiesKilled > 1) points += MULTI_KILL_BONUS * (zombiesKilled - 1);
    }

    public void onFastKill() {
        points += FAST_KILL_BONUS;
    }

    public void onSimultaneousKills(int zombiesKilled) {
        if (zombiesKilled > 1) points += SIMULTANEOUS_KILL_BONUS * (zombiesKilled - 1);
    }

    public void onWaveClearedQuickly() {
        points += FAST_WAVE_BONUS;
    }

    public void onPerfectDefense() {
        points += PERFECT_DEFENSE_BONUS;
    }

    public int getPoints() {
        return points;
    }

    public void applyTo(User user) {
        if (points > user.getMaxPoint()) user.setMaxPoint(points);
        if (points > user.getMostMeowPoint()) user.setMostMeowPoint(points);
    }
}

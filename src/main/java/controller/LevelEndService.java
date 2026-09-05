package controller;

import model.App;
import model.ChapterType;
import model.Game;
import model.GameStats;
import model.User;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;
import model.mechanics.MeowPointTracker;

import java.util.HashMap;
import java.util.Map;

public final class LevelEndService {

    public static final int WIN_COINS = 500;
    public static final int SCORE_PER_KILL = 10;
    public static final int FAST_KILL_TICKS = 300;
    public static final int QUICK_CLEAR_TICKS = 600;

    private final QuestService quests = new QuestService();
    private final SaveService saves = new SaveService();

    public void finish(App app, Game game) {
        if (app == null || game == null) {
            return;
        }
        User user = app.getCurrentuser();
        if (user != null && game.isWon()) {
            reward(app, user, game);
        }
        if (user != null) {
            awardMeowPoints(game, user);
        }
        quests.onLevelEnd(game, game.isWon());
        if (user != null) {
            saves.persist(user);
        }
    }

    public void clearDeck(App app) {
        if (app == null) {
            return;
        }
        app.getPlantSelection().clear();
        app.getBoostedSelection().clear();
        app.setImitatedPlant(null);
        app.setAwaitingImitate(false);
        app.setPendingSpecial(null);
        app.getLockedPlants().clear();
    }

    private void reward(App app, User user, Game game) {
        user.setCoins(user.getCoins() + WIN_COINS);
        recordZombiesMet(user, game);
        int score = game.getStats().getZombiesKilled() * SCORE_PER_KILL + game.getSunAmount();
        if (score > user.getMaxPoint()) {
            user.setMaxPoint(score);
        }
        int cleared = game.getLevel() != null
                ? game.getLevel().getLevelnumber() : app.getSelectedLevel();
        ChapterType playedIn = game.getLevel() != null
                ? game.getLevel().getChaptertype() : app.getSelectedChapter();
        recordAdventure(user, playedIn, cleared);
        advancePointer(user, cleared);
    }

    private void recordAdventure(User user, ChapterType playedIn, int cleared) {
        if (playedIn == null) {
            return;
        }
        user.getAdventure().openChapter(playedIn);
        user.getAdventure().recordCleared(playedIn, cleared);
        if (cleared >= ChapterType.LEVELS_PER_CHAPTER) {
            ChapterType next = ChapterType.byNumber(playedIn.number() + 1);
            if (next != null) {
                user.getAdventure().openChapter(next);
            }
        }
    }

    private void advancePointer(User user, int cleared) {
        if (cleared < user.getLastLevel()) {
            return;
        }
        if (cleared >= ChapterType.LEVELS_PER_CHAPTER) {
            int nextChapter = Math.min(ChapterType.values().length,
                    Math.max(1, user.getLastChapter()) + 1);
            user.setLastChapter(nextChapter);
            user.setLastLevel(1);
            user.getNewsList().addNews("Chapter cleared! A new world is open.");
            return;
        }
        user.setLastLevel(cleared + 1);
        user.getNewsList().addNews("New level unlocked: level " + (cleared + 1)
                + "! A tougher wave awaits.");
    }

    private void recordZombiesMet(User user, Game game) {
        for (String alias : game.getStats().getKilledZombies()) {
            ZombieRecord record = ZombieData.byAlias(alias);
            if (record != null && user.markZombieSeen(alias)) {
                user.getNewsList().addNews(
                        "A new zombie joined your almanac: " + record.getName() + "!");
            }
        }
    }

    private void awardMeowPoints(Game game, User user) {
        MeowPointTracker meow = new MeowPointTracker();
        GameStats stats = game.getStats();
        Map<Integer, Integer> perTick = new HashMap<Integer, Integer>();
        for (int tick : stats.getKillTicks()) {
            Integer count = perTick.get(tick);
            perTick.put(tick, count == null ? 1 : count + 1);
        }
        for (int count : perTick.values()) {
            if (count > 1) {
                meow.onSimultaneousKills(count);
            }
        }
        int fast = stats.killsWithinTicksOfFirstWave(FAST_KILL_TICKS);
        for (int i = 0; i < fast; i++) {
            meow.onFastKill();
        }
        if (game.isWon()) {
            if (stats.getPlantsLost() == 0) {
                meow.onPerfectDefense();
            }
            if (stats.getFirstWaveTick() >= 0
                    && game.getCurrentTick() - stats.getFirstWaveTick() <= QUICK_CLEAR_TICKS) {
                meow.onWaveClearedQuickly();
            }
        }
        meow.applyTo(user);
    }
}

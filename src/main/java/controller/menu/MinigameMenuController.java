package controller.menu;

import minigame.MinigameType;
import model.App;
import model.Result;
import model.User;
import model.enums.MenuType;

import java.util.ArrayList;
import java.util.List;

public class MinigameMenuController {

    private final App app;

    public MinigameMenuController(App app) {
        this.app = app;
    }

    public List<MinigameType> catalogue() {
        List<MinigameType> out = new ArrayList<MinigameType>();
        for (MinigameType type : MinigameType.values()) {
            out.add(type);
        }
        return out;
    }

    public boolean isUnlocked(MinigameType type) {
        if (type == null || !type.isPlayable()) {
            return false;
        }
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return false;
        }
        return user.isDebugMode() || clearedLevels() >= requiredLevels(type);
    }

    public int requiredLevels(MinigameType type) {
        switch (type) {
            case VASE_BREAKER:    return 0;
            case WALLNUT_BOWLING: return 1;
            case ZOMBOTANY:       return 2;
            case BEGHOULED:       return 3;
            case I_ZOMBIE:        return 4;
            case SCORE:           return 5;
            default:              return Integer.MAX_VALUE;
        }
    }

    public String lockReason(MinigameType type) {
        if (type == null) {
            return "Unknown minigame.";
        }
        if (!type.isPlayable()) {
            return type.getDisplayName() + " arrives in phase 3.";
        }
        if (app == null || app.getCurrentuser() == null) {
            return "Sign in to play the minigames.";
        }
        int needed = requiredLevels(type);
        return "Clear " + needed + " adventure level"
                + (needed == 1 ? "" : "s") + " to unlock " + type.getDisplayName() + ".";
    }

    public int clearedLevels() {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return 0;
        }
        int total = 0;
        for (model.ChapterType chapter : model.ChapterType.values()) {
            total += user.getAdventure().clearedLevels(chapter);
        }
        return total;
    }

    public int bestScore(MinigameType type) {
        User user = app == null ? null : app.getCurrentuser();
        return user == null || type == null ? 0 : user.getMinigameBest(type.name());
    }

    public void recordScore(MinigameType type, int score) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null || type == null || score <= 0) {
            return;
        }
        if (user.recordMinigameBest(type.name(), score)) {
            user.getNewsList().addNews("New " + type.getDisplayName()
                    + " best: " + score + "!");
        }
        new controller.SaveService().persist(user);
    }

    public Result open(MinigameType type) {
        if (type == null) {
            return new Result(false, "Unknown minigame.", null);
        }
        if (!isUnlocked(type)) {
            return new Result(false, lockReason(type), null);
        }
        User user = app.getCurrentuser();
        if (user != null && user.markMinigameUnlocked(type.name())) {
            user.getNewsList().addNews("New minigame unlocked: "
                    + type.getDisplayName() + "!");
        }
        app.getPlantSelection().clear();
        app.getBoostedSelection().clear();
        app.getLockedPlants().clear();
        app.setPendingSpecial(null);
        app.setPendingMinigame(type.name());
        app.setCurrentmenu(type == MinigameType.BEGHOULED
                ? MenuType.BEGHOULED_MENU : MenuType.MINIGAME_MENU);
        return new Result(true, type.getDisplayName() + " starting.", type);
    }
}

package controller.menu;

import controller.Navigation;
import controller.SaveService;
import model.App;
import model.ChapterType;
import model.Result;
import model.User;
import model.adventure.AdventureProgress;
import model.adventure.ChapterMap;
import model.entities.plants.Plants;
import model.enums.MenuType;
import model.level.LockedPlantsLevel;
import model.level.SpecialLevel;

import java.util.Random;

public class AdventureMenuController {

    private static final Random RANDOM = new Random();

    private final App app;
    private final SaveService saves = new SaveService();

    public AdventureMenuController(App app) {
        this.app = app;
    }

    public int clearedLevels(ChapterType chapter) {
        User user = app == null ? null : app.getCurrentuser();
        return user == null ? 0
                : ChapterMap.clearedLevels(user.getLastChapter(), user.getLastLevel(), chapter);
    }

    public boolean isChapterUnlocked(ChapterType chapter) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return chapter == ChapterType.first();
        }
        return chapter != null && chapter.ordinal() < Math.max(1, user.getLastChapter());
    }

    public Result claimIsland(ChapterType chapter, int slot) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (chapter == null) {
            return failure("Unknown chapter.");
        }
        AdventureProgress progress = user.getAdventure();
        if (progress.isClaimed(chapter, slot)) {
            return failure("You already opened this one.");
        }
        if (!ChapterMap.isSlotOpen(clearedLevels(chapter), slot)) {
            return failure("Clear level " + ChapterMap.levelRequiredFor(slot)
                    + " to reach this island.");
        }
        Plants prize = progress.nextPrize(user.getPlants(), chapter, RANDOM);
        if (prize == null) {
            return failure("Every plant in this world is already yours.");
        }
        progress.record(chapter, slot, prize);
        user.getPlants().grant(prize, 1);
        user.getNewsList().addNews("A pinata burst open: " + prize.getName()
                + " joined your garden!");
        saves.persist(user);
        return new Result(true, prize.getName() + " is yours!", prize);
    }

    public Result unlockLevel(ChapterType chapter, int levelNumber) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (chapter == null || levelNumber < 1
                || levelNumber > ChapterType.LEVELS_PER_CHAPTER) {
            return failure("Unknown level.");
        }
        if (ChapterMap.isLevelPlayable(clearedLevels(chapter), levelNumber)) {
            return failure("That level is already open.");
        }
        if (chapter.number() > Math.max(1, user.getLastChapter())) {
            user.setLastChapter(chapter.number());
            user.setLastLevel(1);
        }
        if (levelNumber > Math.max(1, user.getLastLevel())) {
            user.setLastLevel(levelNumber);
        }
        saves.persist(user);
        return new Result(true, "Level " + levelNumber + " forced open.", chapter);
    }

    public Result completeLevel(ChapterType chapter, int levelNumber) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (chapter == null || levelNumber < 1
                || levelNumber > ChapterType.LEVELS_PER_CHAPTER) {
            return failure("Unknown level.");
        }
        if (levelNumber <= clearedLevels(chapter)) {
            return failure("That level is already cleared.");
        }
        if (chapter.number() > Math.max(1, user.getLastChapter())) {
            user.setLastChapter(chapter.number());
        }
        if (levelNumber >= ChapterType.LEVELS_PER_CHAPTER) {
            user.setLastChapter(Math.min(ChapterType.values().length,
                    chapter.number() + 1));
            user.setLastLevel(1);
        } else {
            user.setLastLevel(levelNumber + 1);
        }
        saves.persist(user);
        return new Result(true, "Level " + levelNumber + " marked cleared.", chapter);
    }

    public Result openLevel(ChapterType chapter, int levelNumber, SpecialLevel special) {
        User user = app == null ? null : app.getCurrentuser();
        if (user == null) {
            return failure("No user is signed in.");
        }
        if (chapter == null) {
            return failure("Unknown chapter.");
        }
        if (!isChapterUnlocked(chapter)) {
            return failure("Finish the previous world first.");
        }
        if (!ChapterMap.isLevelPlayable(clearedLevels(chapter), levelNumber)) {
            return failure("Clear the level before it to unlock this one.");
        }
        app.setSelectedChapter(chapter);
        app.setSelectedLevel(levelNumber);
        app.getPlantSelection().clear();
        app.getBoostedSelection().clear();
        app.getLockedPlants().clear();
        app.setPendingSpecial(special == null ? null : special.getKey());
        if (special == SpecialLevel.LOCKED_PLANTS) {
            app.getLockedPlants().addAll(LockedPlantsLevel.defaultLocked());
        }
        Navigation.go(app, MenuType.CHOOSE_PLANT_MENU);
        return new Result(true, "Pick your deck for level " + levelNumber + ".", chapter);
    }

    private Result failure(String message) {
        return new Result(false, message, null);
    }
}

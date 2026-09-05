package view.gui;

import com.badlogic.gdx.Gdx;
import controller.Navigation;
import util.Log;
import model.enums.MenuType;
import view.gui.screens.TitleScreen;

final class ScreenTour {
    private static final int SETTLE_FRAMES = 24;

    private final PvzGame game;
    private final MenuType[] route = {
            MenuType.SIGNUP_MENU,
            MenuType.LOGIN_MENU,
            MenuType.MAIN_MENU,
            MenuType.PROFILE_MEMU,
            MenuType.SETTINGS_MENU,
            MenuType.NEWS_MENU,
            MenuType.CHAPTER_MENU,
            MenuType.COLLECTION_MENU,
            MenuType.CHOOSE_PLANT_MENU,
            MenuType.GREENHOUSE_MENU,
            MenuType.TRAVEL_LOG_MENU,
    };

    private int index = -1;
    private int frames;
    private boolean finished;

    ScreenTour(PvzGame game) {
        this.game = game;
        Log.info("tour", "Screen tour starting");
    }

    void step() {
        if (finished) {
            return;
        }
        if (++frames < SETTLE_FRAMES) {
            return;
        }
        frames = 0;

        if (index >= 0 && index < route.length) {
            Screenshots.capture("screenshots/tour-" + (index + 1) + "-"
                    + route[index].name().toLowerCase() + ".png");
        }

        index++;
        if (index >= route.length) {
            captureExtras();
            return;
        }
        Navigation.go(game.context().app(), route[index]);
    }

    private void captureExtras() {
        finished = true;
        captureCheatBar();
        captureLevelIntro();
        captureLevel();
        captureWorldMap();
        captureBackNavigation();
        captureOverlays();
        captureTitleAndPopups();
        game.showLeaderboard();
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sleep(700);
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Screenshots.capture("screenshots/tour-12-leaderboard.png");
                                game.showShop();
                            }
                        });
                        sleep(700);
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                Screenshots.capture("screenshots/tour-13-shop.png");
                                captureSignedOut();
                                Log.info("tour", "Screen tour complete");
                                Gdx.app.exit();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void captureSignedOut() {
        new controller.menu.PlayerListController(game.context().app()).signOut();
        game.navigator().goMenu(model.enums.MenuType.COLLECTION_MENU);
        pump(6);
        Screenshots.capture("screenshots/tour-27-signed-out-topbar.png");
    }

    private void captureBackNavigation() {
        Navigator nav = game.navigator();
        nav.goTitle();
        pump(4);

        com.badlogic.gdx.Screen screen = game.getScreen();
        if (screen instanceof Navigator.Hosted) {
            new PlayerListPopup(game.context()).showOn(((Navigator.Hosted) screen).uiStage());
        }
        pump(4);

        nav.goLeaderboard();
        pump(4);
        Screenshots.capture("screenshots/tour-25-leaderboard-from-popup.png");

        nav.back();
        pump(8);
        Screenshots.capture("screenshots/tour-26-back-restores-popup.png");
    }

    private void captureLevelIntro() {
        game.navigator().goMenu(MenuType.CHOOSE_PLANT_MENU);
        pump(6);
        com.badlogic.gdx.Screen screen = game.getScreen();
        if (!(screen instanceof view.gui.screens.ChoosePlantScreen)) {
            return;
        }
        view.gui.screens.ChoosePlantScreen picker =
                (view.gui.screens.ChoosePlantScreen) screen;
        picker.show();
        picker.poseIntro(1f);
        pump(2);
        Screenshots.capture("screenshots/tour-33-level-intro.png");
        picker.poseReady();
        pump(2);
        Screenshots.capture("screenshots/tour-34-choose-plants.png");
    }

    private void captureLevel() {
        model.App app = game.context().app();
        app.setSelectedChapter(model.ChapterType.ANCIENT_EGYPT);
        app.setSelectedLevel(1);
        app.getPlantSelection().clear();
        app.getPlantSelection().add(model.entities.plants.Plants.PEASHOOTER);
        app.getPlantSelection().add(model.entities.plants.Plants.SUNFLOWER);
        app.getPlantSelection().add(model.entities.plants.Plants.WALL_NUT);
        if (!new controller.menu.LevelController(app).start().isSuccess()) {
            return;
        }
        game.render();
        com.badlogic.gdx.Screen screen = game.getScreen();
        if (!(screen instanceof view.gui.screens.LevelScreen)) {
            return;
        }
        view.gui.screens.LevelScreen level = (view.gui.screens.LevelScreen) screen;
        controller.menu.LevelController runner = new controller.menu.LevelController(app);
        app.getGame().setSunAmount(500);
        runner.plant(model.entities.plants.Plants.SUNFLOWER, 0, 0);
        runner.plant(model.entities.plants.Plants.PEASHOOTER, 4, 2);
        runner.plant(model.entities.plants.Plants.WALL_NUT, 8, 4);
        level.arm(model.entities.plants.Plants.PEASHOOTER);
        level.setGrid(true);
        pump(20);
        Screenshots.capture("screenshots/tour-35-level.png");
        app.setGame(null);
        captureSpecial(model.level.SpecialLevel.CONVEYOR,
                model.ChapterType.FROSTBITE_CAVES, "36-conveyor");
        captureSpecial(model.level.SpecialLevel.DEADLINE,
                model.ChapterType.BIG_WAVE_BEACH, "37-deadline");
        captureSpecial(model.level.SpecialLevel.TIMED_WAR,
                model.ChapterType.FROSTBITE_CAVES, "38-timed-war");
        captureSpecial(model.level.SpecialLevel.PLANT_WHAT_YOU_GET,
                model.ChapterType.DARK_AGES, "39-plant-what-you-get");
        captureMinigames();
        captureBosses();
    }

    private void captureBosses() {
        captureBoss(model.ChapterType.ANCIENT_EGYPT, "50-boss-egypt");
        captureBoss(model.ChapterType.FROSTBITE_CAVES, "51-boss-frostbite");
        captureBoss(model.ChapterType.DARK_AGES, "52-boss-dark");
        captureBoss(model.ChapterType.BIG_WAVE_BEACH, "53-boss-beach");
    }

    private void captureBoss(model.ChapterType chapter, String name) {
        model.App app = game.context().app();
        app.setGame(null);
        game.navigator().goMenu(MenuType.MAIN_MENU);
        pump(2);
        app.setSelectedChapter(chapter);
        app.setSelectedLevel(model.ChapterType.LEVELS_PER_CHAPTER);
        app.getPlantSelection().clear();
        app.setPendingSpecial(null);
        if (!new controller.menu.LevelController(app).start().isSuccess()) {
            return;
        }
        game.navigator().goMenu(MenuType.GAME_MENU);
        pump(4);
        if (!(game.getScreen() instanceof view.gui.screens.LevelScreen)) {
            app.setGame(null);
            return;
        }
        ((view.gui.screens.LevelScreen) game.getScreen()).show();
        controller.menu.LevelController runner = new controller.menu.LevelController(app);
        for (int tick = 0; tick < 200; tick++) {
            runner.tick((float) model.Game.SECONDS_PER_TICK);
        }
        pump(20);
        Screenshots.capture("screenshots/tour-" + name + ".png");
        view.gui.widgets.BossDialogue.play(
                ((view.gui.screens.LevelScreen) game.getScreen()).uiStage(),
                game.context(), "Dr. Zomboss",
                new model.level.ZombossLevel(4, chapter).partingLines(true), null);
        pump(1);
        Screenshots.capture("screenshots/tour-54-" + name + "-dialogue.png");
        app.setGame(null);
    }

    private void captureMinigames() {
        model.App app = game.context().app();
        model.User user = app.getCurrentuser();
        boolean cheat = user != null && user.isDebugMode();
        if (user != null) {
            user.setDebugMode(true);
        }
        game.navigator().goMenu(MenuType.MAIN_MENU);
        pump(8);
        Screenshots.capture("screenshots/tour-40-minigame-panel.png");
        if (game.getScreen() instanceof view.gui.screens.MainMenuScreen) {
            ((view.gui.screens.MainMenuScreen) game.getScreen()).scrollMinigames(1f);
            pump(4);
            Screenshots.capture("screenshots/tour-40-minigame-panel-end.png");
        }
        captureMinigame(minigame.MinigameType.VASE_BREAKER, "41-vasebreaker");
        captureMinigame(minigame.MinigameType.WALLNUT_BOWLING, "42-bowling");
        captureMinigame(minigame.MinigameType.I_ZOMBIE, "43-izombie");
        captureMinigame(minigame.MinigameType.ZOMBOTANY, "44-zombotany");
        captureMinigame(minigame.MinigameType.SCORE, "45-score");
        captureBeghouled();
        if (user != null) {
            user.setDebugMode(cheat);
        }
        game.navigator().goMenu(MenuType.MAIN_MENU);
        pump(4);
    }

    private void captureMinigame(minigame.MinigameType kind, String name) {
        model.App app = game.context().app();
        app.setGame(null);
        game.navigator().goMenu(MenuType.MAIN_MENU);
        pump(2);
        app.setSelectedLevel(1);
        app.getPlantSelection().clear();
        app.setPendingMinigame(kind.name());
        game.navigator().goMenu(MenuType.MINIGAME_MENU);
        pump(4);
        if (!(game.getScreen() instanceof view.gui.screens.MinigameScreen)) {
            app.setPendingMinigame(null);
            return;
        }
        controller.menu.MinigameRunController runner =
                new controller.menu.MinigameRunController(app);
        int ticks = kind == minigame.MinigameType.ZOMBOTANY
                || kind == minigame.MinigameType.SCORE ? 380 : 60;
        for (int tick = 0; tick < ticks; tick++) {
            runner.tick((float) model.Game.SECONDS_PER_TICK);
        }
        pump(20);
        Screenshots.capture("screenshots/tour-" + name + ".png");
        app.setGame(null);
        app.setPendingMinigame(null);
    }

    private void captureBeghouled() {
        model.App app = game.context().app();
        app.setGame(null);
        game.navigator().goMenu(MenuType.MAIN_MENU);
        pump(2);
        app.setPendingMinigame(minigame.MinigameType.BEGHOULED.name());
        game.navigator().goMenu(MenuType.BEGHOULED_MENU);
        pump(8);
        Screenshots.capture("screenshots/tour-46-beghouled.png");
        app.setPendingMinigame(null);
    }

    private void captureSpecial(model.level.SpecialLevel kind,
            model.ChapterType chapter, String name) {
        model.App app = game.context().app();
        app.setSelectedChapter(chapter);
        app.setSelectedLevel(2);
        app.getPlantSelection().clear();
        app.getPlantSelection().add(model.entities.plants.Plants.PEASHOOTER);
        app.getPlantSelection().add(model.entities.plants.Plants.SUNFLOWER);
        app.setPendingSpecial(kind.getKey());
        if (!new controller.menu.LevelController(app).start().isSuccess()) {
            app.setPendingSpecial(null);
            return;
        }
        game.render();
        if (!(game.getScreen() instanceof view.gui.screens.LevelScreen)) {
            app.setGame(null);
            return;
        }
        ((view.gui.screens.LevelScreen) game.getScreen()).show();
        controller.menu.LevelController runner = new controller.menu.LevelController(app);
        for (int tick = 0; tick < 90; tick++) {
            runner.tick((float) model.Game.SECONDS_PER_TICK);
        }
        pump(20);
        Screenshots.capture("screenshots/tour-" + name + ".png");
        app.setGame(null);
    }

    private void captureWorldMap() {
        game.navigator().goMenu(MenuType.CHAPTER_MENU);
        pump(6);
        com.badlogic.gdx.Screen screen = game.getScreen();
        if (!(screen instanceof view.gui.screens.WorldMapScreen)) {
            return;
        }
        view.gui.screens.WorldMapScreen map = (view.gui.screens.WorldMapScreen) screen;
        float[] stops = {0f, 0.5f, 1f};
        String[] names = {"start", "middle", "zomboss"};
        for (int i = 0; i < stops.length; i++) {
            map.scrollToFraction(stops[i]);
            pump(6);
            Screenshots.capture("screenshots/tour-28-worldmap-" + names[i] + ".png");
        }
        captureMidProgress(map);
        captureWorlds(map);
    }

    private void captureWorlds(view.gui.screens.WorldMapScreen map) {
        model.User user = game.context().user();
        if (user == null) {
            return;
        }
        int chapter = user.getLastChapter();
        int level = user.getLastLevel();
        for (model.ChapterType world : model.ChapterType.values()) {
            game.context().app().setSelectedChapter(world);
            user.setLastChapter(world.number());
            user.setLastLevel(2);
            map.show();
            pump(8);
            map.scrollToFraction(0.35f);
            pump(8);
            Screenshots.capture("screenshots/tour-32-world-"
                    + world.name().toLowerCase() + ".png");
        }
        user.setLastChapter(chapter);
        user.setLastLevel(level);
        map.show();
        pump(4);
    }

    private void captureMidProgress(view.gui.screens.WorldMapScreen map) {
        model.User user = game.context().user();
        if (user == null) {
            return;
        }
        int chapter = user.getLastChapter();
        int level = user.getLastLevel();
        user.setLastChapter(1);
        for (int stage = 1; stage <= 2; stage++) {
            user.setLastLevel(stage);
            map.show();
            pump(6);
            map.scrollToFraction(0f);
            pump(8);
            Screenshots.capture("screenshots/tour-29-worldmap-level" + stage + ".png");
        }
        user.setLastChapter(chapter);
        user.setLastLevel(level);
        map.show();
        pump(4);
    }

    private void pump(int frames) {
        for (int i = 0; i < frames; i++) {
            game.render();
        }
    }

    private void captureCheatBar() {
        game.context().settings().setDebugMode(true);
        com.badlogic.gdx.Screen screen = game.getScreen();
        if (screen instanceof BaseScreen) {
            ((BaseScreen) screen).refreshTopBar();
        }
        for (int i = 0; i < 6; i++) {
            game.render();
        }
        Screenshots.capture("screenshots/tour-24-cheat-topbar.png");
        game.context().settings().setDebugMode(false);
    }

    private void captureTitleAndPopups() {
        TitleScreen title = new TitleScreen(game.context(), new Runnable() {
            @Override
            public void run() {
            }
        });
        game.setScreen(title);
        for (int i = 0; i < 20; i++) {
            title.render(1f / 60f);
        }
        Screenshots.capture("screenshots/tour-0-title.png");

        com.badlogic.gdx.scenes.scene2d.Stage stage = title.stageForCapture();
        capturePanel(stage, new PlayerListPopup(game.context()), "tour-18-player-list.png");
        capturePanel(stage, new SettingsPopup(game.context()), "tour-19-settings-popup.png");
        capturePanel(stage, new AccountFormPopup(game.context(),
                AccountFormPopup.Mode.REGISTER, null, null), "tour-20-register.png");
        capturePanel(stage, new AccountFormPopup(game.context(),
                AccountFormPopup.Mode.PROFILE, game.context().user(), null), "tour-21-profile.png");
        AccountFormPopup reset = new AccountFormPopup(game.context(),
                AccountFormPopup.Mode.SIGN_IN, null, null);
        reset.swap(AccountFormPopup.RECOVER_IDENTITY);
        capturePanel(stage, reset, "tour-23-reset-password.png");

        capturePanel(stage, new ConfirmPopup(game.context().ui(), "Delete account",
                "Delete arya for good? This cannot be undone.", "Delete", noop()),
                "tour-22-confirm-delete.png");
    }

    private void captureOverlays() {
        com.badlogic.gdx.Screen current = game.getScreen();
        if (!(current instanceof BaseScreen)) {
            return;
        }
        com.badlogic.gdx.scenes.scene2d.Stage stage = ((BaseScreen) current).stage;
        UiKit ui = game.context().ui();

        java.util.List<String> objectives = new java.util.ArrayList<String>();
        objectives.add("Do not let the zombies reach your house.");
        objectives.add("Survive all three waves.");

        capturePanel(stage, Overlays.levelStart(ui, "Ancient Egypt", 1, objectives,
                noop()), "tour-14-level-start.png");
        capturePanel(stage, Overlays.pause(ui, noop(), noop(), noop()), "tour-15-pause.png");
        capturePanel(stage, Overlays.result(ui, false, 1250, noop(), noop()), "tour-16-defeat.png");
        capturePanel(stage, Overlays.result(ui, true, 4820, null, noop()), "tour-17-victory.png");
    }

    private void capturePanel(com.badlogic.gdx.scenes.scene2d.Stage stage,
            com.badlogic.gdx.scenes.scene2d.ui.Table overlay, String file) {
        stage.addActor(overlay);

        for (int i = 0; i < 20; i++) {
            stage.act(1f / 60f);
        }
        stage.draw();
        Screenshots.capture("screenshots/" + file);
        overlay.remove();
    }

    private Runnable noop() {
        return new Runnable() {
            @Override
            public void run() {
            }
        };
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

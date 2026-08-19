package view.gui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import model.App;
import util.Log;
import view.MenuType;
import view.gui.screens.AdventureScreen;
import view.gui.screens.ChoosePlantScreen;
import view.gui.screens.CollectionScreen;
import view.gui.screens.GreenhouseScreen;
import view.gui.screens.LeaderboardScreen;
import view.gui.screens.MainMenuScreen;
import view.gui.screens.NewsScreen;
import view.gui.screens.QuestsScreen;
import view.gui.screens.ShopScreen;
import view.gui.screens.TitleScreen;

import java.util.HashMap;
import java.util.Map;

public final class PvzGame extends Game {
    private final Map<MenuType, Screen> screens = new HashMap<MenuType, Screen>();

    private UiKit ui;
    private Toasts toasts;
    private GameContext context;
    private MenuType displayed;

    private boolean routingSuspended;
    private boolean firstFrameLogged;
    private TitleScreen titleScreen;

    private final boolean runTour;

    public PvzGame() {
        this(false);
    }

    public PvzGame(boolean runTour) {
        this.runTour = runTour;
    }

    @Override
    public void create() {
        Log.info("gui", "Window ready after "
                + (System.currentTimeMillis() - DesktopLauncher.PROCESS_START) + " ms");

        ui = new UiKit();
        toasts = new Toasts(ui);
        toasts.listenToLog();

        context = new GameContext(App.getInstance(), ui, toasts, new GameContext.Settings());

        if (runTour) {
            syncToModel(true);
            startScreenTour();
            return;
        }
        showTitle();
    }

    public void showTitle() {
        routingSuspended = true;
        if (titleScreen == null) {
            titleScreen = new TitleScreen(context, new Runnable() {
                @Override
                public void run() {
                    enterGame();
                }
            });
        }
        setScreen(titleScreen);
    }

    private void enterGame() {
        controller.Navigation.go(context.app(), MenuType.MAIN_MENU);
        resumeRouting();
    }

    @Override
    public void render() {
        if (!firstFrameLogged) {
            firstFrameLogged = true;
            Log.info("gui", "First frame at "
                    + (System.currentTimeMillis() - DesktopLauncher.PROCESS_START) + " ms");
        }
        if (!routingSuspended) {
            syncToModel(false);
        }
        super.render();

        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F11)) {
            Display.toggle();
        }
        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F12)) {
            Screenshots.capture();
            toasts.info("Screenshot saved.");
        }
        if (tour != null) {
            tour.step();
        }
    }

    public void startScreenTour() {
        tour = new ScreenTour(this);
    }

    private ScreenTour tour;

    private void syncToModel(boolean force) {
        MenuType target = context.app().getCurrentmenu();
        if (target == null) {
            target = MenuType.SIGNUP_MENU;
        }
        if (!force && target == displayed) {
            return;
        }
        Screen screen = screenFor(target);
        if (screen == null) {
            Log.warn("gui", "No screen for menu " + target + "; staying on " + displayed);
            return;
        }
        displayed = target;
        setScreen(screen);
    }

    private Screen screenFor(MenuType type) {
        Screen existing = screens.get(type);
        if (existing != null) {
            return existing;
        }
        Screen created = create(type);
        if (created != null) {
            screens.put(type, created);
        }
        return created;
    }

    private Screen create(MenuType type) {
        switch (type) {
            case MAIN_MENU:        return new MainMenuScreen(context);
            case NEWS_MENU:        return new NewsScreen(context);
            case CHAPTER_MENU:     return new AdventureScreen(context);
            case COLLECTION_MENU:  return new CollectionScreen(context);
            case GREENHOUSE_MENU:  return new GreenhouseScreen(context);
            case TRAVEL_LOG_MENU:  return new QuestsScreen(context);
            case CHOOSE_PLANT_MENU: return new ChoosePlantScreen(context);
            default:               return null;
        }
    }

    public void showLeaderboard() {
        pushDetached(new LeaderboardScreen(context));
    }

    public void showShop() {
        pushDetached(new ShopScreen(context));
    }

    public void pushDetached(Screen screen) {
        routingSuspended = true;
        setScreen(screen);
    }

    public void resumeRouting() {
        routingSuspended = false;
        displayed = null;
        syncToModel(true);
    }

    public GameContext context() {
        return context;
    }

    @Override
    public void dispose() {
        for (Screen screen : screens.values()) {
            screen.dispose();
        }
        screens.clear();
        if (toasts != null) {
            toasts.stopListening();
        }
        if (ui != null) {
            ui.dispose();
        }
        Log.info("gui", "Graphical shell stopped");
    }
}

package view.gui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import model.App;
import util.Log;
import view.MenuType;
import view.gui.screens.AdventureScreen;
import view.gui.screens.ChoosePlantScreen;
import view.gui.screens.AlmanacScreen;
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
    private Assets assets;
    private Toasts toasts;
    private GameContext context;
    private MenuType displayed;
    private MenuType unmapped;

    private view.gui.layout.LayoutEditor layoutEditor;
    private boolean firstFrameLogged;
    private TitleScreen titleScreen;
    private Navigator navigator;

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

        assets = new Assets();
        ui = new UiKit(assets);
        toasts = new Toasts(ui);
        toasts.listenToLog();

        context = new GameContext(App.getInstance(), ui, toasts,
                new GameContext.Settings(App.getInstance()), assets);
        navigator = new Navigator(this);

        if (runTour) {
            navigator.reset(MenuType.MAIN_MENU);
            startScreenTour();
            return;
        }
        showTitle();
    }

    public Navigator navigator() {
        return navigator;
    }

    public void showTitle() {
        navigator.goTitle();
    }

    void showTitleScreen() {
        if (titleScreen == null) {
            titleScreen = new TitleScreen(context, new Runnable() {
                @Override
                public void run() {
                    navigator.goMenu(MenuType.MAIN_MENU);
                }
            });
        }
        displayed = null;
        setScreen(titleScreen);
    }

    void showMenuScreen(MenuType target) {
        controller.Navigation.go(context.app(), target);
        Screen screen = screenFor(target);
        if (screen == null) {
            Log.warn("gui", "No screen for menu " + target);
            return;
        }
        displayed = target;
        setScreen(screen);
    }

    void showLeaderboardScreen() {
        displayed = null;
        setScreen(new LeaderboardScreen(context));
    }

    void showShopScreen() {
        displayed = null;
        setScreen(new ShopScreen(context));
    }

    @Override
    public void render() {
        if (!firstFrameLogged) {
            firstFrameLogged = true;
            Log.info("gui", "First frame at "
                    + (System.currentTimeMillis() - DesktopLauncher.PROCESS_START) + " ms");
        }
        syncToModel(false);
        syncLayoutEditor();
        super.render();

        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F10)) {
            context.settings().setUiEditMode(!context.settings().isUiEditMode());
        }
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

    private void syncLayoutEditor() {
        Screen screen = getScreen();
        if (!(screen instanceof Navigator.Hosted)) {
            return;
        }
        com.badlogic.gdx.scenes.scene2d.Stage stage = ((Navigator.Hosted) screen).uiStage();
        view.gui.layout.UiLayout.setScope(screen.getClass().getSimpleName());
        view.gui.layout.UiLayout.apply(stage.getRoot());
        if (!context.settings().isUiEditMode()) {
            if (layoutEditor != null && layoutEditor.getStage() != null) {
                layoutEditor.detach();
            }
            return;
        }
        if (layoutEditor == null) {
            layoutEditor = new view.gui.layout.LayoutEditor(context);
        }
        layoutEditor.attach(stage);
        layoutEditor.poll();
    }

    public void startScreenTour() {
        tour = new ScreenTour(this);
    }

    private ScreenTour tour;

    private void syncToModel(boolean force) {
        if (navigator == null || navigator.place() != Navigator.Place.MENU) {
            return;
        }
        MenuType target = context.app().getCurrentmenu();
        if (target == null || (!force && target == displayed)) {
            return;
        }
        if (screenFor(target) == null) {
            if (target != unmapped) {
                unmapped = target;
                Log.debug("gui", "Menu " + target + " has no screen; the GUI handles it in a popup");
            }
            return;
        }
        unmapped = null;
        displayed = target;
        navigator.modelChanged(target);
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
            case COLLECTION_MENU:  return new AlmanacScreen(context);
            case GREENHOUSE_MENU:  return new GreenhouseScreen(context);
            case TRAVEL_LOG_MENU:  return new QuestsScreen(context);
            case CHOOSE_PLANT_MENU: return new ChoosePlantScreen(context);
            default:               return null;
        }
    }

    public void showLeaderboard() {
        navigator.goLeaderboard();
    }

    public void showShop() {
        navigator.goShop();
    }

    public GameContext context() {
        return context;
    }

    @Override
    public void dispose() {
        view.gui.layout.UiLayout.save();
        new controller.SaveService().persist();
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
        if (assets != null) {
            assets.dispose();
        }
        Log.info("gui", "Graphical shell stopped");
    }
}

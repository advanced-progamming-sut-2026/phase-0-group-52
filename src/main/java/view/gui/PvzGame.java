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
import view.gui.screens.LoginScreen;
import view.gui.screens.MainMenuScreen;
import view.gui.screens.NewsScreen;
import view.gui.screens.ProfileScreen;
import view.gui.screens.QuestsScreen;
import view.gui.screens.SettingsScreen;
import view.gui.screens.ShopScreen;
import view.gui.screens.SignupScreen;

import java.util.HashMap;
import java.util.Map;

/**
 * The application root: owns the shared interface services and keeps the visible
 * screen in step with the model.
 *
 * <p>Navigation is deliberately one-way. Screens never call {@code setScreen};
 * they invoke a controller, the controller updates {@code App}'s current menu, and
 * {@link #render} notices the change and swaps screens. The model stays the single
 * source of truth for "where am I", exactly as in the console build, so both front
 * ends can drive the same navigation rules.
 */
public final class PvzGame extends Game {

    private final Map<MenuType, Screen> screens = new HashMap<MenuType, Screen>();

    private UiKit ui;
    private Toasts toasts;
    private GameContext context;
    private MenuType displayed;

    /**
     * Screens that exist only inside a level (choose-plant, the lawn, overlays) are
     * pushed directly rather than via a MenuType, so the router leaves them alone.
     */
    private boolean routingSuspended;

    /** When set, a scripted capture run starts as soon as the shell is up. */
    private final boolean runTour;

    public PvzGame() {
        this(false);
    }

    public PvzGame(boolean runTour) {
        this.runTour = runTour;
    }

    @Override
    public void create() {
        Log.info("gui", "Starting graphical shell");

        ui = new UiKit();
        toasts = new Toasts(ui);
        toasts.listenToLog();
        context = new GameContext(App.getInstance(), ui, toasts, new GameContext.Settings());

        syncToModel(true);

        if (runTour) {
            startScreenTour();
        }
    }

    @Override
    public void render() {
        if (!routingSuspended) {
            syncToModel(false);
        }
        super.render();

        if (com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.F12)) {
            Screenshots.capture();
            toasts.info("Screenshot saved.");
        }
        if (tour != null) {
            tour.step();
        }
    }

    /**
     * Attaches a scripted walk-through that visits each screen and captures it.
     * Used to check the interface renders without having to click through it by
     * hand; started by {@link DesktopLauncher} when {@code -Dpvz.tour=true} is set.
     */
    public void startScreenTour() {
        tour = new ScreenTour(this);
    }

    private ScreenTour tour;

    /**
     * Compares the model's current menu with what is on screen and swaps if they
     * differ.
     */
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
            // No graphical screen exists for this menu yet; stay where we are so the
            // window never goes blank.
            Log.warn("gui", "No screen for menu " + target + "; staying on " + displayed);
            return;
        }
        displayed = target;
        setScreen(screen);
    }

    /** Screens are built once and reused; {@code show()} rebuilds their contents. */
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
            case SIGNUP_MENU:      return new SignupScreen(context);
            case LOGIN_MENU:       return new LoginScreen(context);
            case MAIN_MENU:        return new MainMenuScreen(context);
            case PROFILE_MEMU:     return new ProfileScreen(context);
            case SETTINGS_MENU:    return new SettingsScreen(context);
            case NEWS_MENU:        return new NewsScreen(context);
            case CHAPTER_MENU:     return new AdventureScreen(context);
            case COLLECTION_MENU:  return new CollectionScreen(context);
            case GREENHOUSE_MENU:  return new GreenhouseScreen(context);
            case TRAVEL_LOG_MENU:  return new QuestsScreen(context);
            case CHOOSE_PLANT_MENU: return new ChoosePlantScreen(context);
            default:               return null;
        }
    }

    /** Opens the leaderboard, which has no MenuType of its own. */
    public void showLeaderboard() {
        pushDetached(new LeaderboardScreen(context));
    }

    /** Opens the shop, which the console reaches from inside the greenhouse. */
    public void showShop() {
        pushDetached(new ShopScreen(context));
    }

    /**
     * Shows a screen that is not tied to a menu in the model, and stops the router
     * until {@link #resumeRouting()} is called.
     */
    public void pushDetached(Screen screen) {
        routingSuspended = true;
        setScreen(screen);
    }

    /** Returns to whatever menu the model currently says we are in. */
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

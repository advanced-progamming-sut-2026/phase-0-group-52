package view.gui;

import com.badlogic.gdx.Gdx;
import controller.Navigation;
import util.Log;
import view.MenuType;

/**
 * Walks the interface from screen to screen, capturing each one.
 *
 * <p>A development aid, enabled with {@code -Dpvz.tour=true}. It answers the
 * question "does every menu still render" without a person clicking through all of
 * them, and leaves a set of PNGs that double as documentation.
 *
 * <p>It drives navigation the same way the screens do — by asking
 * {@link Navigation} to change menus — so it exercises the real routing rather
 * than reaching into the screen stack.
 */
final class ScreenTour {

    /** Frames to wait on each screen so entry animations finish before capture. */
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

    /** Called once per frame; advances the tour when the current screen has settled. */
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

    /**
     * The leaderboard and shop have no menu of their own, so they are visited
     * directly at the end of the run.
     */
    private void captureExtras() {
        finished = true;
        captureOverlays();
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
                                Log.info("tour", "Screen tour complete");
                                Gdx.app.exit();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    /**
     * Captures the three in-level panels. They normally sit over the lawn, which
     * does not exist yet, so they are shown over the current screen instead — enough
     * to check they lay out and read correctly.
     */
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

    /** Draws one frame with the overlay attached, captures it, then removes it. */
    private void capturePanel(com.badlogic.gdx.scenes.scene2d.Stage stage,
            com.badlogic.gdx.scenes.scene2d.ui.Table overlay, String file) {
        stage.addActor(overlay);
        // Settle the entry animation before the grab.
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

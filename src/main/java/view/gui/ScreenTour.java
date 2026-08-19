package view.gui;

import com.badlogic.gdx.Gdx;
import controller.Navigation;
import util.Log;
import view.MenuType;
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
                                Log.info("tour", "Screen tour complete");
                                Gdx.app.exit();
                            }
                        });
                    }
                }).start();
            }
        });
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

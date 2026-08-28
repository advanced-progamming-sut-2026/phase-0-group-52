package view.gui;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import util.Log;
import model.enums.MenuType;

import java.util.ArrayDeque;
import java.util.Deque;

public final class Navigator {
    public enum Place { TITLE, MENU, LEADERBOARD, SHOP }

    public enum PopupKind { NONE, PLAYERS, SETTINGS }

    public interface Hosted {
        Stage uiStage();
    }

    private static final int MAX_DEPTH = 24;

    private final PvzGame game;
    private final Deque<Entry> history = new ArrayDeque<Entry>();

    private Entry current = new Entry(Place.TITLE, null, PopupKind.NONE);

    public Navigator(PvzGame game) {
        this.game = game;
    }

    public Place place() {
        return current.place;
    }

    public MenuType menu() {
        return current.menu;
    }

    public boolean canGoBack() {
        return !history.isEmpty();
    }

    public void goTitle() {
        push();
        apply(new Entry(Place.TITLE, null, PopupKind.NONE));
    }

    public void goMenu(MenuType menu) {
        if (current.place == Place.MENU && current.menu == menu) {
            return;
        }
        push();
        apply(new Entry(Place.MENU, menu, PopupKind.NONE));
    }

    public void goLeaderboard() {
        push();
        apply(new Entry(Place.LEADERBOARD, null, PopupKind.NONE));
    }

    public void goShop() {
        push();
        apply(new Entry(Place.SHOP, null, PopupKind.NONE));
    }

    public void back() {
        if (history.isEmpty()) {
            Log.debug("gui", "Back with empty history; falling back to the title screen");
            apply(new Entry(Place.TITLE, null, PopupKind.NONE));
            return;
        }
        apply(history.removeLast());
    }

    public void modelChanged(MenuType menu) {
        if (current.place == Place.MENU && current.menu == menu) {
            return;
        }
        push();
        current = new Entry(Place.MENU, menu, PopupKind.NONE);
        show(current);
    }

    public void reset(MenuType menu) {
        history.clear();
        current = new Entry(Place.MENU, menu, PopupKind.NONE);
        show(current);
    }

    private void push() {
        history.addLast(new Entry(current.place, current.menu, openPopup()));
        while (history.size() > MAX_DEPTH) {
            history.removeFirst();
        }
        clearPopups();
    }

    private void clearPopups() {
        Stage stage = currentStage();
        if (stage == null) {
            return;
        }
        for (Actor actor : stage.getActors().toArray()) {
            if (actor instanceof Popup) {
                actor.clearActions();
                actor.remove();
            }
        }
    }

    private void apply(Entry entry) {
        clearPopups();
        current = entry;
        show(entry);
        reopen(entry);
    }

    private void show(Entry entry) {
        switch (entry.place) {
            case TITLE:
                game.showTitleScreen();
                break;
            case LEADERBOARD:
                game.showLeaderboardScreen();
                break;
            case SHOP:
                game.showShopScreen();
                break;
            default:
                game.showMenuScreen(entry.menu);
                break;
        }
    }

    private PopupKind openPopup() {
        Stage stage = currentStage();
        if (stage == null) {
            return PopupKind.NONE;
        }
        for (Actor actor : stage.getActors()) {
            if (actor instanceof Popup) {
                return ((Popup) actor).kind();
            }
        }
        return PopupKind.NONE;
    }

    private void reopen(Entry entry) {
        if (entry.popup == PopupKind.NONE) {
            return;
        }
        Stage stage = currentStage();
        if (stage == null) {
            return;
        }
        Popup popup = (entry.popup == PopupKind.PLAYERS)
                ? new PlayerListPopup(game.context(), entry.place != Place.LEADERBOARD)
                : new SettingsPopup(game.context());
        popup.showOn(stage);
    }

    private Stage currentStage() {
        Screen screen = game.getScreen();
        return (screen instanceof Hosted) ? ((Hosted) screen).uiStage() : null;
    }

    private static final class Entry {
        private final Place place;
        private final MenuType menu;
        private final PopupKind popup;

        private Entry(Place place, MenuType menu, PopupKind popup) {
            this.place = place;
            this.menu = menu;
            this.popup = popup;
        }
    }
}

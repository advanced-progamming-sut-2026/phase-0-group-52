package pvz.model;

import pvz.view.AppMenu;

import java.util.ArrayList;

import pvz.minigame.*;

public class App {
    private Game game;
    private ArrayList<User> users;
    private User currentuser;
    private AppMenu currentmenu;
    private Minigame minigame;

    public App(Game game, ArrayList<User> users, User currentuser, AppMenu currentmenu, Minigame minigame) {
        this.game = game;
        this.users = users;
        this.currentuser = currentuser;
        this.currentmenu = currentmenu;
        this.minigame = minigame;
    }
}

package pvz.model;

import pvz.view.AppMenu;

import java.util.ArrayList;

import pvz.minigame.*;
import pvz.view.MenuType;

public class App {
    private Game game;
    private ArrayList<User> users;
    private User currentuser;
    private MenuType currentmenu;
    private Minigame minigame;

    public App(Game game, ArrayList<User> users, User currentuser, MenuType currentmenu, Minigame minigame) {
        this.game = game;
        this.users = users;
        this.currentuser = currentuser;
        this.currentmenu = currentmenu;
        this.minigame = minigame;
    }

    public User getCurrentuser() { return currentuser; }
    public void setCurrentuser(User currentuser) { this.currentuser = currentuser; }

    public MenuType getCurrentmenu() { return currentmenu; }
    public void setCurrentmenu(MenuType currentmenu) { this.currentmenu = currentmenu; }

    public ArrayList<User> getUsers() { return users; }
}

package model;

import database.UserRepository;
import model.enums.Menu;
import view.AppMenu;

import java.util.ArrayList;

import minigame.*;
import view.MenuType;

public class App {
//    private static final List<User> users = new ArrayList<>();

    private static App instance;
    private Game game;
    private GameEngine gameEngine;
    public Menu currentMenu;
    private ArrayList<User> users = new ArrayList<>();
    private User currentuser;
    public static User loggedInUser;
    private MenuType currentmenu;
    private Minigame minigame;



    public App(Game game, ArrayList<User> users, User currentuser, MenuType currentmenu, Minigame minigame) {
        this.game = game;
        this.users = users;
        this.currentuser = currentuser;
        this.currentmenu = currentmenu;
        this.minigame = minigame;
    }


    private App(){
        UserRepository repository = new UserRepository();
        User rememberedUser = repository.getRememberedUser();

        if (rememberedUser != null) {
            loggedInUser = rememberedUser;
            currentMenu = Menu.MainMenu;
        } else {
            currentMenu = Menu.SignUpMenu;
        }
    }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public GameEngine getGameEngine() { return gameEngine; }
    public void setGameEngine(GameEngine gameEngine) { this.gameEngine = gameEngine; }

    public User getCurrentuser() { return currentuser; }
    public void setCurrentuser(User currentuser) { this.currentuser = currentuser; }

    public MenuType getCurrentmenu() { return currentmenu; }
    public void setCurrentmenu(MenuType currentmenu) { this.currentmenu = currentmenu; }


    public static App getInstance(){
        if(instance==null){instance = new App ();}
        return instance;
    }

    public Menu getCurrentMenu() {
        return currentMenu;
    }

    public void setCurrentMenu(Menu currentMenu) {
        this.currentMenu = currentMenu;
    }

    public static void setInstance(App instance) {
        App.instance = instance;
    }
}


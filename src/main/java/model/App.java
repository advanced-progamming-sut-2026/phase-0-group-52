package model;

import database.UserRepository;
import model.enums.Menu;
import view.AppMenu;

import java.util.ArrayList;

import minigame.*;
import model.greenhouse.Greenhouse;
import model.shop.Shop;
import view.MenuType;

public class App {

    private static App instance;
    private Game game;
    public Menu currentMenu;
    private ArrayList<User> users = new ArrayList<>();
    private User currentuser;
    public static User loggedInUser;
    private MenuType currentmenu;
    private Minigame minigame;
    private final Greenhouse greenhouse = new Greenhouse();
    private final Shop shop = new Shop();
    private final java.util.List<model.entities.plants.Plants> plantSelection = new ArrayList<>();
    private final java.util.Set<model.entities.plants.Plants> boostedSelection = new java.util.HashSet<>();

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
            currentuser = rememberedUser;
            currentMenu = Menu.MainMenu;
            currentmenu = MenuType.MAIN_MENU;
        } else {
            currentMenu = Menu.SignUpMenu;
            currentmenu = MenuType.SIGNUP_MENU;
        }
    }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public User getCurrentuser() { return currentuser; }
    public void setCurrentuser(User currentuser) { this.currentuser = currentuser; }

    public MenuType getCurrentmenu() { return currentmenu; }
    public void setCurrentmenu(MenuType currentmenu) { this.currentmenu = currentmenu; }

    public Greenhouse getGreenhouse() { return greenhouse; }
    public Shop getShop() { return shop; }
    public java.util.List<model.entities.plants.Plants> getPlantSelection() { return plantSelection; }
    public java.util.Set<model.entities.plants.Plants> getBoostedSelection() { return boostedSelection; }

    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User user) { loggedInUser = user; this.currentuser = user; }

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

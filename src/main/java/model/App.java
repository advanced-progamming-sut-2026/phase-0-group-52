package model;

import database.UserRepository;

import java.util.ArrayList;

import model.greenhouse.Greenhouse;
import model.shop.Shop;
import model.enums.MenuType;

public class App {

    private Game suspendedGame;

    public Game getSuspendedGame() {
        return suspendedGame;
    }

    public void setSuspendedGame(Game value) {
        this.suspendedGame = value;
    }


    private static App instance;
    private Game game;
    private User currentuser;
    public static User loggedInUser;
    private MenuType currentmenu;
    private final Shop shop = new Shop();
    private final java.util.List<model.entities.plants.Plants> plantSelection = new ArrayList<>();
    private final java.util.Set<model.entities.plants.Plants> boostedSelection = new java.util.HashSet<>();
    private int selectedLevel = 1;
    private ChapterType selectedChapter;
    private model.entities.plants.Plants imitatedPlant;
    private boolean awaitingImitate;
    private String pendingSpecial;
    private String pendingMinigame;
    private final java.util.Set<model.entities.plants.Plants> lockedPlants = new java.util.HashSet<>();

    private App(){
        UserRepository repository = new UserRepository();
        User rememberedUser = repository.getRememberedUser();

        if (rememberedUser != null) {
            loggedInUser = rememberedUser;
            currentuser = rememberedUser;
            currentmenu = MenuType.MAIN_MENU;
            currentmenu = MenuType.MAIN_MENU;
        } else {
            currentmenu = MenuType.SIGNUP_MENU;
            currentmenu = MenuType.SIGNUP_MENU;
        }
    }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    public User getCurrentuser() { return currentuser; }
    public void setCurrentuser(User currentuser) { this.currentuser = currentuser; }

    public MenuType getCurrentmenu() { return currentmenu; }
    public void setCurrentmenu(MenuType currentmenu) { this.currentmenu = currentmenu; }

    public Greenhouse getGreenhouse() {
        return getLoggedInUser() == null ? null : getLoggedInUser().getGreenhouse();
    }
    public Shop getShop() { return shop; }
    public java.util.List<model.entities.plants.Plants> getPlantSelection() { return plantSelection; }
    public int getSelectedLevel() { return selectedLevel; }
    public void setSelectedLevel(int selectedLevel) { this.selectedLevel = selectedLevel; }
    public ChapterType getSelectedChapter() { return selectedChapter; }
    public void setSelectedChapter(ChapterType selectedChapter) { this.selectedChapter = selectedChapter; }
    public model.entities.plants.Plants getImitatedPlant() { return imitatedPlant; }
    public void setImitatedPlant(model.entities.plants.Plants imitatedPlant) { this.imitatedPlant = imitatedPlant; }
    public boolean isAwaitingImitate() { return awaitingImitate; }
    public void setAwaitingImitate(boolean awaitingImitate) { this.awaitingImitate = awaitingImitate; }
    public String getPendingSpecial() { return pendingSpecial; }
    public void setPendingSpecial(String pendingSpecial) { this.pendingSpecial = pendingSpecial; }

    public String getPendingMinigame() { return pendingMinigame; }
    public void setPendingMinigame(String value) { this.pendingMinigame = value; }
    public java.util.Set<model.entities.plants.Plants> getLockedPlants() { return lockedPlants; }
    public java.util.Set<model.entities.plants.Plants> getBoostedSelection() { return boostedSelection; }

    public User getLoggedInUser() { return loggedInUser; }
    public void setLoggedInUser(User user) { loggedInUser = user; this.currentuser = user; }

    public static App getInstance(){
        if(instance==null){instance = new App ();}
        return instance;
    }

    public static void setInstance(App instance) {
        App.instance = instance;
    }
}

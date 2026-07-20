package model.enums;

import view.*;

import java.util.Scanner;

public enum Menu {
    LoginMenu(new LoginMenu()),
    SignUpMenu(new SignupMenu()),
    ProfileMenu(new ProfileMenu()),
    MainMenu(new MainMenu()),
    GameMenu(new GameMenu()),
    SettingMenu(new SettingMenu()),
    ChapterMenu(new ChapterMenu()),
    NetworkMenu(new NetworkMenu()),
    NewsMenu(new NewsMenu()),
    CollectionMenu(new CollectionMenu()),
    GreenhouseMenu(new GreenhouseMenu()),
    TravelLogMenu(new TravelLogMenu()),
    ChoosePlantMenu(new ChoosePlantMenu());
    private final AppMenu menu;
    Menu(AppMenu menu){
        this.menu = menu;
    }

    public void checkCommand(Scanner scanner){
        this.menu.check(scanner);
    }
}

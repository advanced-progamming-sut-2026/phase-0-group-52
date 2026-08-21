package view;

import model.enums.Menu;

public enum MenuType {
    SIGNUP_MENU, GAME_MENU, CHAPTER_MENU, COLLECTION_MENU, GREENHOUSE_MENU, LOGIN_MENU, MAIN_MENU, NETWORK_MENU,
    NEWS_MENU, PROFILE_MEMU, SETTINGS_MENU, TRAVEL_LOG_MENU, CHOOSE_PLANT_MENU;

    public static MenuType fromName(String name) {
        if (name == null) return null;
        String n = name.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (MenuType t : values())
            if (t.name().equals(n) || t.name().equals(n + "_MENU")) return t;
        if (n.equals("SETTING")) return SETTINGS_MENU;
        if (n.equals("PROFILE") || n.equals("PROFILE_MENU")) return PROFILE_MEMU;
        if (n.equals("SIGN_UP")) return SIGNUP_MENU;
        if (n.equals("TRAVELLOG")) return TRAVEL_LOG_MENU;
        if (n.equals("CHOOSEPLANT") || n.equals("CHOOSE_PLANT")) return CHOOSE_PLANT_MENU;
        return null;
    }

    public Menu toMenu() {
        switch (this) {
            case SIGNUP_MENU:      return Menu.SignUpMenu;
            case GAME_MENU:        return Menu.GameMenu;
            case CHAPTER_MENU:     return Menu.ChapterMenu;
            case COLLECTION_MENU:  return Menu.CollectionMenu;
            case GREENHOUSE_MENU:  return Menu.GreenhouseMenu;
            case LOGIN_MENU:       return Menu.LoginMenu;
            case MAIN_MENU:        return Menu.MainMenu;
            case NETWORK_MENU:     return Menu.NetworkMenu;
            case NEWS_MENU:        return Menu.NewsMenu;
            case PROFILE_MEMU:     return Menu.ProfileMenu;
            case SETTINGS_MENU:    return Menu.SettingMenu;
            case TRAVEL_LOG_MENU:  return Menu.TravelLogMenu;
            case CHOOSE_PLANT_MENU: return Menu.ChoosePlantMenu;
            default:               return null;
        }
    }
}

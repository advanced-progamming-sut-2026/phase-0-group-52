package model.enums;


public enum MenuType {
    SIGNUP_MENU, GAME_MENU, CHAPTER_MENU, COLLECTION_MENU, GREENHOUSE_MENU, LOGIN_MENU, MAIN_MENU, NETWORK_MENU,
    NEWS_MENU, PROFILE_MEMU, SETTINGS_MENU, TRAVEL_LOG_MENU, CHOOSE_PLANT_MENU,
    MINIGAME_LIST_MENU, MINIGAME_MENU, BEGHOULED_MENU;

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
        if (n.equals("MINIGAMES") || n.equals("MINIGAME_LIST")) return MINIGAME_LIST_MENU;
        if (n.equals("MINIGAME")) return MINIGAME_MENU;
        if (n.equals("BEGHOULED")) return BEGHOULED_MENU;
        return null;
    }

}

package view.gui;

public final class Icons {
    public static final class Icon {
        private final String normal;
        private final String selected;

        Icon(String normal, String selected) {
            this.normal = normal;
            this.selected = selected;
        }

        public String normal() {
            return normal;
        }

        public String selected() {
            return selected;
        }
    }

    public static final Icon BACK =
            new Icon("image_ui_almanac_buttons_hud_back_normal",
                    "image_ui_almanac_buttons_hud_back_selected");
    public static final Icon SETTINGS =
            new Icon("image_ui_hud_settingsbutton_buttons_hud_settings_normal",
                    "image_ui_hud_settingsbutton_buttons_hud_settings_selected");
    public static final Icon ALMANAC =
            new Icon("image_ui_hud_almanacbutton_buttons_hud_almanac_normal",
                    "image_ui_hud_almanacbutton_buttons_hud_almanac_selected");
    public static final Icon NEWS =
            new Icon("image_ui_hud_tasklist_buttons_hud_task_list_normal",
                    "image_ui_hud_tasklist_buttons_hud_task_list_selected");
    public static final Icon QUESTS =
            new Icon("image_ui_generic_buttons_hud_quests_normal",
                    "image_ui_generic_buttons_hud_quests_selected");
    public static final Icon MINIGAMES =
            new Icon("image_ui_generic_button_hud_minigames_normal",
                    "image_ui_generic_button_hud_minigames_selected");
    public static final Icon GREENHOUSE =
            new Icon("image_ui_generic_buttons_hud_zg_normal",
                    "image_ui_generic_buttons_hud_zg_selected");
    public static final Icon SHOP =
            new Icon("image_ui_hud_eventshop_buttons_hud_event_shop_normal",
                    "image_ui_hud_eventshop_buttons_hud_event_shop_selected");
    public static final Icon PLAYERS =
            new Icon("image_ui_mainmenu_edit_btn_normal",
                    "image_ui_mainmenu_edit_btn_pressed");
    public static final Icon QUIT_GAME =
            new Icon("image_ui_generic_close_btn",
                    "image_ui_generic_close_down");
    public static final Icon CLOSE_POPUP =
            new Icon("image_ui_generic_close_circle",
                    "image_ui_generic_close_circle_down");
    public static final Icon SHOVEL =
            new Icon("image_ui_hud_ingame_shovel_button",
                    "image_ui_hud_ingame_shovel_button_down");
    public static final Icon PAUSE =
            new Icon("image_ui_hud_ingame_pause_button",
                    "image_ui_hud_ingame_pause_button_down");
    public static final Icon PLANT_FOOD =
            new Icon("image_ui_hud_ingame_plantfood_button",
                    "image_ui_hud_ingame_plantfood_button_down");
    public static final Icon FAST_FORWARD =
            new Icon("image_ui_hud_ingame_2x",
                    "image_ui_hud_ingame_2x_selected");

    private Icons() {
    }
}

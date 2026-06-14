package pvz.controller.menu;

import pvz.model.App;
import pvz.model.news.NewsList;
import pvz.view.MenuType;
import pvz.view.NewsMenu;

public class NewsMenuController {

    private final App app;
    private final NewsMenu view;

    public NewsMenuController(App app) {
        this.app = app;
        this.view = new NewsMenu();
    }

    public void handleCommand(String[] parts) {
        if (parts.length < 2) {
            view.showError("Invalid command.");
            return;
        }
        switch (parts[1]) {
            case "show":
                if (parts.length >= 3 && parts[2].equals("current"))
                    System.out.println("Current menu: " + app.getCurrentmenu());
                else
                    view.showError("Usage: menu show current");
                break;
            case "enter":
                handleEnter(parts);
                break;
            case "news":
                handleNews(parts);
                break;
            default:
                view.showError("Unknown command: " + parts[1]);
        }
    }

    private void handleNews(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu news show-unread  |  menu news show-all");
            return;
        }
        NewsList newsList = app.getCurrentuser().getNewsList();
        switch (parts[2]) {
            case "show-unread":
                view.showNewsList(newsList.getUnread(), true);
                newsList.markAllRead();
                break;
            case "show-all":
                view.showNewsList(newsList.getAll(), false);
                break;
            default:
                view.showError("Unknown subcommand: " + parts[2]);
        }
    }

    private void handleEnter(String[] parts) {
        if (parts.length < 3) {
            view.showError("Usage: menu enter <menu_name>");
            return;
        }
        try {
            MenuType target = MenuType.valueOf(parts[2].toUpperCase());
            app.setCurrentmenu(target);
        } catch (IllegalArgumentException e) {
            view.showError("Unknown menu: " + parts[2]);
        }
    }
}

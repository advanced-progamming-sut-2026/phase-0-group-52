package pvz.view;

import pvz.controller.menu.NewsMenuController;
import pvz.model.App;
import pvz.model.news.News;

import java.util.List;
import java.util.Scanner;

public class NewsMenu implements AppMenu {

    private NewsMenuController controller;

    @Override
    public void check(Scanner scanner) {
        if (controller == null) controller = new NewsMenuController(App.getInstance());
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        controller.handleCommand(line.split("\\s+"));
    }

    public void showNewsList(List<News> list, boolean onlyUnread) {
        if (list.isEmpty()) {
            System.out.println(onlyUnread ? "No unread notifications." : "No notifications.");
            return;
        }
        System.out.println(onlyUnread ? "Unread notifications:" : "All notifications:");
        for (int i = 0; i < list.size(); i++) {
            News n = list.get(i);
            String status = onlyUnread ? "" : (n.isIsread() ? "[read]  " : "[new]   ");
            System.out.println("  " + (i + 1) + ". " + status + n.getNews());
        }
    }

    public void showError(String message) {
        System.out.println("Error: " + message);
    }
}

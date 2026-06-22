package pvz.view;

import pvz.model.news.News;

import java.util.List;

public class NewsMenu implements AppMenu {

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

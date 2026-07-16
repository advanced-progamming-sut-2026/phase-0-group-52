package pvz.model.news;

import java.util.ArrayList;
import java.util.List;

public class NewsList {
    private ArrayList<News> newslist;

    public NewsList() {
        this.newslist = new ArrayList<>();
    }

    public void addNews(String content) {
        newslist.add(new News(content, false));
    }

    public List<News> getUnread() {
        List<News> unread = new ArrayList<>();
        for (News n : newslist)
            if (!n.isIsread()) unread.add(n);
        return unread;
    }

    public List<News> getAll() {
        return newslist;
    }

    public void markAllRead() {
        for (News n : newslist)
            n.setIsread(true);
    }
}

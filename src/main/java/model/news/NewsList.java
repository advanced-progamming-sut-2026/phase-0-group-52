package model.news;

import java.util.ArrayList;
import java.util.List;

public class NewsList {
    private final ArrayList<News> newslist;

    public NewsList() {
        this.newslist = new ArrayList<>();
        addNews("Welcome to Plants vs. Zombies! Defend your lawn against the zombie horde.");
        addNews("The Travel Log now tracks daily, main, and epic quests with rewards.");
        addNews("Tip: upgrade your plants in the greenhouse to boost damage, health, and speed.");
        addNews("Five minigames are available: Beghouled, Vasebreaker, Wallnut Bowling, I-Zombie, and Zombotany.");
        addNews("Special levels unlocked: Conveyor Belt, Save Our Seeds, Dead Line, Timed War, and more.");
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

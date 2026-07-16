package model;

import model.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {

    public static class Entry {
        private final User user;
        private final int passedLevels;

        Entry(User user, int passedLevels) {
            this.user = user;
            this.passedLevels = passedLevels;
        }

        public User getUser() { return user; }
        public int getPassedLevels() { return passedLevels; }

        public String getProgressText() {
            return "chapter " + (passedLevels / 4 + 1) + " level " + (passedLevels % 4 + 1);
        }
    }

    private final UserRepository repository = new UserRepository();

    public List<Entry> getEntries(String column, boolean ascending) {
        List<User> users = repository.getAllUsers();
        List<Entry> entries = new ArrayList<Entry>();
        for (User user : users)
            entries.add(new Entry(user, repository.getPassedLevels(user.getId())));
        Comparator<Entry> comparator = comparatorFor(column);
        if (!ascending) comparator = Collections.reverseOrder(comparator);
        Collections.sort(entries, comparator);
        return entries;
    }

    private Comparator<Entry> comparatorFor(final String column) {
        return new Comparator<Entry>() {
            @Override
            public int compare(Entry a, Entry b) {
                switch (column) {
                    case "level":
                        return Integer.compare(a.passedLevels, b.passedLevels);
                    case "minigames":
                        return Integer.compare(a.user.getMiniGamesPlayed(), b.user.getMiniGamesPlayed());
                    case "daily":
                        return Integer.compare(a.user.getQuestDailyNum(), b.user.getQuestDailyNum());
                    case "quests":
                        return Integer.compare(a.user.getQuestNonDailyNum(), b.user.getQuestNonDailyNum());
                    default:
                        return Integer.compare(a.user.getMaxPoint(), b.user.getMaxPoint());
                }
            }
        };
    }
}

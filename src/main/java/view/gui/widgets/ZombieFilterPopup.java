package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import model.User;
import model.entities.zombies.ZombieData;
import model.entities.zombies.ZombieRecord;
import view.gui.GameContext;
import view.gui.Navigator;
import view.gui.Popup;
import view.gui.Theme;
import view.gui.UiKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ZombieFilterPopup extends Popup {

    public enum SortKey { ALPHABETICAL, DISCOVERY, CHAPTER, TOUGHNESS, SPEED }

    private static final String[] CHAPTERS = {
        "ANCIENT_EGYPT", "FROSTBITE_CAVES",
        "BIG_WAVE_BEACH", "DARK_AGES", "ZOMBOSS",
    };

    public static final class Rule {
        private final SortKey key;
        private boolean ascending = true;

        Rule(SortKey key) {
            this.key = key;
        }

        public SortKey getKey() {
            return key;
        }

        public boolean isAscending() {
            return ascending;
        }

        public void flip() {
            ascending = !ascending;
        }
    }

    public static final class Rules {
        private final List<Rule> order = new ArrayList<Rule>();
        private final Set<String> chapters = new LinkedHashSet<String>();
        private String query = "";
        private boolean showSeen = true;
        private boolean showUnseen = true;

        public Rules() {
            for (SortKey key : SortKey.values()) {
                order.add(new Rule(key));
            }
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String value) {
            query = value == null ? "" : value;
        }

        public List<Rule> getOrder() {
            return order;
        }

        public Set<String> getChapters() {
            return chapters;
        }

        public boolean isShowSeen() {
            return showSeen;
        }

        public void setShowSeen(boolean value) {
            showSeen = value;
        }

        public boolean isShowUnseen() {
            return showUnseen;
        }

        public void setShowUnseen(boolean value) {
            showUnseen = value;
        }

        public void move(int from, int to) {
            if (from < 0 || to < 0 || from >= order.size() || to >= order.size()) {
                return;
            }
            order.add(to, order.remove(from));
        }

        public List<ZombieRecord> apply(User user, boolean revealAll) {
            List<ZombieRecord> out = new ArrayList<ZombieRecord>();
            for (ZombieRecord record : ZombieData.all()) {
                if (keeps(record, user, revealAll)) {
                    out.add(record);
                }
            }
            Collections.sort(out, comparator());
            return out;
        }

        private boolean keeps(ZombieRecord record, User user, boolean revealAll) {
            if (!query.isEmpty()
                    && !record.getName().toLowerCase().contains(query.toLowerCase())) {
                return false;
            }
            if (!chapters.isEmpty() && !chapters.contains(record.getChapter())) {
                return false;
            }
            boolean seen = revealAll || ZombieData.isSeen(user, record);
            return seen ? showSeen : showUnseen;
        }

        private Comparator<ZombieRecord> comparator() {
            return new Comparator<ZombieRecord>() {
                @Override
                public int compare(ZombieRecord a, ZombieRecord b) {
                    for (Rule rule : order) {
                        int cmp = compareBy(rule.getKey(), a, b);
                        if (cmp != 0) {
                            return rule.isAscending() ? cmp : -cmp;
                        }
                    }
                    return 0;
                }
            };
        }

        private int compareBy(SortKey key, ZombieRecord a, ZombieRecord b) {
            switch (key) {
                case ALPHABETICAL:
                    return a.getName().compareToIgnoreCase(b.getName());
                case DISCOVERY:
                    return Integer.compare(a.getId(), b.getId());
                case CHAPTER:
                    return a.getChapter().compareTo(b.getChapter());
                case TOUGHNESS:
                    return Integer.compare(a.getToughness().getIndex(),
                            b.getToughness().getIndex());
                case SPEED:
                    return Integer.compare(a.getSpeed().getIndex(), b.getSpeed().getIndex());
                default:
                    return 0;
            }
        }
    }

    private final Rules rules;
    private final Runnable onChange;
    private final Table columns = new Table();

    public ZombieFilterPopup(GameContext context, Rules rules, Runnable onChange) {
        super(context.ui(), "Sort and filter", 760f, 560f);
        this.rules = rules;
        this.onChange = onChange;
        body().add(columns).grow();
        rebuild();
    }

    @Override
    public Navigator.PopupKind kind() {
        return Navigator.PopupKind.NONE;
    }

    private void rebuild() {
        columns.clear();
        columns.top();
        columns.add(sortColumn()).width(320f).top().padRight(Theme.PAD_LARGE);
        columns.add(filterColumn()).width(320f).top();
    }

    private Table sortColumn() {
        Table column = new Table();
        column.top().left();
        Label head = new Label("Sort", ui.skin(), "rowHeader");
        column.add(head).left().padBottom(Theme.PAD_SMALL).row();

        Label hint = new Label("Drag to reorder. Click to flip direction.",
                ui.skin(), "muted");
        column.add(hint).left().padBottom(Theme.PAD_SMALL).row();

        column.add(new SortList(ui, new SortList.Model() {
            @Override
            public int size() {
                return rules.getOrder().size();
            }

            @Override
            public String label(int index) {
                return pretty(rules.getOrder().get(index).getKey().name());
            }

            @Override
            public boolean ascending(int index) {
                return rules.getOrder().get(index).isAscending();
            }

            @Override
            public void flip(int index) {
                rules.getOrder().get(index).flip();
            }

            @Override
            public void move(int from, int to) {
                rules.move(from, to);
            }
        }, new Runnable() {
            @Override
            public void run() {
                notifyChange();
            }
        })).growX();
        return column;
    }

    private Table filterColumn() {
        Table column = new Table();
        column.top().left();
        Label head = new Label("Filter", ui.skin(), "rowHeader");
        column.add(head).left().padBottom(Theme.PAD_SMALL).row();

        column.add(seenBox("Discovered", true)).left().row();
        column.add(seenBox("Not discovered", false)).left().padBottom(Theme.PAD).row();

        Label where = new Label("Chapter", ui.skin(), "rowSub");
        column.add(where).left().padBottom(Theme.PAD_SMALL).row();
        for (String chapter : CHAPTERS) {
            column.add(chapterBox(chapter)).left().row();
        }
        return column;
    }

    private CheckBox seenBox(String text, final boolean seenSide) {
        CheckBox box = ui.checkBox(text);
        box.setChecked(seenSide ? rules.isShowSeen() : rules.isShowUnseen());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                boolean value = ((CheckBox) actor).isChecked();
                if (seenSide) {
                    rules.setShowSeen(value);
                } else {
                    rules.setShowUnseen(value);
                }
                notifyChange();
            }
        });
        return box;
    }

    private CheckBox chapterBox(final String chapter) {
        CheckBox box = ui.checkBox(pretty(chapter));
        box.setChecked(rules.getChapters().contains(chapter));
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (((CheckBox) actor).isChecked()) {
                    rules.getChapters().add(chapter);
                } else {
                    rules.getChapters().remove(chapter);
                }
                notifyChange();
            }
        });
        return box;
    }

    private void notifyChange() {
        if (onChange != null) {
            onChange.run();
        }
    }

    private static String pretty(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}

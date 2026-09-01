package view.gui.widgets;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import model.ChapterType;
import model.entities.plants.PlantData;
import model.entities.plants.PlantRecord;
import model.entities.plants.Plants;
import model.entities.plants.PlantsCategory;
import view.gui.Animations;
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

public final class AlmanacFilterPopup extends Popup {

    public enum SortKey { ALPHABETICAL, PROGRESSION, LEVEL, CATEGORY, TOUGHNESS, SUN_COST }

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
        private final Set<PlantsCategory> categories = new LinkedHashSet<PlantsCategory>();
        private String query = "";
        private boolean showLocked = true;
        private boolean showUnlocked = true;

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

        public Set<PlantsCategory> getCategories() {
            return categories;
        }

        public boolean isShowLocked() {
            return showLocked;
        }

        public void setShowLocked(boolean value) {
            showLocked = value;
        }

        public boolean isShowUnlocked() {
            return showUnlocked;
        }

        public void setShowUnlocked(boolean value) {
            showUnlocked = value;
        }

        public void move(int from, int to) {
            if (from < 0 || to < 0 || from >= order.size() || to >= order.size()) {
                return;
            }
            order.add(to, order.remove(from));
        }

        public List<Plants> apply() {
            List<Plants> out = new ArrayList<Plants>();
            for (Plants plant : Plants.values()) {
                if (keeps(plant)) {
                    out.add(plant);
                }
            }
            Collections.sort(out, comparator());
            return out;
        }

        private boolean keeps(Plants plant) {
            PlantRecord r = PlantData.record(plant);
            if (r == null) {
                return false;
            }
            if (!query.isEmpty()
                    && !plant.getName().toLowerCase().contains(query.toLowerCase())) {
                return false;
            }
            if (!categories.isEmpty() && !categories.contains(r.getCategory())) {
                return false;
            }
            if (!chapters.isEmpty() && !chapters.contains(tierOf(r))) {
                return false;
            }
            return true;
        }

        private static String tierOf(PlantRecord r) {
            return r.getChapter() == null ? r.getUnlockKind().name() : r.getChapter().name();
        }

        private Comparator<Plants> comparator() {
            return new Comparator<Plants>() {
                @Override
                public int compare(Plants a, Plants b) {
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

        private static int progression(PlantRecord ra, PlantRecord rb) {
            int cmp = Integer.compare(PlantData.progressionRank(ra),
                    PlantData.progressionRank(rb));
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(ra.getChapterOrder(), rb.getChapterOrder());
            return cmp != 0 ? cmp : Integer.compare(ra.getId(), rb.getId());
        }

        private int compareBy(SortKey key, Plants a, Plants b) {
            PlantRecord ra = PlantData.record(a);
            PlantRecord rb = PlantData.record(b);
            switch (key) {
                case ALPHABETICAL:
                    return a.getName().compareToIgnoreCase(b.getName());
                case PROGRESSION:
                    return progression(ra, rb);
                case LEVEL:
                    return Integer.compare(ra.getChapterOrder(), rb.getChapterOrder());
                case CATEGORY:
                    return ra.getCategory().name().compareTo(rb.getCategory().name());
                case TOUGHNESS:
                    return Integer.compare(a.getBaseHP(), b.getBaseHP());
                case SUN_COST:
                    return Integer.compare(a.getCost(), b.getCost());
                default:
                    return 0;
            }
        }
    }

    private final Rules rules;
    private final Runnable onChange;
    private final Table columns = new Table();

    public AlmanacFilterPopup(GameContext context, Rules rules, Runnable onChange) {
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

        column.add(lockBox("Unlocked", true)).left().row();
        column.add(lockBox("Locked", false)).left().padBottom(Theme.PAD_SMALL).row();

        Label origin = new Label("Origin", ui.skin(), "muted");
        column.add(origin).left().padBottom(2f).row();
        column.add(tierBox("Starter", "STARTER")).left().row();
        for (ChapterType chapter : ChapterType.values()) {
            column.add(tierBox(chapter.getDisplayName(), chapter.name())).left().row();
        }
        column.add(tierBox("Premium", "PREMIUM")).left().row();
        column.add(tierBox("Mint", "MINT")).left().padBottom(Theme.PAD_SMALL).row();

        Label cat = new Label("Category", ui.skin(), "muted");
        column.add(cat).left().padBottom(2f).row();
        for (PlantsCategory value : PlantsCategory.values()) {
            if (value != PlantsCategory.MINT) {
                column.add(categoryBox(value)).left().row();
            }
        }
        return column;
    }

    private CheckBox lockBox(String text, final boolean unlockedSide) {
        CheckBox box = ui.checkBox(text);
        box.setChecked(unlockedSide ? rules.isShowUnlocked() : rules.isShowLocked());
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                boolean value = ((CheckBox) actor).isChecked();
                if (unlockedSide) {
                    rules.setShowUnlocked(value);
                } else {
                    rules.setShowLocked(value);
                }
                notifyChange();
            }
        });
        return box;
    }

    private CheckBox tierBox(String text, final String tier) {
        CheckBox box = ui.checkBox(text);
        box.setChecked(rules.getChapters().contains(tier));
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (((CheckBox) actor).isChecked()) {
                    rules.getChapters().add(tier);
                } else {
                    rules.getChapters().remove(tier);
                }
                notifyChange();
            }
        });
        return box;
    }

    private CheckBox categoryBox(final PlantsCategory value) {
        CheckBox box = ui.checkBox(pretty(value.name()));
        box.setChecked(rules.getCategories().contains(value));
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (((CheckBox) actor).isChecked()) {
                    rules.getCategories().add(value);
                } else {
                    rules.getCategories().remove(value);
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

package database;

import model.quest.QuestDef;
import model.quest.QuestFactory;
import model.quest.QuestProgress;
import model.quest.QuestState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuestRepository {

    private static final Path FILE = Paths.get("quests.json");

    private static String quote(String value) {
        return (value == null) ? "null" : "\"" + Json.escape(value) + "\"";
    }

    public synchronized QuestState load(String username) {
        List<QuestState> all = readAll();
        QuestState found = null;
        for (QuestState s : all) {
            if (username.equals(s.getUsername())) {
                found = s;
                break;
            }
        }
        if (found == null) {
            QuestState fresh = QuestFactory.buildDefault(username);
            all.add(fresh);
            writeAll(all);
            return fresh;
        }
        String today = LocalDate.now().toString();
        if (!today.equals(found.getLastResetDate())) {
            QuestFactory.resetDaily(found);
            writeAll(all);
        }
        return found;
    }

    public synchronized void save(QuestState state) {
        List<QuestState> all = readAll();
        boolean replaced = false;
        for (int i = 0; i < all.size(); i++) {
            if (state.getUsername().equals(all.get(i).getUsername())) {
                all.set(i, state);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            all.add(state);
        }
        writeAll(all);
    }

    private List<QuestState> readAll() {
        List<QuestState> result = new ArrayList<>();
        if (!Files.exists(FILE)) {
            return result;
        }
        try {
            String text = new String(Files.readAllBytes(FILE), StandardCharsets.UTF_8);
            Object parsed = Json.parse(text);
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (item instanceof Map) {
                        result.add(fromMap((Map<?, ?>) item));
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("Could not read quests file: " + e.getMessage());
        }
        return result;
    }

    private void writeAll(List<QuestState> all) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < all.size(); i++) {
            sb.append("  ").append(toJson(all.get(i)));
            if (i < all.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("]\n");
        try {
            Files.write(FILE, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Could not write quests file: " + e.getMessage());
        }
    }

    private QuestState fromMap(Map<?, ?> m) {
        QuestState state = new QuestState();
        state.setUsername(Json.str(m, "username"));
        state.setLastResetDate(Json.str(m, "lastResetDate"));
        List<QuestProgress> quests = new ArrayList<>();
        Object arr = m.get("quests");
        if (arr instanceof List) {
            for (Object item : (List<?>) arr) {
                if (item instanceof Map) {
                    QuestProgress qp = questFromMap((Map<?, ?>) item);
                    if (qp != null) {
                        quests.add(qp);
                    }
                }
            }
        }
        state.setQuests(quests);
        return state;
    }

    private QuestProgress questFromMap(Map<?, ?> m) {
        String defName = Json.str(m, "def");
        QuestDef def;
        try {
            def = QuestDef.valueOf(defName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
        QuestProgress qp = new QuestProgress(def);
        qp.setProgress(Json.doubleOf(m, "progress"));
        qp.setTarget(Json.intOf(m, "target"));
        qp.setCompleted(Json.boolOf(m, "completed"));
        qp.setClaimed(Json.boolOf(m, "claimed"));
        qp.setVarInt(Json.intOf(m, "varInt"));
        qp.setVarStr(Json.str(m, "varStr"));
        return qp;
    }

    private String toJson(QuestState s) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"username\":").append(quote(s.getUsername())).append(',');
        sb.append("\"lastResetDate\":").append(quote(s.getLastResetDate())).append(',');
        sb.append("\"quests\":[");
        List<QuestProgress> quests = s.getQuests();
        for (int i = 0; i < quests.size(); i++) {
            sb.append(questJson(quests.get(i)));
            if (i < quests.size() - 1) {
                sb.append(',');
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    private String questJson(QuestProgress qp) {
        String sb = '{' +
                "\"def\":" + quote(qp.getDef().name()) + ',' +
                "\"progress\":" + qp.getProgress() + ',' +
                "\"target\":" + qp.getTarget() + ',' +
                "\"completed\":" + qp.isCompleted() + ',' +
                "\"claimed\":" + qp.isClaimed() + ',' +
                "\"varInt\":" + qp.getVarInt() + ',' +
                "\"varStr\":" + quote(qp.getVarStr()) +
                '}';
        return sb;
    }
}

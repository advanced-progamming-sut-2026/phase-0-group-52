package database;

import model.User;
import model.entities.plants.PlantCollection;
import model.entities.plants.PlantProgress;
import model.entities.plants.Plants;
import util.Json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserRepository {

    private static final Path FILE = Paths.get("database", "users.json");

    public synchronized boolean register(User user) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getUsername() != null && u.getUsername().equals(user.getUsername())) {
                return false;
            }
        }
        user.setId(nextId(users));
        users.add(user);
        writeAll(users);
        return true;
    }

    public synchronized boolean usernameExists(String username) {
        for (User u : readAll()) {
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public synchronized User getUserByUsername(String username) {
        for (User u : readAll()) {
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public synchronized void setStayLoggedIn(int userId, boolean value) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getId() == userId) {
                u.setStayLoggedIn(value);
            } else if (value) {
                u.setStayLoggedIn(false);
            }
        }
        writeAll(users);
    }

    public synchronized User getRememberedUser() {
        for (User u : readAll()) {
            if (u.isStayLoggedIn()) {
                return u;
            }
        }
        return null;
    }

    public synchronized List<User> getAllUsers() {
        return readAll();
    }

    public synchronized int getPassedLevels(int userId) {
        for (User u : readAll()) {
            if (u.getId() == userId) {
                int chapter = Math.max(1, u.getLastChapter());
                int level = Math.max(1, u.getLastLevel());
                return (chapter - 1) * 4 + (level - 1);
            }
        }
        return 0;
    }

    public synchronized void updatePassword(String username, String passwordHash) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                u.setPasswordHash(passwordHash);
            }
        }
        writeAll(users);
    }

    public synchronized void updateUsername(int userId, String newUsername) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getId() == userId) {
                u.setUsername(newUsername);
            }
        }
        writeAll(users);
    }

    public synchronized void updateNickname(int userId, String newNickname) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getId() == userId) {
                u.setNickname(newNickname);
            }
        }
        writeAll(users);
    }

    public synchronized void updateEmail(int userId, String newEmail) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getId() == userId) {
                u.setEmail(newEmail);
            }
        }
        writeAll(users);
    }

    public synchronized void updateDifficulty(String username, int difficultyLevel) {
        List<User> users = readAll();
        for (User u : users) {
            if (u.getUsername() != null && u.getUsername().equals(username)) {
                u.setDifficultyLevel(difficultyLevel);
            }
        }
        writeAll(users);
    }

    public synchronized boolean delete(int userId) {
        List<User> users = readAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == userId) {
                users.remove(i);
                return writeAll(users, true);
            }
        }
        return false;
    }

    public synchronized boolean updateStats(User user) {
        List<User> users = readAll();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == user.getId()) {
                users.set(i, user);
                writeAll(users);
                return true;
            }
        }
        return false;
    }

    private int nextId(List<User> users) {
        int max = 0;
        for (User u : users) {
            max = Math.max(max, u.getId());
        }
        return max + 1;
    }

    private List<User> readAll() {
        List<User> result = new ArrayList<>();
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
            System.err.println("Could not read users file: " + e.getMessage());
        }
        return result;
    }

    private boolean wouldDestroyData(List<User> users) {
        if (users != null && !users.isEmpty()) {
            return false;
        }
        if (!Files.exists(FILE)) {
            return false;
        }
        try {
            String existing = new String(Files.readAllBytes(FILE), StandardCharsets.UTF_8);
            Object parsed = Json.parse(existing);
            return (parsed instanceof List) && !((List<?>) parsed).isEmpty();
        } catch (IOException | RuntimeException e) {
            return true;
        }
    }

    private void writeAll(List<User> users) {
        writeAll(users, false);
    }

    private boolean writeAll(List<User> users, boolean allowEmpty) {
        if (!allowEmpty && wouldDestroyData(users)) {
            System.err.println("Refused to overwrite " + FILE
                    + " with an empty user list; the existing accounts were kept.");
            return false;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < users.size(); i++) {
            sb.append("  ").append(toJson(users.get(i)));
            if (i < users.size() - 1) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("]\n");
        try {
            Files.createDirectories(FILE.getParent());
            Files.write(FILE, sb.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            System.err.println("Could not write users file: " + e.getMessage());
            return false;
        }
    }

    private String toJson(User u) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        num(sb, "id", u.getId());
        str(sb, "username", u.getUsername());
        str(sb, "passwordHash", u.getPasswordHash());
        str(sb, "nickname", u.getNickname());
        str(sb, "email", u.getEmail());
        str(sb, "gender", u.getGender());
        num(sb, "securityQuestion", u.getSecurityQuestion());
        str(sb, "securityAnswerHash", u.getSecurityAnswerHash());
        num(sb, "coins", u.getCoins());
        num(sb, "gems", u.getGems());
        num(sb, "plantFoodNum", u.getPlantFoodNum());
        num(sb, "mostMeowPoint", u.getMostMeowPoint());
        num(sb, "maxPoint", u.getMaxPoint());
        num(sb, "lastChapter", u.getLastChapter());
        num(sb, "lastLevel", u.getLastLevel());
        num(sb, "gamesPlayed", u.getGamesPlayed());
        num(sb, "miniGamesPlayed", u.getMiniGamesPlayed());
        num(sb, "questDailyNum", u.getQuestDailyNum());
        num(sb, "questNonDailyNum", u.getQuestNonDailyNum());
        str(sb, "lastWonGame", u.getLastWonGame());
        num(sb, "difficultyLevel", u.getDifficultyLevel());
        num(sb, "questMainNum", u.getQuestMainNum());
        num(sb, "questEpicNum", u.getQuestEpicNum());
        num(sb, "gameSpeed", u.getGameSpeed());
        bool(sb, "showGrid", u.isShowGrid());
        bool(sb, "debugMode", u.isDebugMode());
        bool(sb, "uiEditMode", u.isUiEditMode());
        bool(sb, "stayLoggedIn", u.isStayLoggedIn());
        plants(sb, u);

        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append('}');
        return sb.toString();
    }

    private User fromMap(Map<?, ?> m) {
        User u = new User();
        u.setId(intOf(m, "id"));
        u.setUsername(strOf(m, "username"));
        u.setPasswordHash(strOf(m, "passwordHash"));
        u.setNickname(strOf(m, "nickname"));
        u.setEmail(strOf(m, "email"));
        u.setGender(strOf(m, "gender"));
        u.setSecurityQuestion(intOf(m, "securityQuestion"));
        u.setSecurityAnswerHash(strOf(m, "securityAnswerHash"));
        u.setCoins(intOf(m, "coins"));
        u.setGems(intOf(m, "gems"));
        u.setPlantFoodNum(intOf(m, "plantFoodNum"));
        u.setMostMeowPoint(intOf(m, "mostMeowPoint"));
        u.setMaxPoint(intOf(m, "maxPoint"));
        u.setLastChapter(intOf(m, "lastChapter"));
        u.setLastLevel(intOf(m, "lastLevel"));
        u.setGamesPlayed(intOf(m, "gamesPlayed"));
        u.setMiniGamesPlayed(intOf(m, "miniGamesPlayed"));
        u.setQuestDailyNum(intOf(m, "questDailyNum"));
        u.setQuestNonDailyNum(intOf(m, "questNonDailyNum"));
        u.setLastWonGame(strOf(m, "lastWonGame"));
        u.setDifficultyLevel(intOf(m, "difficultyLevel"));
        u.setQuestMainNum(intOf(m, "questMainNum"));
        u.setQuestEpicNum(intOf(m, "questEpicNum"));
        u.setGameSpeed(intOf(m, "gameSpeed"));
        u.setShowGrid(boolOf(m, "showGrid"));
        u.setDebugMode(boolOf(m, "debugMode"));
        u.setUiEditMode(boolOf(m, "uiEditMode"));
        u.setStayLoggedIn(boolOf(m, "stayLoggedIn"));
        readPlants(u, m.get("plants"));
        return u;
    }

    private void plants(StringBuilder sb, User u) {
        StringBuilder rows = new StringBuilder();
        for (Plants plant : Plants.values()) {
            PlantProgress state = u.getPlants().progress(plant);
            boolean boosted = u.getStoredBoosts().contains(plant);
            if (!state.isUnlocked() && state.getPackets() == 0
                    && state.getXp() == 0 && state.getLevel() <= 1 && !boosted) {
                continue;
            }
            if (rows.length() > 0) {
                rows.append(',');
            }
            rows.append("{\"plant\":\"").append(plant.name()).append("\",\"packets\":")
                    .append(state.getPackets()).append(",\"xp\":").append(state.getXp())
                    .append(",\"level\":").append(state.getLevel())
                    .append(",\"unlocked\":").append(state.isUnlocked())
                    .append(",\"boosted\":").append(boosted).append('}');
        }
        sb.append("\"plants\":[").append(rows).append("],");
    }

    private void readPlants(User u, Object raw) {
        if (!(raw instanceof List)) {
            return;
        }
        PlantCollection collection = u.getPlants();
        for (Object item : (List<?>) raw) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) item;
            Plants plant = plantOf(strOf(row, "plant"));
            if (plant == null) {
                continue;
            }
            PlantProgress state = collection.progress(plant);
            state.setPackets(intOf(row, "packets"));
            state.setXp(intOf(row, "xp"));
            state.setLevel(intOf(row, "level"));
            state.setUnlocked(boolOf(row, "unlocked"));
            if (boolOf(row, "boosted")) {
                u.getStoredBoosts().add(plant);
            }
        }
    }

    private static Plants plantOf(String name) {
        if (name == null) {
            return null;
        }
        try {
            return Plants.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void str(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"').append(Json.escape(value)).append('"');
        }
        sb.append(',');
    }

    private void num(StringBuilder sb, String key, int value) {
        sb.append('"').append(key).append("\":").append(value).append(',');
    }

    private void bool(StringBuilder sb, String key, boolean value) {
        sb.append('"').append(key).append("\":").append(value).append(',');
    }

    private static String strOf(Map<?, ?> m, String key) { return Json.str(m, key); }

    private static int intOf(Map<?, ?> m, String key) { return Json.intOf(m, key); }

    private static boolean boolOf(Map<?, ?> m, String key) { return Json.boolOf(m, key); }
}

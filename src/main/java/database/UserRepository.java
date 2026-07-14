package database;

import model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * مخزن کاربران؛ داده را در یک فایلِ خارجیِ JSON نگه می‌دارد تا بین اجراها پایدار بماند.
 *
 * <p>عمداً stateless است: هر متد فایل را تازه می‌خواند و می‌نویسد. چون در برنامه چند نمونه از این
 * کلاس ساخته می‌شود (ثبت‌نام، ورود، App)، این‌طور هیچ نمونه‌ای داده‌ی قدیمیِ درون‌حافظه ندارد.</p>
 *
 * <p>رابطِ عمومی همان امضاهای قبلیِ نسخه‌ی SQLite است تا کنترلرها تغییری نخواهند.</p>
 */
public class UserRepository {

    private static final Path FILE = Paths.get("users.json");

    // ======================================================================
    //  رابط عمومی (هم‌امضا با نسخه‌ی قبلی)
    // ======================================================================

    /** ثبت کاربر جدید؛ اگر نام کاربری تکراری باشد false. id به‌صورت خودکار تخصیص می‌یابد. */
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

    /** تنظیم پرچمِ «stay logged in»؛ در حالت true فقط همین کاربر remembered می‌ماند. */
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

    /** کاربری که باید در شروعِ برنامه به‌طور خودکار وارد شود (اگر باشد). */
    public synchronized User getRememberedUser() {
        for (User u : readAll()) {
            if (u.isStayLoggedIn()) {
                return u;
            }
        }
        return null;
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

    // ======================================================================
    //  خواندن/نوشتنِ فایل
    // ======================================================================

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
            Object parsed = new JsonParser(text).parseValue();
            if (parsed instanceof List) {
                for (Object item : (List<?>) parsed) {
                    if (item instanceof Map) {
                        result.add(fromMap((Map<?, ?>) item));
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // فایلِ خراب/ناقص → لیست خالی برگردانده می‌شود تا برنامه crash نکند.
            System.err.println("Could not read users file: " + e.getMessage());
        }
        return result;
    }

    private void writeAll(List<User> users) {
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
            Files.write(FILE, sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Could not write users file: " + e.getMessage());
        }
    }

    // ======================================================================
    //  نگاشتِ User ↔ JSON
    // ======================================================================

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
        str(sb, "answer", u.getAnswer());
        num(sb, "coins", u.getCoins());
        num(sb, "gems", u.getGems());
        num(sb, "seedPacket", u.getSeedPacket());
        num(sb, "plantFoodNum", u.getPlantFoodNum());
        num(sb, "mostMeowPoint", u.getMostMeowPoint());
        num(sb, "maxPoint", u.getMaxPoint());
        num(sb, "gamesPlayed", u.getGamesPlayed());
        num(sb, "miniGamesPlayed", u.getMiniGamesPlayed());
        str(sb, "lastWonGame", u.getLastWonGame());
        num(sb, "difficultyLevel", u.getDifficultyLevel());
        bool(sb, "stayLoggedIn", u.isStayLoggedIn());
        // حذفِ کاماىِ انتهایی
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
        u.setAnswer(strOf(m, "answer"));
        u.setCoins(intOf(m, "coins"));
        u.setGems(intOf(m, "gems"));
        u.setSeedPacket(intOf(m, "seedPacket"));
        u.setPlantFoodNum(intOf(m, "plantFoodNum"));
        u.setMostMeowPoint(intOf(m, "mostMeowPoint"));
        u.setMaxPoint(intOf(m, "maxPoint"));
        u.setGamesPlayed(intOf(m, "gamesPlayed"));
        u.setMiniGamesPlayed(intOf(m, "miniGamesPlayed"));
        u.setLastWonGame(strOf(m, "lastWonGame"));
        u.setDifficultyLevel(intOf(m, "difficultyLevel"));
        u.setStayLoggedIn(boolOf(m, "stayLoggedIn"));
        return u;
    }

    // ---- کمکی‌های نوشتن ----

    private void str(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"').append(escape(value)).append('"');
        }
        sb.append(',');
    }

    private void num(StringBuilder sb, String key, int value) {
        sb.append('"').append(key).append("\":").append(value).append(',');
    }

    private void bool(StringBuilder sb, String key, boolean value) {
        sb.append('"').append(key).append("\":").append(value).append(',');
    }

    private static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n");  break;
                case '\r': b.append("\\r");  break;
                case '\t': b.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
            }
        }
        return b.toString();
    }

    // ---- کمکی‌های خواندن ----

    private static String strOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v == null) ? null : v.toString();
    }

    private static int intOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return 0;
    }

    private static boolean boolOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v instanceof Boolean) && (Boolean) v;
    }

    // ======================================================================
    //  پارسرِ کوچکِ JSON (بدون وابستگیِ خارجی)
    //  از subsetِ استاندارد پشتیبانی می‌کند: object / array / string / number / true / false / null
    // ======================================================================

    private static final class JsonParser {
        private final String src;
        private int pos;

        JsonParser(String src) {
            this.src = src;
        }

        Object parseValue() {
            skipWhitespace();
            char c = src.charAt(pos);
            switch (c) {
                case '{': return parseObject();
                case '[': return parseArray();
                case '"': return parseString();
                case 't': pos += 4; return Boolean.TRUE;         // true
                case 'f': pos += 5; return Boolean.FALSE;        // false
                case 'n': pos += 4; return null;                 // null
                default:  return parseNumber();
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++;                       // '{'
            skipWhitespace();
            if (src.charAt(pos) == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                pos++;                   // ':'
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char c = src.charAt(pos++);
                if (c == '}') { break; }  // '}' → پایان؛ در غیر این صورت ',' بوده
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;                       // '['
            skipWhitespace();
            if (src.charAt(pos) == ']') { pos++; return list; }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                char c = src.charAt(pos++);
                if (c == ']') { break; }  // ']' → پایان؛ در غیر این صورت ',' بوده
            }
            return list;
        }

        private String parseString() {
            StringBuilder b = new StringBuilder();
            pos++;                       // '"' آغازین
            while (true) {
                char c = src.charAt(pos++);
                if (c == '"') { break; }
                if (c == '\\') {
                    char e = src.charAt(pos++);
                    switch (e) {
                        case '"':  b.append('"');  break;
                        case '\\': b.append('\\'); break;
                        case '/':  b.append('/');  break;
                        case 'n':  b.append('\n'); break;
                        case 't':  b.append('\t'); break;
                        case 'r':  b.append('\r'); break;
                        case 'b':  b.append('\b'); break;
                        case 'f':  b.append('\f'); break;
                        case 'u':
                            b.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                            pos += 4;
                            break;
                        default:   b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
            return b.toString();
        }

        private Double parseNumber() {
            int start = pos;
            while (pos < src.length() && "+-.eE0123456789".indexOf(src.charAt(pos)) >= 0) {
                pos++;
            }
            return Double.parseDouble(src.substring(start, pos));
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }
    }
}

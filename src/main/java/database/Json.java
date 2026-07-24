package database;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {
    }

    public static Object parse(String text) {
        return new Parser(text).parseValue();
    }

    public static String escape(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    b.append("\\\"");
                    break;
                case '\\':
                    b.append("\\\\");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\t':
                    b.append("\\t");
                    break;
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

    public static String str(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v == null) ? null : v.toString();
    }

    public static int intOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v instanceof Number) ? ((Number) v).intValue() : 0;
    }

    public static double doubleOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v instanceof Number) ? ((Number) v).doubleValue() : 0.0;
    }

    public static boolean boolOf(Map<?, ?> m, String key) {
        Object v = m.get(key);
        return (v instanceof Boolean) && (Boolean) v;
    }

    private static final class Parser {
        private final String src;
        private int pos;

        Parser(String src) {
            this.src = src;
        }

        Object parseValue() {
            skipWhitespace();
            char c = src.charAt(pos);
            switch (c) {
                case '{':
                    return parseObject();
                case '[':
                    return parseArray();
                case '"':
                    return parseString();
                case 't':
                    pos += 4;
                    return Boolean.TRUE;
                case 'f':
                    pos += 5;
                    return Boolean.FALSE;
                case 'n':
                    pos += 4;
                    return null;
                default:
                    return parseNumber();
            }
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++;
            skipWhitespace();
            if (src.charAt(pos) == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                pos++;
                map.put(key, parseValue());
                skipWhitespace();
                char c = src.charAt(pos++);
                if (c == '}') {
                    break;
                }
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            skipWhitespace();
            if (src.charAt(pos) == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                char c = src.charAt(pos++);
                if (c == ']') {
                    break;
                }
            }
            return list;
        }

        private String parseString() {
            StringBuilder b = new StringBuilder();
            pos++;
            while (true) {
                char c = src.charAt(pos++);
                if (c == '"') {
                    break;
                }
                if (c == '\\') {
                    char e = src.charAt(pos++);
                    switch (e) {
                        case '"':
                            b.append('"');
                            break;
                        case '\\':
                            b.append('\\');
                            break;
                        case '/':
                            b.append('/');
                            break;
                        case 'n':
                            b.append('\n');
                            break;
                        case 't':
                            b.append('\t');
                            break;
                        case 'r':
                            b.append('\r');
                            break;
                        case 'b':
                            b.append('\b');
                            break;
                        case 'f':
                            b.append('\f');
                            break;
                        case 'u':
                            b.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                            pos += 4;
                            break;
                        default:
                            b.append(e);
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

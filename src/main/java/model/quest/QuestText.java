package model.quest;

public final class QuestText {

    private QuestText() {
    }

    public static String describe(QuestProgress qp) {
        if (qp == null || qp.getDef() == null) {
            return "";
        }
        String text = qp.getDef().getDescription();
        text = text.replace("{t}", String.valueOf(qp.getTarget()));
        text = text.replace("{n}", String.valueOf(qp.getVarInt()));
        text = text.replace("{v}", pretty(qp.getVarStr()));
        return text;
    }

    public static String pretty(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String[] words = raw.toLowerCase().split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}

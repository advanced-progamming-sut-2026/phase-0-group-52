package model.quest;

import model.User;


public class RewardService {

    public void grant(User user, QuestProgress qp) {
        int amount = amountFor(qp);
        switch (qp.getDef().getRewardType()) {
            case COIN:
                user.setCoins(user.getCoins() + amount);
                break;
            case GEM:
                user.setGems(user.getGems() + amount);
                break;
            case SEED_PACKET:
                user.setSeedPacket(user.getSeedPacket() + amount);
                break;
            case PLANT_UNLOCK:
                unlockRandomPlant(user);
                break;
            default:
                break;
        }
    }

    public int amountFor(QuestProgress qp) {
        int amount;
        switch (qp.getDef()) {
            case DAILY_SUN:
                amount = qp.getTarget() / 100;                 // sun/۱۰۰
                break;
            case THRIFTY_HERBIVORE:
                amount = Math.max(0, 20 - qp.getVarInt());     // ۲۰−n
                break;
            default:
                amount = qp.getDef().getRewardAmount();
                break;
        }
        return amount;
    }

    /**
     * بازکردنِ یک گیاهِ تصادفی (پاداشِ Unlockable).
     * TODO(تیم): وابسته به سیستمِ آنلاکِ کالکشن؛ پس از آماده‌شدن، اینجا صدا زده و یک خبر در NewsList ثبت شود.
     */
    private void unlockRandomPlant(User user) {
    }
}

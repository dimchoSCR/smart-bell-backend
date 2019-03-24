package smartbell.restapi;

import org.springframework.stereotype.Component;

@Component
public class BellStatus {

    public class DoNotDisturbStatus {
        private int[] days;

        private boolean inDoNotDisturb;
        private boolean endTomorrow;

        private long startTimeMillis;
        private long endTimeMillis;

        DoNotDisturbStatus() {
            days = null;
            inDoNotDisturb = false;
            endTomorrow = false;
            startTimeMillis = -1;
            endTimeMillis = -1;
        }

        public int[] getDays() {
            return days;
        }

        public void setDays(int[] days) {
            this.days = days;
        }

        public boolean isInDoNotDisturb() {
            return inDoNotDisturb;
        }

        public void setInDoNotDisturb(boolean inDoNotDisturb) {
            this.inDoNotDisturb = inDoNotDisturb;
        }

        public boolean isEndTomorrow() {
            return endTomorrow;
        }

        public void setEndTomorrow(boolean endTomorrow) {
            this.endTomorrow = endTomorrow;
        }

        public long getStartTimeMillis() {
            return startTimeMillis;
        }

        public void setStartTimeMillis(long startTimeMillis) {
            this.startTimeMillis = startTimeMillis;
        }

        public long getEndTimeMillis() {
            return endTimeMillis;
        }

        public void setEndTimeMillis(long endTimeMillis) {
            this.endTimeMillis = endTimeMillis;
        }
    }

    private final DoNotDisturbStatus doNotDisturbStatus;

    BellStatus() {
        this.doNotDisturbStatus = new DoNotDisturbStatus();
    }

    public DoNotDisturbStatus getDoNotDisturbStatus() {
        return doNotDisturbStatus;
    }
}

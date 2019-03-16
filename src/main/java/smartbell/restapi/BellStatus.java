package smartbell.restapi;

import org.springframework.stereotype.Component;

@Component
public class BellStatus {

    public class DoNotDisturbStatus {
        private boolean inDoNotDisturb;
        private boolean endTomorrow;

        private String startTime;
        private String endTime;


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

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
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

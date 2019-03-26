package smartbell.restapi.firebase;

public class BellMessage {

    private NotificationType notificationType;
    private String data;

    public BellMessage(NotificationType notificationType, String data) {
        this.notificationType = notificationType;
        this.data = data;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

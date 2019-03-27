package smartbell.restapi.db.entities;


import java.time.OffsetDateTime;

public class RingEntry {
    private long id;
    private String melodyName;
    private OffsetDateTime dateTime;

    public RingEntry(long id, String melodyName, OffsetDateTime dateTime) {
        this.id = id;
        this.melodyName = melodyName;
        this.dateTime = dateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMelodyName() {
        return melodyName;
    }

    public void setMelodyName(String melodyName) {
        this.melodyName = melodyName;
    }

    public OffsetDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(OffsetDateTime dateTime) {
        this.dateTime = dateTime;
    }
}

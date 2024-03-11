package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class Tuple {

    private String format;
    private boolean locked;
    private int clientId;

    public Tuple(String format) {
        this.format = format;
        this.locked = false;
        this.clientId = -1;
    }

    public String getFormat() {
        return this.format;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isLockedBy(int clientId) {
        return this.locked && this.clientId == clientId;
    }

    public void lock(int clientId) {
        this.clientId = clientId;
        this.locked = true;
    }

    public void unlock() {
        this.clientId = -1;
        this.locked = false;
    }

}

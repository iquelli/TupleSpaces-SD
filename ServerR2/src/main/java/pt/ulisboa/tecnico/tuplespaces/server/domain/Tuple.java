package pt.ulisboa.tecnico.tuplespaces.server.domain;

/*
 * Tuple class, used to manage locks.
 */
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
        this.locked = true;
        this.clientId = clientId;
    }

    public void unlock() {
        this.clientId = -1;
        this.locked = false;
    }

}

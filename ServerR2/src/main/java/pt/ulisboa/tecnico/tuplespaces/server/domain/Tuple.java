package pt.ulisboa.tecnico.tuplespaces.server.domain;

public class Tuple {

    private String format;
    private boolean taken;
    private int clientId;

    public Tuple(String format) {
        this.format = format;
        this.taken = false;
    }

    public String getFormat() {
        return this.format;
    }

}

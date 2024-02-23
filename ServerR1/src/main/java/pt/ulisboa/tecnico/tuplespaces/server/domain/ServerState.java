package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

    private List<String> tuples;

    public ServerState() {
        this.tuples = new ArrayList<String>();
    }

    public void put(String tuple) {
        // TODO: verify tuple is valid
        tuples.add(tuple);
    }

    private String getMatchingTuple(String pattern) {
        // TODO: verify pattern validity
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    public String read(String pattern) {
        return getMatchingTuple(pattern);
    }

    public String take(String pattern) {
        // TODO: take operation
        return null;
    }

    public List<String> getTupleSpacesState() {
        // TODO: getTupleSpacesState operation
        return null;
    }

}

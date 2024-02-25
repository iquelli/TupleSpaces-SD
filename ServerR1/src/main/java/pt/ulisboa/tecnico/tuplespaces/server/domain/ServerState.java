package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InexistantTupleException;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";

    private List<String> tuples;

    public ServerState() {
        this.tuples = new ArrayList<String>();
    }

    public void put(String tuple) {
        if (checkTupleValidity(tuple)) {
            tuples.add(tuple);
        } else {
            throw new InvalidTupleException(tuple);
        }
    }

    private String getMatchingTuple(String pattern) {
        if (!checkTupleValidity(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        throw new InexistantTupleException(pattern);
    }

    public String read(String pattern) {
        return getMatchingTuple(pattern);
    }

    public String take(String pattern) {
        String tuple = getMatchingTuple(pattern);
        tuples.remove(tuple);
        return tuple;
    }

    public List<String> getTupleSpacesState() {
        return tuples;
    }

    private boolean checkTupleValidity(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input
                .endsWith(END_TUPLE) && !input.contains(SPACE);
    }

}

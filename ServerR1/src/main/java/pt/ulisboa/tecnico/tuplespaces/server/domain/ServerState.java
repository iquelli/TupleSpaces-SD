package pt.ulisboa.tecnico.tuplespaces.server.domain;

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
        if (!checkInput(tuple)) {
            throw new InvalidTupleException(tuple);
        }
        synchronized (tuples) {
            tuples.add(tuple);
            // We notify that a new tuple was inserted that could match
            // the pattern.
            tuples.notifyAll();
        }
    }

    private String getMatchingTuple(String pattern) {
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    public String read(String pattern) throws InterruptedException {
        if (!checkInput(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        String tuple = null;
        // We look in the tuple space for the given pattern, and return the
        // first tuple that matches.
        // If there is no tuple that matches the pattern, it waits until it does.
        synchronized (tuples) {
            while ((tuple = getMatchingTuple(pattern)) == null) {
                tuples.wait();
            }
        }
        return tuple;
    }

    public String take(String pattern) throws InterruptedException {
        if (!checkInput(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        String tuple = null;
        synchronized (tuples) {
            while ((tuple = getMatchingTuple(pattern)) == null) {
                tuples.wait();
            }
            // We remove the tuple after waiting for it.
            tuples.remove(tuple);
        }
        return tuple;
    }

    public List<String> getTupleSpacesState() {
        synchronized (tuples) {
            return new ArrayList<String>(tuples);
        }
    }

    private boolean checkInput(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";

    // TODO: probably dont need the Tuple class anymore, so remove it
    private List<Tuple> tuples;

    public ServerState() {
        this.tuples = new ArrayList<Tuple>();
    }

    public void put(String format) throws InvalidTupleException {
        // TODO: implement put state
        // if (!checkFormat(format)) {
        //     throw new InvalidTupleException(format);
        // }

        // synchronized (this.tuples) {
        //     this.tuples.add(new Tuple(format));

        //     // We notify that a new tuple format was inserted that could match
        //     // the pattern.
        //     this.tuples.notifyAll();
        // }
    }

    private String getMatchingTuple(String pattern) {
        for (Tuple tuple : this.tuples) {
            String format = tuple.getFormat();
            if (format.matches(pattern)) {
                return format;
            }
        }
        return null;
    }

    public String read(String pattern) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        // We look in the tuple space for the given pattern, and return the
        // first tuple format that matches.
        // If there is no tuple format that matches the pattern, it waits until it does.
        String tuple = null;
        synchronized (this.tuples) {
            while ((tuple = getMatchingTuple(pattern)) == null) {
                this.tuples.wait();
            }
        }
        return tuple;
    }

    public String take(String pattern) {
        // TODO: implement take state
        return null;
    }

    public List<String> getTupleSpacesState() {
        return this.tuples.stream().map(tuple -> tuple.getFormat()).toList();
    }

    private boolean checkFormat(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

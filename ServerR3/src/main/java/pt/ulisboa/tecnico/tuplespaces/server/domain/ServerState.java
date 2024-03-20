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

    public void put(String format) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(format)) {
            throw new InvalidTupleException(format);
        }

        // TODO: implement put state
        // synchronized (this.tuples) {
        //     this.tuples.add(new Tuple(format));

        //     // We notify that a new tuple format was inserted that could match
        //     // the pattern.
        //     this.tuples.notifyAll();
        // }
    }

    private String getMatchingTuple(String pattern) {
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
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

    public String take(String pattern) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        // TODO: implement take state
        return null;
    }

    public List<String> getTupleSpacesState() {
        return this.tuples;
    }

    private boolean checkFormat(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

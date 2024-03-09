package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";

    private List<Tuple> tuples;

    public ServerState() {
        this.tuples = new ArrayList<Tuple>();
    }

    public void put(String format) {
        if (!checkInput(format)) {
            throw new InvalidTupleException(format);
        }
        synchronized (this.tuples) {
            this.tuples.add(new Tuple(format));
            // We notify that a new tuple was inserted that could match
            // the pattern.
            this.tuples.notifyAll();
        }
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

    public String read(String pattern) throws InterruptedException {
        if (!checkInput(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        String tuple = null;
        // We look in the tuple space for the given pattern, and return the
        // first tuple that matches.
        // If there is no tuple that matches the pattern, it waits until it does.
        synchronized (this.tuples) {
            while ((tuple = getMatchingTuple(pattern)) == null) {
                this.tuples.wait();
            }
        }
        return tuple;
    }

    // TODO: add the take operation to the state
    // public String take(String pattern) throws InterruptedException {
    //     if (!checkInput(pattern)) {
    //         throw new InvalidTupleException(pattern);
    //     }
    //     String tuple = null;
    //     synchronized (tuples) {
    //         while ((tuple = getMatchingTuple(pattern)) == null) {
    //             tuples.wait();
    //         }
    //         // We remove the tuple after waiting for it.
    //         tuples.remove(tuple);
    //     }
    //     return tuple;
    // }

    public List<String> getTupleSpacesState() {
        return this.tuples.stream().map(tuple -> tuple.getFormat()).toList();
    }

    private boolean checkInput(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

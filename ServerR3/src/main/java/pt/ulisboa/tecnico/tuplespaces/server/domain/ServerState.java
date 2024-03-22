package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores the current state of the server.
 */
public class ServerState {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";

    /** Global list of all tuples. **/
    private List<String> tuples;

    /** Stores the sequence number of the take in the waiting queue that can take a tuple. **/
    private Integer canTake;

    /** Maps the sequence number of the waiting take operation to its regex format. **/
    private Map<Integer, String> waitingQueue;

    /** Stores the sequence number of the current operation. **/
    private Integer seqNumber;

    public ServerState() {
        this.tuples = new ArrayList<String>();
        this.canTake = 0;
        this.waitingQueue = new TreeMap<Integer, String>();
        this.seqNumber = 1;
    }

    public synchronized void put(
            String tuple,
            int seqNumber
    ) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(tuple)) {
            throw new InvalidTupleException(tuple);
        }

        while (seqNumber != this.seqNumber) {
            wait();
        }

        for (Map.Entry<Integer, String> entry : waitingQueue.entrySet()) {
            if (tuple.matches(entry.getValue())) {
                this.waitingQueue.put(entry.getKey(), tuple);
                this.canTake = entry.getKey();
                notifyAll();
                return;
            }
        }

        this.tuples.add(tuple);
        ++this.seqNumber;
        notifyAll();
    }

    private String getMatchingTuple(String pattern) {
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    public synchronized String read(
            String pattern
    ) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        // We look in the tuple space for the given pattern, and return the
        // first tuple format that matches.
        // If there is no tuple format that matches the pattern, it waits until it does.
        String tuple = null;
        while ((tuple = getMatchingTuple(pattern)) == null) {
            wait();
        }
        return tuple;
    }

    public synchronized String take(
            String pattern,
            int seqNumber
    ) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        while (seqNumber != this.seqNumber) {
            wait();
        }
        ++this.seqNumber;

        String tuple = null;
        if ((tuple = getMatchingTuple(pattern)) == null) {
            this.waitingQueue.put(seqNumber, pattern);
            while (seqNumber != this.canTake) {
                wait();
            }
            this.canTake = 0;
            tuple = (String) this.waitingQueue.remove(seqNumber);
            ++this.seqNumber;
        } else {
            this.tuples.remove(tuple);
        }

        notifyAll();
        return tuple;
    }

    public synchronized List<String> getTupleSpacesState() {
        return this.tuples;
    }

    private boolean checkFormat(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

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

    /**
     * Inserts a new tuple in the TupleSpace, altering the state of
     * the server.
     *
     * @param tuple     A brand new tuple to insert into the TupleSpace.
     * @param seqNumber The sequence number for this operation.
     **/
    public synchronized void put(
            String tuple,
            int seqNumber
    ) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(tuple)) {
            throw new InvalidTupleException(tuple);
        }

        // Wait for the global sequence number to reach its own number
        while (seqNumber != this.seqNumber) {
            wait();
        }

        for (Map.Entry<Integer, String> entry : waitingQueue.entrySet()) {
            if (tuple.matches(entry.getValue())) {
                // The oldest take that is blocked gets notified there is a tuple
                // that he matches with
                this.waitingQueue.put(entry.getKey(), tuple);
                this.canTake = entry.getKey();
                notifyAll();
                return;
            }
        }

        // If there are no blocked takes, it adds the tuple to the global list
        // and notifies any blocked reads
        synchronized (this.tuples) {
            this.tuples.add(tuple);
            this.tuples.notifyAll();
        }

        // Notifies the next put or take that it can start
        ++this.seqNumber;
        notifyAll();
    }

    /**
     * Looks in the global tuple list for a tuple that matches
     * with its given pattern.
     *
     * @param pattern A regex pattern that matches with tuples.
     * @return A tuple that matches with the pattern, or null if that doesn't exist.
     **/
    private String getMatchingTuple(String pattern) {
        for (String tuple : this.tuples) {
            if (tuple.matches(pattern)) {
                return tuple;
            }
        }
        return null;
    }

    /**
     * Looks in the TupleSpace for the given pattern, and returns
     * the first tuple that matches with that pattern.
     *
     * @param pattern A regex pattern that matches with tuples.
     * @return A tuple that matches with the pattern.
     **/
    public String read(String pattern) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        String tuple = null;
        synchronized (this.tuples) {
            // If there is no tuple that matches the pattern, it waits until
            // there exists at least one
            while ((tuple = getMatchingTuple(pattern)) == null) {
                this.tuples.wait();
            }
        }
        return tuple;
    }

    /**
     * Looks in the TupleSpace for the given pattern, and returns
     * the first tuple that matches with that pattern, removing
     * it from the TupleSpace.
     *
     * @param pattern   A regex pattern that matches with tuples.
     * @param seqNumber The sequence number for this operation.
     * @return A tuple that matches with the pattern and was removed from the TupleSpace.
     **/
    public synchronized String take(
            String pattern,
            int seqNumber
    ) throws InvalidTupleException, InterruptedException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }

        // Wait for the global sequence number to reach its own number
        while (seqNumber != this.seqNumber) {
            wait();
        }
        ++this.seqNumber; // increment the sequence number before getting blocked

        String tuple = null;
        synchronized (this.tuples) {
            if ((tuple = getMatchingTuple(pattern)) != null) {
                // There is a tuple in the global list that he matches with, therefore he takes it
                // and notifies the next put or take that it can start
                this.tuples.remove(tuple);
                notifyAll();
                return tuple;
            }
        }

        // The take inserts itself in the waiting queue and then blocks
        this.waitingQueue.put(seqNumber, pattern);
        while (seqNumber != this.canTake) {
            wait();
        }
        this.canTake = 0;
        // Takes the tuple and removes itself from the waiting queue
        tuple = (String) this.waitingQueue.remove(seqNumber);

        // Notifies the next put or take that it can start
        ++this.seqNumber;
        notifyAll();
        return tuple;
    }

    /**
     * Returns the state of the TupleSpace.
     *
     * @return The global tuple list that represents the TupleSpace.
     **/
    public synchronized List<String> getTupleSpacesState() {
        return this.tuples;
    }

    /**
     * Checks if the given input is valid or not.
     *
     * @param input Input that needs to be checked.
     * @return True if the input is valid, or false if it isn't.
     **/
    private boolean checkFormat(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

package pt.ulisboa.tecnico.tuplespaces.server.domain;

import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidClientIDException;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.InvalidTupleException;
import pt.ulisboa.tecnico.tuplespaces.server.exceptions.TupleNotFoundException;

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

    public void put(String format) throws InvalidTupleException {
        if (!checkFormat(format)) {
            throw new InvalidTupleException(format);
        }

        synchronized (this.tuples) {
            this.tuples.add(new Tuple(format));
            // We notify that a new tuple format was inserted that could match
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

    public List<String> lock(
            String pattern,
            int clientId
    ) throws InvalidClientIDException, InvalidTupleException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        if (clientId < 0) {
            throw new InvalidClientIDException(clientId);
        }

        // We look in the tuple space for all the tuples with the given format, and lock the
        // ones that are possible.
        // Finally we return them in the tupleFormats list.
        List<String> tupleFormats = new ArrayList<>();
        synchronized (this.tuples) {
            for (Tuple tuple : this.tuples) {
                String format = tuple.getFormat();
                if ((!tuple.isLocked() || tuple.isLockedBy(clientId)) && format.matches(pattern) &&
                        !tupleFormats.contains(format)) {
                    tuple.lock(clientId);
                    tupleFormats.add(format);
                }
            }
        }
        return tupleFormats;
    }

    public void release(int clientId) throws InvalidClientIDException {
        if (clientId < 0) {
            throw new InvalidClientIDException(clientId);
        }

        synchronized (this.tuples) {
            // Release every tuple locked by the client with the given id
            for (Tuple tuple : this.tuples) {
                if (tuple.isLockedBy(clientId)) {
                    tuple.unlock();
                }
            }
        }
    }

    public void unlock(
            String pattern,
            int clientId
    ) throws InvalidClientIDException, InvalidTupleException, TupleNotFoundException {
        if (!checkFormat(pattern)) {
            throw new InvalidTupleException(pattern);
        }
        if (clientId < 0) {
            throw new InvalidClientIDException(clientId);
        }

        boolean found = false;
        synchronized (this.tuples) {
            // Unlock every tuple locked by the client with the given id and return
            // the tuple requested by said user
            int size = this.tuples.size();
            for (int i = size - 1; i >= 0; --i) {
                Tuple tuple = this.tuples.get(i);
                if (tuple.isLockedBy(clientId)) {
                    if (tuple.getFormat().matches(pattern)) {
                        this.tuples.remove(i);
                        found = true;
                    }
                    tuple.unlock();
                }
            }
        }

        if (!found) {
            throw new TupleNotFoundException(pattern, clientId);
        }
    }

    public List<String> getTupleSpacesState() {
        return this.tuples.stream().map(tuple -> tuple.getFormat()).toList();
    }

    private boolean checkFormat(String input) {
        return input.length() >= 3 && input.startsWith(BGN_TUPLE) && input.endsWith(END_TUPLE) &&
                !input.contains(SPACE);
    }

}

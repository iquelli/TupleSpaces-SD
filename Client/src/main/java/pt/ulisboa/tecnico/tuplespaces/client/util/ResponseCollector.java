package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {

    List<String> responses;

    public ResponseCollector() {
        responses = new ArrayList<String>();
    }

    synchronized public void addResponse(String s) {
        responses.add(s);
        notifyAll();
    }

    synchronized public String getResponse() {
        if (this.isEmpty()) {
            return "";
        }
        return responses.get(0);
    }

    synchronized public List<String> getResponses() {
        return this.responses;
    }

    synchronized public void setResponses(List<String> responses) {
        this.responses = responses;
    }

    synchronized public boolean isEmpty() {
        return this.responses.isEmpty();
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (responses.size() < n) {
            wait();
        }
    }

}

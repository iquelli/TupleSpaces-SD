package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {

    List<String> responses;
    int nResponses;

    public ResponseCollector() {
        responses = new ArrayList<String>();
        nResponses = 0;
    }

    synchronized public void addResponse(String s) {
        responses.add(s);
        ++nResponses;
        notifyAll();
    }

    synchronized public void addAllResponses(List<String> responses) {
        this.responses.addAll(responses);
        ++nResponses;
        notifyAll();
    }

    synchronized public void intersectResponses(List<String> responses)  {
        this.responses.retainAll(responses);
        ++nResponses;
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

    synchronized public void clearResponses() {
        this.responses.clear();
        nResponses = 0;
    }

    synchronized public boolean isEmpty() {
        return this.responses.isEmpty();
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (nResponses < n) {
            wait();
        }
    }

}

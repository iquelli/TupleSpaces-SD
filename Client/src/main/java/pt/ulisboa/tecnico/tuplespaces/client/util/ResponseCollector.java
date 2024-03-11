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
        if (responses.isEmpty()) {
            return "";
        }
        return responses.get(0);
    }

    synchronized public List<String> getResponses() {
        return this.responses;
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (responses.size() < n) {
            wait();
        }
    }

}

package pt.ulisboa.tecnico.tuplespaces.client.util;

import java.util.ArrayList;

public class ResponseCollector {

    ArrayList<String> readResponses;
    ArrayList<String> putResponses;

    public ResponseCollector() {
        readResponses = new ArrayList<String>();
        putResponses = new ArrayList<String>();
    }

    synchronized public void addReadString(String s) {
        readResponses.add(s);
        notifyAll();
    }

    synchronized public void addPutString(String s) {
        putResponses.add(s);
        notifyAll();
    }

    synchronized public String getReadStrings() {
        String res = new String();
        for (String s : readResponses) {
            res = res.concat(s);
        }
        return res;
    }

    synchronized public String getPutStrings() {
        String res = new String();
        for (String s : putResponses) {
            res = res.concat(s);
        }
        return res;
    }

    synchronized public void waitUntilAllReadReceived(int n) throws InterruptedException {
        while (readResponses.size() < n)
            wait();
    }

    synchronized public void waitUntilAllPutReceived(int n) throws InterruptedException {
        while (putResponses.size() < n)
            wait();
    }

}

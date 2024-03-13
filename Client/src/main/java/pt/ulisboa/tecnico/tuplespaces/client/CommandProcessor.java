package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;
import pt.ulisboa.tecnico.tuplespaces.common.Logger;

import java.util.List;
import java.util.Scanner;

public class CommandProcessor {

    private static final String SPACE = " ";
    private static final String BGN_TUPLE = "<";
    private static final String END_TUPLE = ">";
    private static final String PUT = "put";
    private static final String READ = "read";
    private static final String TAKE = "take";
    private static final String SLEEP = "sleep";
    private static final String SET_DELAY = "setdelay";
    private static final String EXIT = "exit";
    private static final String GET_TUPLE_SPACES_STATE = "getTupleSpacesState";
    private static final String GET_TSS = "getTSS";

    private final ClientService clientService;

    public CommandProcessor(ClientService clientService) {
        this.clientService = clientService;
    }

    void parseInput() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                scanner.close();
                return;
            }
            String line = scanner.nextLine().trim();
            String[] split = line.split(SPACE);

            try {
                switch (split[0]) {
                    case PUT:
                        this.put(split);
                        break;
                    case READ:
                        this.read(split);
                        break;
                    case TAKE:
                        this.take(split);
                        break;
                    case GET_TUPLE_SPACES_STATE:
                    case GET_TSS:
                        this.getTupleSpacesState(split);
                        break;
                    case SLEEP:
                        this.sleep(split);
                        break;
                    case SET_DELAY:
                        this.setdelay(split);
                        break;
                    case EXIT:
                        scanner.close();
                        return;
                    default:
                        this.printUsage();
                        break;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                    Logger.error("Failed to establish connection to server");
                } else if (e.getStatus().getDescription() != null) {
                    Logger.error(e.getStatus().getDescription());
                } else {
                    Logger.error("gRCP Error: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Logger.error("An unexpected error occurred");
            } catch (Exception e) {
                Logger.error("An unexpected error occurred: " + e.getMessage());
            }
            System.out.print("\n");
        }
    }

    private void put(String[] split) throws InterruptedException {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];
        // put the tuple
        clientService.put(tuple);
        System.out.println("OK");
    }

    private void read(String[] split) throws InterruptedException {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];
        // read the tuple
        String responseTuple = clientService.read(tuple);
        System.out.println("OK");
        System.out.println(responseTuple);
    }

    private void take(String[] split) throws InterruptedException {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        // take the tuple
        String responseTuple = clientService.take(tuple);
        System.out.println("OK");
        System.out.println(responseTuple);
    }

    private void getTupleSpacesState(String[] split) {
        if (split.length != 2) {
            this.printUsage();
            return;
        }
        String qualifier = split[1];

        // get the tuple spaces state
        List<String> tupleList = clientService.getTupleSpacesState(qualifier);
        System.out.println("OK");

        if (tupleList != null && !tupleList.isEmpty()) {
            System.out.println(tupleList);
        } else {
            System.out.println("[]");
        }
    }

    private void sleep(String[] split) {
        if (split.length != 2) {
            this.printUsage();
            return;
        }
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }

        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setdelay(String[] split) {
        if (split.length != 3) {
            this.printUsage();
            return;
        }
        int qualifier = indexOfServerQualifier(split[1]);
        if (qualifier == -1) {
            Logger.error("Invalid server qualifier");
            return;
        }
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }

        // register delay <time> for when calling server <qualifier>
        this.clientService.setDelay(qualifier, time);
        System.out.println("");
    }

    private void printUsage() {
        System.out.println(
                "Usage:\n" + "- put <element[,more_elements]>\n" +
                        "- read <element[,more_elements]>\n" +
                        "- take <element[,more_elements]>\n" +
                        "- getTupleSpacesState <server>\n" +
                        "- sleep <integer>\n" +
                        "- setdelay <server> <integer>\n" + "- exit"
        );
    }

    private int indexOfServerQualifier(String qualifier) {
        switch (qualifier) {
            case "A":
                return 0;
            case "B":
                return 1;
            case "C":
                return 2;
            default:
                return -1;
        }
    }

    private boolean inputIsValid(String[] input) {
        if (input.length < 2 || input[1].length() < 3 || !input[1].startsWith(BGN_TUPLE) ||
                !input[1].endsWith(END_TUPLE) || input.length > 2) {
            System.out.println(
                    "The tuple/pattern inserted is invalid. A valid tuple has the format <element[,more_elements]> with no spaces."
            );
            return false;
        } else {
            return true;
        }
    }

}

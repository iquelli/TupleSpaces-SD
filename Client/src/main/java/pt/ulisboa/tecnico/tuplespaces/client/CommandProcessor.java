package pt.ulisboa.tecnico.tuplespaces.client;

import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.tuplespaces.client.grpc.ClientService;

import java.util.Scanner;

import com.google.protobuf.ProtocolStringList;

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

    private final ClientService clientService;

    public CommandProcessor(ClientService clientService) {
        this.clientService = clientService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
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
                        this.getTupleSpacesState(split);
                        break;
                    case SLEEP:
                        this.sleep(split);
                        break;
                    case SET_DELAY:
                        this.setdelay(split);
                        break;
                    case EXIT:
                        exit = true;
                        break;
                    default:
                        this.printUsage();
                        break;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getDescription() != null) {
                    System.err.println(
                            "gRCP Error: " + e.getStatus().getDescription()
                    );
                } else {
                    System.err.println("gRCP Error: " + e.getMessage());
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }


    private void put(String[] split) {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        // put the tuple
        clientService.put(tuple);
        System.out.println("Ok");
    }

    private void read(String[] split) {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        // read the tuple
        String responseTuple = clientService.read(tuple);
        System.out.println("Ok");
        System.out.println(responseTuple);
    }


    private void take(String[] split) {
        // check if input is valid
        if (!this.inputIsValid(split)) {
            this.printUsage();
            return;
        }

        // get the tuple
        String tuple = split[1];

        // take the tuple
        String responseTuple = clientService.take(tuple);
        System.out.println("Ok");
        System.out.println(responseTuple);
    }

    private void getTupleSpacesState(String[] split) {
        if (split.length != 2) {
            this.printUsage();
            return;
        }
        String qualifier = split[1];

        // get the tuple spaces state
        ProtocolStringList tupleList = clientService.getTupleSpacesState(qualifier);
        System.out.println("Ok");
        for (String tuple : tupleList) {
            System.out.println(tuple);
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
        String qualifier = split[1];
        Integer time;

        // checks if input String can be parsed as an Integer
        try {
            time = Integer.parseInt(split[2]);
        } catch (NumberFormatException e) {
            this.printUsage();
            return;
        }

        // register delay <time> for when calling server <qualifier>
        System.out.println(
                "TODO: implement setdelay command (only needed in phases 2+3)"
        );
    }

    private void printUsage() {
        System.out.println(
                "Usage:\n" + "- put <element[,more_elements]>\n" +
                        "- read <element[,more_elements]>\n" +
                        "- take <element[,more_elements]>\n" +
                        "- getTupleSpacesState <server>\n" +
                        "- sleep <integer>\n" +
                        "- setdelay <server> <integer>\n" + "- exit\n"
        );
    }

    private boolean inputIsValid(String[] input) {
        if (input.length < 2 || !input[1].startsWith(BGN_TUPLE) || !input[1]
                .endsWith(END_TUPLE) || input.length > 2
        ) {
            this.printUsage();
            return false;
        } else {
            return true;
        }
    }

}

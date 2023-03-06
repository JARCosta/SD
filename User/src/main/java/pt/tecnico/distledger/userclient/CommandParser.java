package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;

import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;

    public CommandParser(UserService userService) {
        this.userService = userService;
    }

    void parseInput(UserServiceGrpc.UserServiceBlockingStub stub) {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line, stub);
                        break;

                    case DELETE_ACCOUNT:
                        this.deleteAccount(line, stub);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line, stub);
                        break;

                    case BALANCE:
                        this.balance(line, stub);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
    }

    private void createAccount(String line, UserServiceGrpc.UserServiceBlockingStub stub){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        // Integer result = 
        stub.createAccount(CreateAccountRequest.newBuilder().setUserId(username).build());
        // System.out.println("TODO: implement createAccount command");
    }

    private void deleteAccount(String line, UserServiceGrpc.UserServiceBlockingStub stub){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        stub.deleteAccount(DeleteAccountRequest.newBuilder().setUserId(username).build());
        // System.out.println("TODO: implement deleteAccount command");
    }


    private void balance(String line, UserServiceGrpc.UserServiceBlockingStub stub){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        stub.balance(BalanceRequest.newBuilder().setUserId(username).build());
        // System.out.println("TODO: implement balance command");
    }

    private void transferTo(String line, UserServiceGrpc.UserServiceBlockingStub stub){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        stub.transferTo(TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build());
        // System.out.println("TODO: implement transferTo command");
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- deleteAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}

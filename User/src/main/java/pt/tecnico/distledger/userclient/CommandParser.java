package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.NamingServerService;
import pt.tecnico.distledger.userclient.grpc.UserService;
import java.util.*;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private UserService readUserService;
    private UserService writeUserService;

    public CommandParser() {
        this.readUserService = new UserService(lookup("B").get(0));
        this.writeUserService = new UserService(lookup("A").get(0));
        if (readUserService == null || writeUserService == null ) {
            Debug.debug("No servers with the name and/or qualifier");
            System.exit(0);
        }
    }

    public static List<String> lookup(String qualifier){

        String host = "localhost";
        int namingServerPort = 5001;
        NamingServerService namingServerService = new NamingServerService(host, namingServerPort);

        String serviceName = "DistLedgerServerService";
        List<String> servers = namingServerService.lookup(serviceName, qualifier);

        return servers;
    }

    public UserService getUserService(String server){
        if(server.equals("B"))
            return readUserService;
        else 
            return writeUserService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case DELETE_ACCOUNT:
                        this.deleteAccount(line);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
                        break;

                    case HELP:
                        this.printUsage();
                        break;

                    case EXIT:
                        exit = true;
                        break;

                    default:
                        System.err.println("Invalid command");
                        break;
                }
            }
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
		// A Channel should be shutdown before stopping the process.
        writeUserService.shutdownNowChannel();
        readUserService.shutdownNowChannel();
    }

    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        Debug.debug("Asking server '" + server +
                "' to create account with username '" + username + "'...");
        UserService userService = getUserService(server);
        try{
            userService.createAccount(username);
        }catch (Exception e){
            userService = new UserService(lookup(server).get(0));
            userService.createAccount(username);
        }
        Debug.debug("Server completed the create account operation.");
    }

    private void deleteAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        Debug.debug("Asking server '" + server +
                "' to delete account with username '" + username + "'...");
        UserService userService = getUserService(server);
        try{
            userService.deleteAccount(username);
        }catch (Exception e){
            userService = new UserService(lookup(server).get(0));
            userService.deleteAccount(username);
        }
        Debug.debug("Server completed the delete account operation.");
    }


    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        Debug.debug("Asking server '" + server + "' to see the balance of '" + username + "'...");
        UserService userService = getUserService(server);
        try{
            userService.balance(username);
        } catch (Exception e){
            userService = new UserService(lookup(server).get(0));
            userService.balance(username);
        }
        Debug.debug("Server completed the balance operation.");
    }

    private void transferTo(String line){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        Debug.debug("Asking server '" + server + "' to transfer " + amount +
                " from '" + from + "' to '" + dest + "...");
        try{
        writeUserService.transferTo(from, dest, amount);
        }catch (Exception e){
            writeUserService = new UserService(lookup(server).get(0));
            writeUserService.transferTo(from, dest, amount);
        }
        Debug.debug("Server completed the transfer to operation.");
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

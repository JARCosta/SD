package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.grpc.NamingServerService;
import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String DELETE = "delete";
    private static final String EXIT = "exit";
    private final NamingServerService namingServerService;

    public CommandParser(NamingServerService namingServerService) {
        this.namingServerService = namingServerService;
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
                    case DELETE:
                        this.delete(line);
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
    }

    private void delete(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String serviceName = split[1];
        String address = split[2];

        Debug.debug("Asking server '" + serviceName +
                "' to delete...");
        namingServerService.delete(serviceName, address);
        Debug.debug("Server completed the delete operation.");
    }

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- delete <serviceName> <address>\n" + 
                        "- exit\n");
    }
}

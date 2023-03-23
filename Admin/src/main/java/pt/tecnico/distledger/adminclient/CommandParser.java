package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.tecnico.distledger.adminclient.grpc.NamingServerService;

import java.util.*;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private AdminService readAdminService;
    private AdminService writeAdminService;

    public CommandParser() {
        this.readAdminService = new AdminService(lookup("B").get(0));
        this.writeAdminService = new AdminService(lookup("A").get(0));
        if (readAdminService == null || writeAdminService == null ) {
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

    public AdminService getUserService(String server){
        if(server.equals("B"))
            return readAdminService;
        else 
            return writeAdminService;
    }

    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            switch (cmd) {
                case ACTIVATE:
                    this.activate(line);
                    break;

                case DEACTIVATE:
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    this.dump(line);
                    break;

                case GOSSIP:
                    this.gossip(line);
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
        // A Channel should be shutdown before stopping the process.
        readAdminService.shutdownNowChannel();
        writeAdminService.shutdownNowChannel();

    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        Debug.debug("Asking server '" + server + "' to activate...");
        AdminService adminService = getUserService(server);
        try{
            adminService.activate();
        }catch (Exception e){
            adminService = new AdminService(lookup(server).get(0));
            adminService.activate();
        }
        Debug.debug("Server completed the activate operation.");
    }

    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        Debug.debug("Asking server '" + server + "' to deactivate...");
        AdminService adminService = getUserService(server);
        try{
            adminService.deactivate();
        } catch (Exception e){
            adminService = new AdminService(lookup(server).get(0));
            adminService.deactivate();
        }
        Debug.debug("Server completed the deactivate operation.");
    }

    private void dump(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        Debug.debug("Asking server '" + server + "' to get the server state...");
        AdminService adminService = getUserService(server);
        try{
            adminService.getLedgerState();
        } catch (Exception e){
            adminService = new AdminService(lookup(server).get(0));
            adminService.getLedgerState();
        }
        Debug.debug("Server completed the get server state operation.");
    }

    @SuppressWarnings("unused")
    private void gossip(String line){
        /* TODO Phase-3 */
        System.out.println("TODO: implement gossip command (only for Phase-3)");
    }
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}

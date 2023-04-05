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


    public CommandParser() {
    }

    public static List<String> lookup(String qualifier){

        String host = "localhost";
        int namingServerPort = 5001;
        NamingServerService namingServerService = new NamingServerService(host, namingServerPort);

        String serviceName = "DistLedgerServerService";
        List<String> servers = namingServerService.lookup(serviceName, qualifier);
        namingServerService.shutdownNowChannel();
        return servers;
    }

    public AdminService getAdminService(String server){
        for (String ip : lookup(server)) {
            try{
                AdminService userService = new AdminService(ip);
                return userService;
            }catch (Exception e){
                System.err.println("Server not found");
            }
        }
        return null;
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
    }

    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        Debug.debug("Asking server '" + server + "' to activate...");
        AdminService adminService = getAdminService(server);
        while (true){
            try{
                adminService.activate();
                adminService.shutdownNowChannel();
                break;
            } catch (Exception e){
                adminService = getAdminService(server);
            }
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
        AdminService adminService = getAdminService(server);
        while (true){
            try{
                adminService.deactivate();
                adminService.shutdownNowChannel();
                break;
            } catch (Exception e){
                adminService = getAdminService(server);
            }
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
        AdminService adminService = getAdminService(server);
        while (true){
            try{
                adminService.getLedgerState();
                adminService.shutdownNowChannel();
                break;
            } catch (Exception e){
                adminService = getAdminService(server);
            }
        }
        Debug.debug("Server completed the get server state operation.");
    }

    @SuppressWarnings("unused")
    private void gossip(String line){
        String[] split = line.split(SPACE);
        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        AdminService adminService = getAdminService(server);
        while (true){
            try{
                adminService.gossip();
                adminService.shutdownNowChannel();
                break;
            } catch (Exception e){
                adminService = getAdminService(server);
            }
        }
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

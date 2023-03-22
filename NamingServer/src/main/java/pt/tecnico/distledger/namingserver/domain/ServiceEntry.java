package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.*;
import java.util.*;

public class ServiceEntry {

    private String serviceName;
    private List<ServerEntry> servers;

    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
        this.servers = new ArrayList<ServerEntry>();
    }

    public void addServerEntry(String address, String qualifier) {
        ServerEntry serverEntry = new ServerEntry(address, qualifier);
        servers.add(serverEntry);

        Debug.debug("Server Entry: " + serverEntry.toString());
        Debug.debug("Number of Servers: " + servers.size());
    }

/*    public void setServers(List<ServerEntry> servers) {
        this.servers = servers;
    }
*/
/*    public void removeServerEntry(ServerEntry serverEntry) {
        servers.remove(server);
    }
*/
    public List<String> getServers() {
        List<String> res = new ArrayList<String>();

        for (ServerEntry serverEntry : servers) {
            res.add(serverEntry.getAddress());
        }

        return res;
    }

    public List<String> getServers(String qualifier) {
        List<String> res = new ArrayList<String>();

            for (ServerEntry serverEntry : servers) {
                if (serverEntry.getQualifier().equals(qualifier))
                    res.add(serverEntry.getAddress());
            }

        return res;
    }



}
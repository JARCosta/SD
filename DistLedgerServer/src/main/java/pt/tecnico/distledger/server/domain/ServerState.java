package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Set<String> users = new HashSet<>();
    // private Set<String, Integer> users = new HashSet<>();

    public ServerState() {
        this.ledger = new ArrayList<>();
    }

    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

    public Integer createAccount(String userId) {
        ledger.add(new CreateOp(userId));
        users.add(userId);
        return 1;
    }

    public boolean accountExists(String userId) {
        return users.contains(userId);
    }

    public Integer deleteAccount(String userId) {
        ledger.add(new DeleteOp(userId));
        users.remove(userId);
        return 1;
    }

    public Integer transferTo(String from, String dest, Integer amount) {
        ledger.add(new TransferOp(from, dest, amount));
        return 1;
    }

    @Override
    public String toString() {
        String ret = "ledgerState {\n";
        for(Operation op : ledger){
            ret += op.toString();
        }
        ret += "}";
        return ret;
    }

}

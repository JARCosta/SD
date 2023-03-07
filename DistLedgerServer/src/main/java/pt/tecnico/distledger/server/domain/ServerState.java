package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Map<String, Integer> accounts = new HashMap<>();

    // Dictionary<String, Integer> accounts = new Hashtable<>();

    public ServerState() {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
    }

    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

    public Integer getBalance(String userId) {
        // if(!accountExists(userId)) return -1;
        return accounts.get(userId);
    }

    public Integer createAccount(String userId) {
        if(accountExists(userId)) return -1;
        ledger.add(new CreateOp(userId));
        accounts.put(userId, 0);
        return 0;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public Integer deleteAccount(String userId) {
        if(getBalance(userId) != 0) return -1;
        ledger.add(new DeleteOp(userId));
        accounts.remove(userId);
        return 0;
    }

    public Integer transferTo(String from, String dest, Integer amount) {
        if(!(accountExists(from) && accountExists(dest))) return -1;
        ledger.add(new TransferOp(from, dest, amount));
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(dest, accounts.get(dest) + amount);
        return 0;
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

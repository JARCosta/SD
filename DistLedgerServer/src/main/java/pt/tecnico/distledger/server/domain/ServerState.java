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
    public boolean isServerActive;

    // Dictionary<String, Integer> accounts = new Hashtable<>();

    public ServerState() {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
    }

    public Integer activate(String qualifier){
        if(isServerActive) return -1;
        this.isServerActive = true;
        return 0;
    }

    public Integer deactivate(String qualifier){
        if(!isServerActive) return -1;
        this.isServerActive = false;
        return 0;
    }

    public List<Operation> getOperations() {
        return ledger;
    }

    public Integer getBalance(String userId) {
        if(!isServerActive) return -1;
        else if(!accountExists(userId)) return -2;
        return accounts.get(userId);
    }

    public Integer createAccount(String userId) {
        if(!isServerActive) return -1;
        else if(accountExists(userId)) return -2;
        ledger.add(new CreateOp(userId));
        accounts.put(userId, 0);
        return 0;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public Integer deleteAccount(String userId) {
        if(!isServerActive) return -1;
        else if(!accountExists(userId)) return -2;
        else if(getBalance(userId) != 0) return -3;
        ledger.add(new DeleteOp(userId));
        accounts.remove(userId);
        return 0;
    }

    public Integer transferTo(String from, String dest, Integer amount) {
        if(!isServerActive) return -1;
        else if(!(accountExists(from) && accountExists(dest))) return -2;
        else if(from.equals(dest)) return -3;
        else if(amount < 0) return -4;
        else if(getBalance(from) < amount) return -5;
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

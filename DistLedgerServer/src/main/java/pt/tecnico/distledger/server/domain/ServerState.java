package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.Debug;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Map<String, Integer> accounts = new HashMap<>();
    public boolean isServerActive;
    public NamingServerService namingServerService;
    private String serviceName;
    private String qualifier;
    private List<String> neighbours;

    public ServerState(NamingServerService namingServerService, String serviceName, String qualifier) {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.namingServerService = namingServerService;
        this.serviceName = serviceName;
        this.qualifier = qualifier;
    }

    private List<String> updateNeighbours() {
        // TODO: lookup is currently returning this server ip as well
        this.neighbours = namingServerService.lookup(this.serviceName, "");
        Debug.debug("neighbours: " + this.neighbours);
        return this.neighbours;
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
        CreateOp op = new CreateOp(userId);
                
        ledger.add(op);
        gossip();
        accounts.put(userId, 0);
        return 0;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public Integer transferTo(String from, String dest, Integer amount) {
        if(!isServerActive) return -1;
        else if(!(accountExists(from) && accountExists(dest))) return -2;
        else if(from.equals(dest)) return -3;
        else if(amount <= 0) return -4;
        else if(getBalance(from) < amount) return -5;
        TransferOp op = new TransferOp(from, dest, amount);
        
        ledger.add(op);
        gossip();
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(dest, accounts.get(dest) + amount);
        return 0;
    }

    
    public Integer gossip(){
        if(!isServerActive) return -1;
        this.neighbours = updateNeighbours();
        for(String neighbour : neighbours){
            System.out.println("propagating to " + neighbour);
            DistLedgerCrossServerService distLedgerCrossServerService = new DistLedgerCrossServerService(neighbour);
            distLedgerCrossServerService.propagateState(getLedgerState());
        }
        return 0;
    }


    public void receiveOperation(DistLedgerCommonDefinitions.Operation op){
        if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT){
            CreateOp createOp = new CreateOp(op.getUserId());
            ledger.add(createOp);
            accounts.put(createOp.getAccount(), 0);
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO){
            TransferOp transferOp = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount());
            ledger.add(transferOp);
            accounts.put(transferOp.getAccount(), accounts.get(transferOp.getAccount()) - transferOp.getAmount());
            accounts.put(transferOp.getDestAccount(), accounts.get(transferOp.getDestAccount()) + transferOp.getAmount());
        }
    }

    public Integer updateServerState(LedgerState ledgerState){
        if(ledgerState.getLedgerCount() <= ledger.size()){ // TODO: verify if its < or <= and if its even compared by the size
            return -1;
        }
        for(int i = ledger.size(); i < ledgerState.getLedgerCount(); i++){
            receiveOperation(ledgerState.getLedger(i));
        }
        return 0;
    }

    public LedgerState getLedgerState(){
        DistLedgerCommonDefinitions.LedgerState.Builder ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder();
        for(Operation op : ledger){
            ledgerState.addLedger(operationToDistOperation(op));
        }
        return ledgerState.build();
    }

    public DistLedgerCommonDefinitions.Operation operationToDistOperation(Operation op){
        if(op instanceof CreateOp){
            return DistLedgerCommonDefinitions.Operation.newBuilder()
            .setType(DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT)
            .setUserId(op.getAccount())
            .build();
        } else if(op instanceof TransferOp){
            return DistLedgerCommonDefinitions.Operation.newBuilder()
            .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
            .setUserId(op.getAccount())
            .setDestUserId(((TransferOp) op).getDestAccount())
            .setAmount(((TransferOp) op).getAmount())
            .build();
        }
        return null;
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

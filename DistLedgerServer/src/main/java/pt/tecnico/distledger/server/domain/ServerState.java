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

import static io.grpc.Status.*;

public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Map<String, Integer> accounts = new HashMap<>();
    public boolean isServerActive;
    public NamingServerService namingServerService;
    private String serviceName;
    private List<Integer> valueTS;
    private List<Integer> replicaTS;
    private String qualifier;

    public ServerState(NamingServerService namingServerService, String serviceName, String qualifier) {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.namingServerService = namingServerService;
        this.serviceName = serviceName;

        this.valueTS = new ArrayList<Integer>(2);
        this.replicaTS = new ArrayList<Integer>(2);
        this.valueTS.add(0);
        this.valueTS.add(0);
        this.replicaTS.add(0);
        this.replicaTS.add(0);
        this.qualifier = qualifier;
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
        if(!isServerActive) throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(!accountExists(userId)) throw new RuntimeException(NOT_FOUND.withDescription("User not found").asRuntimeException());        
        //TODO: for each op that the server is behind, update the server ledger(add op to server)

        // se n estiver up to date, ent gossip()
        // if prevTS > valueTS
        gossip();
        return accounts.get(userId);
    }

    public List<Integer> createAccount(String userId, List<Integer> prevTS) throws RuntimeException {

        if(!isServerActive)
            throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(accountExists(userId))
            throw new RuntimeException(ALREADY_EXISTS.withDescription("User already exists").asRuntimeException());

        if(qualifier.equals("A")) {

            Debug.debug("replicaTS = " + replicaTS);
            replicaTS.set(0, replicaTS.get(0) + 1);
            Debug.debug("replicaTS = " + replicaTS);

        }
        if(qualifier.equals("B")) {

            Debug.debug("replicaTS = " + replicaTS);
            replicaTS.set(1, replicaTS.get(1) + 1);
            Debug.debug("replicaTS = " + replicaTS);

        }

        // TODO add TS to operation
        CreateOp op = new CreateOp(userId);
        ledger.add(op);

        // if prevTS <= valueTS
        Debug.debug("prevTS = " + prevTS);
        Debug.debug("valueTS = " + valueTS);
        if(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1)) {
            // operacao executada
            accounts.put(userId, 0);
            // TODO set operation to stable
            Debug.debug("Operation is stable");

            valueTS = replicaTS;
        }

        return replicaTS;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public List<Integer> transferTo(String from, String dest, Integer amount) {
        if(!isServerActive) throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(!(accountExists(from) && accountExists(dest))) throw new RuntimeException(NOT_FOUND.withDescription("User not found").asRuntimeException());
        else if(from.equals(dest)) throw new RuntimeException(INVALID_ARGUMENT.withDescription("Can't transfer to same account").asRuntimeException());
        else if(amount <= 0) throw new RuntimeException(INVALID_ARGUMENT.withDescription("Invalid amount").asRuntimeException());
        else if(getBalance(from) < amount) throw new RuntimeException(INVALID_ARGUMENT.withDescription("Not enough balance").asRuntimeException());
        
        TransferOp op = new TransferOp(from, dest, amount);
        
        ledger.add(op);
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(dest, accounts.get(dest) + amount);
        return replicaTS;
    }

    // gossip sends to all neighbours including itself
    public Integer gossip(){
        if(!isServerActive) return -1;
        List<String> neighbours = namingServerService.lookup(this.serviceName, "");
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

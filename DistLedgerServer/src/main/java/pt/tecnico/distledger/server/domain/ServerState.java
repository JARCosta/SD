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

    public List<Integer> getBalance(String userId, List<Integer> prevTS) throws RuntimeException {
        if(!isServerActive) 
            throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(!accountExists(userId))
            throw new RuntimeException(NOT_FOUND.withDescription("User not found").asRuntimeException());        
        else if(!(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1)))
            throw new RuntimeException(FAILED_PRECONDITION.withDescription("PREVTS is not stable").asRuntimeException());
        // else if(!(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1))){
        //     List<Integer> ret = valueTS;
        //     ret.add(-1);
        //     return ret;
        // }
        
        List<Integer> ret = valueTS;
        ret.add(accounts.get(userId));
        
        return ret;
    }

    public List<Integer> createAccount(String userId, List<Integer> prevTS) throws RuntimeException {

        if(!isServerActive)
            throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(accountExists(userId))
            throw new RuntimeException(ALREADY_EXISTS.withDescription("User already exists").asRuntimeException());

        Debug.debug("replicaTS = " + replicaTS);
        int index = (Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0))); // turns "A" into 0, "B" into 1, etc
        this.replicaTS.set(index, replicaTS.get(index) + 1); // increment servers's replicaTS
        Debug.debug("replicaTS = " + replicaTS);

        // TODO revirew if prevTS = this.valueTS and TS = this.replicaTS
        CreateOp op = new CreateOp(userId, this.valueTS, this.replicaTS);
        ledger.add(op);

        // if prevTS <= valueTS
        Debug.debug("prevTS = " + prevTS);
        Debug.debug("valueTS = " + valueTS);
        if(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1)) {
            // operacao executada
            accounts.put(userId, 0);
            op.setStable();
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
        else if(accounts.get(from) < amount) throw new RuntimeException(INVALID_ARGUMENT.withDescription("Not enough balance").asRuntimeException());
        
        TransferOp op = new TransferOp(from, dest, amount, this.valueTS, this.replicaTS);
        
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
            distLedgerCrossServerService.propagateState(getLedgerState(), replicaTS);
        }
        return 0;
    }


    public void receiveOperation(DistLedgerCommonDefinitions.Operation op){
        Integer index = Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0));
        
        if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT){
            CreateOp createOp = new CreateOp(op.getUserId(), valueTS, replicaTS);
            if(op.getPrevTS(index) <= this.valueTS.get(index)){
                createOp.setStable();
                // op é executada
                accounts.put(createOp.getAccount(), 0);
            }
            ledger.add(createOp);
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO){
            TransferOp transferOp = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount(), valueTS, replicaTS);
            if(op.getPrevTS(index) <= this.valueTS.get(index)){
                transferOp.setStable();
                // op é executada
                accounts.put(transferOp.getAccount(), accounts.get(transferOp.getAccount()) - transferOp.getAmount());
                accounts.put(transferOp.getDestAccount(), accounts.get(transferOp.getDestAccount()) + transferOp.getAmount());
                // TODO: 1. B.ValueTS = Merge(B.ValueTS, op.TS)
                valueTS.set(index, op.getPrevTS(index));
            }
            ledger.add(transferOp);
        }
    }
    
    public Integer updateServerState(LedgerState ledgerState, List<Integer> replicaTS){
        Integer index = Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0));
        
        for (DistLedgerCommonDefinitions.Operation op : ledgerState.getLedgerList()) {
            if(op.getTS(index) > replicaTS.get(index)){
                receiveOperation(op);
            }
        }
        // TODO: 2. B.ReplicaTS = merge(B.ReplicaTS, A.ReplicaTS)
        for (int i = 0; i < this.replicaTS.size();i++){
            this.replicaTS.set(i, Math.max(this.replicaTS.get(i), replicaTS.get(i)));
        }
        
        this.replicaTS.set(index, replicaTS.get(index));

        for (DistLedgerCommonDefinitions.Operation op : ledgerState.getLedgerList()) {
            if(op.getTS(index) > replicaTS.get(index)){
                receiveOperation(op);
            }
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

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

        this.valueTS = new ArrayList<Integer>(3);
        this.replicaTS = new ArrayList<Integer>(3);
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

    public List<Integer> getBalance(String userId, List<Integer> prevTS) {
        if(!isServerActive) 
            throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(!accountExists(userId))
            throw new RuntimeException(NOT_FOUND.withDescription("User not found").asRuntimeException());        
        else if(!(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1))){
            Debug.debug("!( [" + prevTS.get(0) + " " + prevTS.get(1) + "] <= [" + this.valueTS.get(0) + " " + this.valueTS.get(1) + "] )");
            throw new RuntimeException(FAILED_PRECONDITION.withDescription("PREVTS is not stable").asRuntimeException());
        }
        // else if(!(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1))){
        //     List<Integer> ret = valueTS;
        //     ret.add(-1);
        //     return ret;
        // }
        
        List<Integer> ret = valueTS;
        ret.add(accounts.get(userId));
        
        return ret;
    }

    public List<Integer> createAccount(String userId, List<Integer> prevTS) {

        if(!isServerActive)
            throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(accountExists(userId))
            throw new RuntimeException(ALREADY_EXISTS.withDescription("User already exists").asRuntimeException());

        int index = (Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0))); // turns "A" into 0, "B" into 1, etc
        this.replicaTS.set(index, replicaTS.get(index) + 1); // increment servers's replicaTS
        Debug.debug("replicaTS = " + replicaTS);

        // TODO revirew if prevTS = this.valueTS and TS = this.replicaTS
        CreateOp op = new CreateOp(userId, new ArrayList<Integer>(prevTS), new ArrayList<Integer>(this.replicaTS));
        ledger.add(op);

        // if prevTS <= valueTS
        if(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1)) {
            // operacao executada
            accounts.put(userId, 0);
            op.setStable();
            Debug.debug("Operation is stable");
            
            this.valueTS.set(index, op.getTS().get(index));
        }
        Debug.debug("prevTS = " + prevTS);
        Debug.debug("valueTS = " + valueTS);
        
        return replicaTS;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public List<Integer> transferTo(String from, String dest, Integer amount, List<Integer> prevTS) {
        if(!isServerActive) throw new RuntimeException(CANCELLED.withDescription("UNAVAILABLE").asRuntimeException());
        else if(amount <= 0) throw new RuntimeException(INVALID_ARGUMENT.withDescription("Invalid amount").asRuntimeException());
        
        int index = (Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0))); // turns "A" into 0, "B" into 1, etc
        this.replicaTS.set(index, replicaTS.get(index) + 1); // increment servers's replicaTS
        Debug.debug("replicaTS = " + replicaTS);

        TransferOp op = new TransferOp(from, dest, amount, new ArrayList<Integer>(prevTS), new ArrayList<Integer>(this.replicaTS));
        ledger.add(op);
        
        // if prevTS <= valueTS
        if(prevTS.get(0) <= valueTS.get(0) && prevTS.get(1) <= valueTS.get(1)) {
            
            if(!(accountExists(from) && accountExists(dest)))
                throw new RuntimeException(NOT_FOUND.withDescription("User not found").asRuntimeException());
            if(from.equals(dest))
                throw new RuntimeException(INVALID_ARGUMENT.withDescription("Can't transfer to same account").asRuntimeException());
            if(accounts.get(from) < amount)
                throw new RuntimeException(INVALID_ARGUMENT.withDescription("Not enough balance").asRuntimeException());
            
            accounts.put(from, accounts.get(from) - amount);
            accounts.put(dest, accounts.get(dest) + amount);
            op.setStable();
            Debug.debug("Operation is stable");
            this.valueTS.set(index, op.getTS().get(index));
        }

        Debug.debug("prevTS = " + prevTS);
        Debug.debug("valueTS = " + valueTS);
        
        return replicaTS;
    }

    // gossip sends to all neighbours including itself
    public Integer gossip(String target){
        if(!isServerActive) return -1;
        String targetIp = namingServerService.lookup(this.serviceName, target).get(0);

        DistLedgerCrossServerService distLedgerCrossServerService = new DistLedgerCrossServerService(targetIp);
        // distLedgerCrossServerService
        LedgerState temp = getLedgerState();
        Debug.debug("propagating " + temp);
        distLedgerCrossServerService.propagateState(temp, replicaTS);
        return 0;
    }


    public void receiveOperation(DistLedgerCommonDefinitions.Operation op){
        Integer index = Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0));
        
        if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT){
            CreateOp createOp = new CreateOp(op.getUserId(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
            if(op.getTS(1-index) > this.replicaTS.get(1-index)){
                ledger.add(createOp);
                if(createOp.getPrevTS().get(1-index) <= this.valueTS.get(1-index)){
                    // op é executada
                    accounts.put(createOp.getAccount(), 0);
                    
                    createOp.setStable();
                    Debug.debug("operation is stable, updating valueTS");
                    
                    // B.ValueTS = Merge(B.ValueTS, op.TS)
                    valueTS.set(1-index, op.getTS(1-index));
                }
            }
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO){
            TransferOp transferOp = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
            if(op.getTS(1-index) > this.replicaTS.get(1-index)){
                ledger.add(transferOp);
                
                if(transferOp.getPrevTS().get(1-index) <= this.valueTS.get(1-index)){
                    // op é executada
                    accounts.put(transferOp.getAccount(), accounts.get(transferOp.getAccount()) - transferOp.getAmount());
                    accounts.put(transferOp.getDestAccount(), accounts.get(transferOp.getDestAccount()) + transferOp.getAmount());
                    
                    transferOp.setStable();
                    Debug.debug("operation is stable, updating valueTS");
                    
                    // B.ValueTS = Merge(B.ValueTS, op.TS)
                    valueTS.set(index, op.getTS(index));
                }
            }
        }
    }

    public void execute(DistLedgerCommonDefinitions.Operation op){
        Integer index = Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0));

        for(int i = 0; i < 3; i++){     // finds correct value for index
            try{
                if(op.getTS(i) - op.getPrevTS(i) > 0)
                    index = i;
            } catch (Exception e) {
                continue;
            }
        }

        if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT){
            accounts.put(op.getUserId(), 0);
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO){
            accounts.put(op.getUserId(), accounts.get(op.getUserId()) - op.getAmount());
            accounts.put(op.getDestUserId(), accounts.get(op.getDestUserId()) + op.getAmount());
        }
        // B.ReplicaTS = merge(B.ReplicaTS, A.ReplicaTS)
        valueTS.set(index, op.getTS(index));
    }
    
    public Integer updateServerState(LedgerState ledgerState, List<Integer> replicaTS){
        Integer index = Character.getNumericValue(qualifier.charAt(0)) - Character.getNumericValue("A".charAt(0));
        
        for (DistLedgerCommonDefinitions.Operation op : ledgerState.getLedgerList()) {
            Debug.debug("" + op.getTSList() + " " + this.replicaTS);
            if(!(op.getTS(index) <= this.replicaTS.get(index) && op.getTS(1-index) <= this.replicaTS.get(1-index))){
                Debug.debug("receiving new operation\n" + op);
                receiveOperation(op);
            }
            Debug.debug("valueTS before merge = " + valueTS);
            for (int i = 0; i < this.valueTS.size();i++){
                this.valueTS.set(i, Math.max(this.valueTS.get(i), valueTS.get(i)));
            }
            Debug.debug("valueTS after merge = " + valueTS);
        }
        
        Debug.debug("2nd for");
        
        for (Operation op : this.ledger) {
            if(!op.isStable()){
                Debug.debug("" + op.getPrevTS() + " " + this.valueTS);
                if(op.getPrevTS().get(index) <= this.valueTS.get(index) && op.getPrevTS().get(1-index) <= this.valueTS.get(1-index)){
                    Debug.debug("executing\n" + op);
                    op.setStable();
                    execute(operationToDistOperation(op));
                }
            }
        }
        Debug.debug("valueTS = " + valueTS);
        
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
            .addAllPrevTS(op.getPrevTS())
            .addAllTS(op.getTS())
            .build();
        } else if(op instanceof TransferOp){
            return DistLedgerCommonDefinitions.Operation.newBuilder()
            .setType(DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO)
            .setUserId(op.getAccount())
            .setDestUserId(((TransferOp) op).getDestAccount())
            .setAmount(((TransferOp) op).getAmount())
            .addAllPrevTS(op.getPrevTS())
            .addAllTS(op.getTS())
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

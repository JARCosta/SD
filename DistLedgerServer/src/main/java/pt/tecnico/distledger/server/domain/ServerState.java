package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.Debug;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Map<String, Integer> accounts = new HashMap<>();
    public boolean isServerActive;
    public boolean isPrimaryServer;
    private List<DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> crossServerStubs;

    // Dictionary<String, Integer> accounts = new Hashtable<>();

    public ServerState(List<DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub> stubs) {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.isPrimaryServer = true;
        this.crossServerStubs = stubs;
    }

    public ServerState() {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.isPrimaryServer = false;
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
        else if(!this.isPrimaryServer) return -3;
        else if(accountExists(userId)) return -2;
        CreateOp op = new CreateOp(userId);
        ledger.add(op);
        accounts.put(userId, 0);

        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .build();
                        
        for(DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub : crossServerStubs){
            PropagateStateResponse result = stub.propagateState(PropagateStateRequest.newBuilder()
                            .setOperation(operation)
                            .build());
        }
        return 0;
    }

    public boolean accountExists(String userId) {
        return accounts.get(userId) != null;
    }

    public Integer deleteAccount(String userId) {
        if(!isServerActive) return -1;
        else if(!this.isPrimaryServer) return -4;
        else if(!accountExists(userId)) return -2;
        else if(getBalance(userId) != 0) return -3;
        DeleteOp op =  new DeleteOp(userId);
        ledger.add(op);
        accounts.remove(userId);

        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .build();
                        
        for(DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub : crossServerStubs){
            PropagateStateResponse result = stub.propagateState(PropagateStateRequest.newBuilder()
                            .setOperation(operation)
                            .build());
        }
        return 0;
    }

    public Integer transferTo(String from, String dest, Integer amount) {
        if(!isServerActive) return -1;
        else if(!this.isPrimaryServer) return -6;
        else if(!(accountExists(from) && accountExists(dest))) return -2;
        else if(from.equals(dest)) return -3;
        else if(amount <= 0) return -4;
        else if(getBalance(from) < amount) return -5;
        TransferOp op = new TransferOp(from, dest, amount);
        ledger.add(op);
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(dest, accounts.get(dest) + amount);

        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
                        .setType(type)
                        .setUserId(op.getAccount())
                        .setDestUserId(op.getDestAccount())
                        .setAmount(op.getAmount())
                        .build();
                        
        for(DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub : crossServerStubs){
            PropagateStateResponse result = stub.propagateState(PropagateStateRequest.newBuilder()
                            .setOperation(operation)
                            .build());
        }        return 0;
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

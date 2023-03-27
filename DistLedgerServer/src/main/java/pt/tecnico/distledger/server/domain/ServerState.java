package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.grpc.DistLedgerCrossServerService;
import pt.tecnico.distledger.server.grpc.NamingServerService;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ServerState {
    private List<Operation> ledger = new ArrayList<>();
    private Map<String, Integer> accounts = new HashMap<>();
    public boolean isServerActive;
    public boolean isPrimaryServer;
    public NamingServerService namingServerService;
    private String serviceName;
    private String qualifier;
    private List<DistLedgerCrossServerServiceBlockingStub> crossServerStubs = new ArrayList<>();
    private List<String> neighbours;

    // Dictionary<String, Integer> accounts = new Hashtable<>();

    public ServerState(NamingServerService namingServerService, String serviceName, String qualifier) {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.isPrimaryServer = true;
        this.namingServerService = namingServerService;
        this.serviceName = serviceName;
        this.qualifier = qualifier;
    }

    public ServerState() {
        this.ledger = new ArrayList<>();
        accounts.put("broker", 1000);
        this.isServerActive = true;
        this.isPrimaryServer = false;
    }

    private List<DistLedgerCrossServerServiceBlockingStub> refreshStubs() {
        neighbours = namingServerService.lookup(serviceName, qualifier);
        List<DistLedgerCrossServerServiceBlockingStub> crossServerStubs = new ArrayList<>();
        for(String neighbour : neighbours){
            ManagedChannel channel = ManagedChannelBuilder.forTarget(neighbour).usePlaintext().build();
            DistLedgerCrossServerServiceBlockingStub stub;
            stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
            crossServerStubs.add(stub);
        }
        return crossServerStubs;
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
        
        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(type)
        .setUserId(op.getAccount())
        .build();
        
        this.crossServerStubs = refreshStubs();
        for(String neighbour : neighbours){
            DistLedgerCrossServerService distLedgerCrossServerService = new DistLedgerCrossServerService(neighbour);
            distLedgerCrossServerService.propagateState(operation);
        }
        ledger.add(op);
        accounts.put(userId, 0);
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
        
        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(type)
        .setUserId(op.getAccount())
        .build();
        
        this.crossServerStubs = refreshStubs();
        for(String neighbour : neighbours){
            DistLedgerCrossServerService distLedgerCrossServerService = new DistLedgerCrossServerService(neighbour);
            distLedgerCrossServerService.propagateState(operation);
        }
        ledger.add(op);
        accounts.remove(userId);
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

        DistLedgerCommonDefinitions.OperationType type = DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO;
        DistLedgerCommonDefinitions.Operation operation = DistLedgerCommonDefinitions.Operation.newBuilder()
        .setType(type)
        .setUserId(op.getAccount())
        .setDestUserId(op.getDestAccount())
        .setAmount(op.getAmount())
        .build();
        
        this.crossServerStubs = refreshStubs();
        for(String neighbour : neighbours){
            DistLedgerCrossServerService distLedgerCrossServerService = new DistLedgerCrossServerService(neighbour);
            distLedgerCrossServerService.propagateState(operation);
        }
        ledger.add(op);
        accounts.put(from, accounts.get(from) - amount);
        accounts.put(dest, accounts.get(dest) + amount);
        return 0;
    }


    public void receiveOperation(DistLedgerCommonDefinitions.Operation op){
        if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_CREATE_ACCOUNT){
            CreateOp createOp = new CreateOp(op.getUserId());
            ledger.add(createOp);
            accounts.put(createOp.getAccount(), 0);
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_DELETE_ACCOUNT){
            DeleteOp deleteOp = new DeleteOp(op.getUserId());
            ledger.add(deleteOp);
            accounts.remove(deleteOp.getAccount());
        } else if(op.getType() == DistLedgerCommonDefinitions.OperationType.OP_TRANSFER_TO){
            TransferOp transferOp = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount());
            ledger.add(transferOp);
            accounts.put(transferOp.getAccount(), accounts.get(transferOp.getAccount()) - transferOp.getAmount());
            accounts.put(transferOp.getDestAccount(), accounts.get(transferOp.getDestAccount()) + transferOp.getAmount());
        }
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

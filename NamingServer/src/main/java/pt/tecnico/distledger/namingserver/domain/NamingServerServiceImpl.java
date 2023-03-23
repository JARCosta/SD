package pt.tecnico.distledger.namingserver.domain;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.Debug;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceImplBase;
//import pt.ulisboa.tecnico.distledger.contract.distledgerserver.NamingServerServiceOuterClass.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceOuterClass.*;
/*
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;*/

import static io.grpc.Status.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;



public class NamingServerServiceImpl extends NamingServerServiceImplBase {

    private Map<String, ServiceEntry> services;

    public NamingServerServiceImpl(Map<String, ServiceEntry> services) {
        this.services = services;
    }

    @Override
    public void register(RegisterRequest request,
                         StreamObserver<RegisterResponse> responseObserver) {
        Debug.debug("Received register request.");

        Debug.debug("Service Name: " + request.getServiceName());
        Debug.debug("Qualifier: " + request.getQualifier());
        Debug.debug("Target: " + request.getAddress());

        String serviceName = request.getServiceName();
        String qualifier = request.getQualifier();
        String address = request.getAddress();

        ServiceEntry serviceEntry = services.get(serviceName);

        if (serviceEntry == null){
            serviceEntry = new ServiceEntry(serviceName);
            services.put(serviceName, serviceEntry);
        }

        serviceEntry.addServerEntry(address, qualifier);

        Debug.debug("Service Entry: " + serviceEntry.toString());
        Debug.debug("Number of services: " + services.size());

        // prepare response
        RegisterResponse response = RegisterResponse.newBuilder().build();

        Debug.debug("Sending response.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        Debug.debug("Request handled.");
        Debug.debug("\n");

    }

    @Override
    public void lookup(LookupRequest request,
                         StreamObserver<LookupResponse> responseObserver) {
        Debug.debug("Received lookup request.");

        Debug.debug(request.getServiceName());
        Debug.debug(request.getQualifier());

        String serviceName = request.getServiceName();
        String qualifier = request.getQualifier();

        ServiceEntry serviceEntry = services.get(serviceName);
        List<String> servers = new ArrayList<>();

        if (serviceEntry != null){
            if (qualifier == "")
                servers = serviceEntry.getServers();
            else
                servers = serviceEntry.getServers(qualifier);
        }

        // prepare response
        LookupResponse response = LookupResponse.newBuilder().addAllServers(servers).build();

        Debug.debug("Sending response.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        Debug.debug("Request handled.");
        Debug.debug("\n");

    }

    @Override
    public void delete(DeleteRequest request,
                         StreamObserver<DeleteResponse> responseObserver) {
        Debug.debug("Received delete request.");

        Debug.debug(request.getServiceName());
        Debug.debug(request.getAddress());

        String serviceName = request.getServiceName();
        String address = request.getAddress();

        ServiceEntry serviceEntry = services.get(serviceName);

        serviceEntry.removeServerEntry(address);

        Debug.debug("Service Entry: " + serviceEntry.toString());
        Debug.debug("Number of services: " + services.size());

        // prepare response
        DeleteResponse response = DeleteResponse.newBuilder().build();

        Debug.debug("Sending response.");
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        Debug.debug("Request handled.");
        Debug.debug("\n");

    }
}
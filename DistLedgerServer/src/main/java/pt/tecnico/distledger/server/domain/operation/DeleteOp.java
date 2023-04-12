package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class DeleteOp extends Operation {

    public DeleteOp(String account, List<Integer> prevTS, List<Integer> TS) {
        super(account, prevTS, TS);
    }

    @Override
    public String toString() {
        return "DeleteOp {account: " + super.getAccount() + "}\n";
    }
}

package pt.tecnico.distledger.server.domain.operation;

public class DeleteOp extends Operation {

    public DeleteOp(String account) {
        super(account);
    }

    @Override
    public String toString() {
        return "DeleteOp {account: " + super.getAccount() + "}\n";
    }
}

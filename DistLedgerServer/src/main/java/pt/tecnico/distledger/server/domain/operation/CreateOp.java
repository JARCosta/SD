package pt.tecnico.distledger.server.domain.operation;

public class CreateOp extends Operation {

    public CreateOp(String account) {
        super(account);
    }

    @Override
    public String toString() {
        return "CreateOp {account: " + super.getAccount() + "}\n";
    }
}

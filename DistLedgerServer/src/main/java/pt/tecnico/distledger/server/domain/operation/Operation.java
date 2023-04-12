package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class Operation {
    private String account;
    private List<Integer> prevTS;
    private List<Integer> TS;
    private Boolean isStable;

    public Operation(String fromAccount, List<Integer> prevTS, List<Integer> TS) {
        this.account = fromAccount;
        this.prevTS = prevTS;
        this.TS = TS;
        this.isStable = false;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setStable() {
        isStable = true;
    }
    
}

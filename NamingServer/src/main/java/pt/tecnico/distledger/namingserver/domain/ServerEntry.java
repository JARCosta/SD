package pt.tecnico.distledger.namingserver.domain;

public class ServerEntry {

    private String address;
    private String qualifier;

    public ServerEntry(String address, String qualifier){
        this.address = address;
        this.qualifier = qualifier;
    }
/*
    public String getQualifier() {
        return qualifier;
    }

    public String getTarget() {
        return target;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setTarget(String address) {
        this.target = target;
    }
*/


}

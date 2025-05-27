package me.athlaeos.valhallammo.trading.services;

public abstract class Service {
    private final String id;
    private final String type;

    public Service(String id, ServiceType type){
        this.id = id;
        this.type = type.getID();
    }

    public String getID() { return id; }
    public String getType() { return type; }
    public ServiceType getServiceType(){
        return ServiceRegistry.getServiceType(type);
    }
}

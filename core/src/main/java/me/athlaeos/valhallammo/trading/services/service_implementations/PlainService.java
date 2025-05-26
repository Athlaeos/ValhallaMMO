package me.athlaeos.valhallammo.trading.services.service_implementations;

import me.athlaeos.valhallammo.trading.services.Service;
import me.athlaeos.valhallammo.trading.services.ServiceType;

/**
 * Plain services are services that require no additional properties or configurations
 * to work
 */
public class PlainService extends Service {
    public PlainService(String id, ServiceType type) {
        super(id, type);
    }
}

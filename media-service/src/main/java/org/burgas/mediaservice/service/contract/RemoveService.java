package org.burgas.mediaservice.service.contract;

import java.util.UUID;

public interface RemoveService<ID extends UUID> {

    void remove(ID id);
}

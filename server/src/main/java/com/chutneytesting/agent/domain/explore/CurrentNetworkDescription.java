package com.chutneytesting.agent.domain.explore;

import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.server.core.admin.Backupable;
import java.util.Optional;

public interface CurrentNetworkDescription extends Backupable {
    Optional<NetworkDescription> findCurrent();
    void switchTo(NetworkDescription networkDescription);
}

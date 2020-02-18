package com.chutneytesting.agent.domain.explore;

import com.chutneytesting.admin.domain.Backupable;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import java.util.Optional;

public interface CurrentNetworkDescription extends Backupable {
    Optional<NetworkDescription> findCurrent();
    void switchTo(NetworkDescription networkDescription);
}

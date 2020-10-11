package com.chutneytesting.design.domain.plugins.linkifier;

import com.chutneytesting.admin.domain.Backupable;
import java.util.List;

public interface Linkifiers extends Backupable {

    List<Linkifier> getAll();

    Linkifier add(Linkifier linkifier);

    Linkifier update(String id, Linkifier linkifier);

    void remove(String id);
}

package com.chutneytesting.component.scenario.infra;

import com.chutneytesting.component.scenario.infra.orient.OrientComponentDB;
import com.chutneytesting.server.core.domain.admin.Backupable;
import java.io.OutputStream;
import org.springframework.stereotype.Component;

@Component
public class ComponentBackupRepository implements Backupable {

    private final OrientComponentDB orientComponentDB;

    public ComponentBackupRepository(OrientComponentDB orientComponentDB) {
        this.orientComponentDB = orientComponentDB;
    }

    @Override
    public void backup(OutputStream outputStream) {
        orientComponentDB.backup(outputStream);
    }

    @Override
    public String name() {
        return "orient";
    }
}

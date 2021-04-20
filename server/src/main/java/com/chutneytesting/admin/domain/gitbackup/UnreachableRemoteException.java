package com.chutneytesting.admin.domain.gitbackup;

public class UnreachableRemoteException extends RuntimeException {
    public UnreachableRemoteException(String message) {
        super(message);
    }
}

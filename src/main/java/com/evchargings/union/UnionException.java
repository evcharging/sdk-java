package com.evchargings.union;

public class UnionException extends Exception {
    public int getErrno() {
        return errno;
    }

    int errno;
    public UnionException(int errno, String message) {
        this(errno, message, null);
    }
    public UnionException(int errno, String message, Throwable e) {
        super(message, e);
        this.errno = errno;
    }
    public UnionException(Throwable e) {
        super(e);
    }
}
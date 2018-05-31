package io.morethan.tweaky.shared;

/**
 * Used on the client side to signal that there was an exception on the server side.
 */
public class RemoteCallFailure extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RemoteCallFailure(Throwable cause) {
        super(cause);
    }

}
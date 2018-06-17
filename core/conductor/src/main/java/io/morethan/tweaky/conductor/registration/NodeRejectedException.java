package io.morethan.tweaky.conductor.registration;

/**
 * Used to signal that a node wasn't accepted by {@link NodeRegistrationValidator}.
 */
public class NodeRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NodeRejectedException(String message) {
        super(message);
    }

}
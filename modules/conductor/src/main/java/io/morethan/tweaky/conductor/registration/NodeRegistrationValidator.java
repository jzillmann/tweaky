package io.morethan.tweaky.conductor.registration;

import java.util.Optional;

import io.morethan.tweaky.conductor.Conductor;

/**
 * Responsible for verifying that a node, which attempts to register on the {@link Conductor} is allowed to join.
 */
public interface NodeRegistrationValidator {

    /**
     * Validates if the node is allowed to join.
     * 
     * @param token
     * @param port
     * @param host
     * 
     * @return {@link Optional#empty()} in case the node is allowed to join or else an rejection message
     */
    Optional<String> accept(String host, int port, String token);

    /**
     * Release the given token, i.e. because the node has been dropped.
     *
     * @param token
     */
    void release(String token);

    public static NodeRegistrationValidator ACCEPT_ALL = new NodeRegistrationValidator() {

        @Override
        public Optional<String> accept(String host, int port, String token) {
            return Optional.empty();
        }

        @Override
        public void release(String token) {
            // nothing to do
        }
    };
}

package io.morethan.tweaky.conductor.registration;

import java.util.function.Supplier;

import io.morethan.tweaky.conductor.util.Try;

/**
 * Responsible for verifying that a node, which attempts to register.
 */
public interface NodeRegistrationValidator {

    /**
     * Validates if the node is allowed to join.
     * 
     * @param token
     * @param port
     * @param host
     * @param registrationProcess
     * 
     * @return a registration try which can either be a success or a failure/rejection
     */
    Try<NodeContact, String> accept(String host, int port, String token, Supplier<Try<NodeContact, String>> registrationProcess);

    public static NodeTokenStore tokenStore() {
        return new NodeTokenStore();
    }

    public static NodeRegistrationValidator singleToken(String token) {
        return new SingleTokenValidator(token);
    }

    public static NodeRegistrationValidator acceptAll() {
        return new NodeRegistrationValidator() {

            @Override
            public Try<NodeContact, String> accept(String host, int port, String token, Supplier<Try<NodeContact, String>> registrationProcess) {
                return registrationProcess.get();
            }

        };
    }

}

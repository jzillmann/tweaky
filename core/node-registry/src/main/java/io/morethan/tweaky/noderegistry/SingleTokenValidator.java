package io.morethan.tweaky.noderegistry;

import java.util.function.Supplier;

import io.morethan.tweaky.noderegistry.util.Try;

/**
 * A {@link NodeRegistrationValidator} which is configured with a single token. Nodes need to pass that token in order to be accepted.
 */
public class SingleTokenValidator implements NodeRegistrationValidator {

    private final String _token;

    public SingleTokenValidator(String token) {
        _token = token;
    }

    @Override
    public Try<NodeContact, String> accept(String host, int port, String token, Supplier<Try<NodeContact, String>> registrationProcess) {
        if (!_token.equals(token)) {
            return Try.failure("Invalid token: " + token);
        }
        return registrationProcess.get();
    }

}

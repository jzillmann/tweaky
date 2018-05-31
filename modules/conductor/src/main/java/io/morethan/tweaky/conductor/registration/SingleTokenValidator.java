package io.morethan.tweaky.conductor.registration;

import java.util.Optional;

import io.morethan.tweaky.conductor.NodeRegistrationValidator;

/**
 * A {@link NodeRegistrationValidator} which is configured with a single token. Nodes need to pass that token in order to be accepted.
 */
public class SingleTokenValidator implements NodeRegistrationValidator {

    private final String _token;

    public SingleTokenValidator(String token) {
        _token = token;
    }

    @Override
    public Optional<String> accept(String host, int port, String token) {
        if (!_token.equals(token)) {
            return Optional.of("Invalid token: " + token);
        }
        return Optional.empty();
    }

}

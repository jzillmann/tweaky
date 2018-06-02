package io.morethan.tweaky.conductor.registration;

import java.util.Optional;

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

    @Override
    public void release(String token) {
        // nothing to do
    }
}

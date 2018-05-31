package io.morethan.tweaky.conductor.registration;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.morethan.tweaky.conductor.NodeRegistrationValidator;

/**
 * A {@link NodeRegistrationValidator} which can be configured with a set of allowed tokens. A token can be used only once.
 */
public class NodeTokenStore implements NodeRegistrationValidator {

    private final Set<String> _allowedTokens = new HashSet<>();
    private final Set<String> _acceptedTokens = new HashSet<>();

    /**
     * Adds the given token to the set of allowed tokens.
     * 
     * @param token
     */
    public void addToken(String token) {
        _allowedTokens.add(token);
    }

    /**
     * Rejects the node if the token is unknown or was already accepted before.
     */
    @Override
    public Optional<String> accept(String host, int port, String token) {
        if (!_allowedTokens.contains(token)) {
            return Optional.of("Unknown token: " + token);
        }
        if (_acceptedTokens.contains(token)) {
            return Optional.of("Token already used: " + token);
        }
        _acceptedTokens.add(token);
        return Optional.empty();
    }

}

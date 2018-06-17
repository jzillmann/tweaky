package io.morethan.tweaky.noderegistry;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.google.common.util.concurrent.Striped;

import io.morethan.tweaky.noderegistry.util.Try;

/**
 * A {@link NodeRegistrationValidator} which can be configured with a set of allowed tokens. A token can be used only once.
 */
public class NodeTokenStore implements NodeRegistrationValidator {

    private final Striped<Lock> _nodeRegistrationLock = Striped.lock(16);
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
    public Try<NodeContact, String> accept(String host, int port, String token, Supplier<Try<NodeContact, String>> registrationProcess) {
        Lock lock = _nodeRegistrationLock.get(token);
        lock.lock();
        try {
            if (!_allowedTokens.contains(token)) {
                return Try.failure("Unknown token: " + token);
            }
            if (_acceptedTokens.contains(token)) {
                return Try.failure("Token already used: " + token);
            }
            Try<NodeContact, String> registrationTry = registrationProcess.get();
            if (registrationTry.isSuccess()) {
                _acceptedTokens.add(token);
            }
            return registrationTry;
        } finally {
            lock.unlock();
        }
    }
}

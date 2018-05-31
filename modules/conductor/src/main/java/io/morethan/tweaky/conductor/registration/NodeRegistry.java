package io.morethan.tweaky.conductor.registration;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.morethan.tweaky.conductor.Conductor;

/**
 * Takes care of registering nodes for a {@link Conductor}.
 */
public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRegistry.class);

    private final NodeRegistrationValidator _nodeRegistrationValidator;

    private int _registeredNodes = 0;

    public NodeRegistry(NodeRegistrationValidator nodeRegistrationValidator) {
        _nodeRegistrationValidator = nodeRegistrationValidator;
    }

    public int registeredNodes() {
        return _registeredNodes;
    }

    public void registerNode(String host, int port, String token) {
        LOG.info("Registering node '{}' from {}:{}", token, host, port);
        Optional<String> rejectMessage = _nodeRegistrationValidator.accept(host, port, token);
        if (rejectMessage.isPresent()) {
            LOG.warn("Rejecting node: {}", rejectMessage.get());
            throw new NodeRejectedException("Rejected node - " + rejectMessage.get());
        }
        _registeredNodes++;
    }
}

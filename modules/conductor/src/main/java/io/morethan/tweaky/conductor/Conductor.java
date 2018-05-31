package io.morethan.tweaky.conductor;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The conductor manages a set of Nodes.
 */
public class Conductor {

    private static final Logger LOG = LoggerFactory.getLogger(Conductor.class);

    private final NodeRegistrationValidator _nodeRegistrationValidator;

    private int _acceptedNodes = 0;

    public Conductor(NodeRegistrationValidator nodeRegistrationValidator) {
        _nodeRegistrationValidator = nodeRegistrationValidator;
    }

    public int nodeCount() {
        return _acceptedNodes;
    }

    public void registerNode(String host, int port, String token) {
        LOG.info("Registering node {} from {}:{}", token, host, port);
        Optional<String> rejectMessage = _nodeRegistrationValidator.accept(host, port, token);
        if (rejectMessage.isPresent()) {
            LOG.warn("Rejecting node: {}", rejectMessage.get());
            throw new RuntimeException("Rejected node - " + rejectMessage.get());
        }
        _acceptedNodes++;
    }

}

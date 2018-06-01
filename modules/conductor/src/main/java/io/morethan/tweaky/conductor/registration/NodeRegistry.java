package io.morethan.tweaky.conductor.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.morethan.tweaky.conductor.Conductor;
import io.morethan.tweaky.conductor.NodeAddress;

/**
 * Takes care of registering nodes for a {@link Conductor}.
 */
public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRegistry.class);

    private final NodeRegistrationValidator _registrationValidator;
    private final NodeNameProvider _nameProvider;
    private final Map<String, NodeAddress> _nodeAddressMap = new HashMap<>();

    public NodeRegistry(NodeRegistrationValidator registrationValidator, NodeNameProvider nameProvider) {
        _registrationValidator = registrationValidator;
        _nameProvider = nameProvider;
    }

    public int registeredNodes() {
        return _nodeAddressMap.size();
    }

    public void registerNode(String host, int port, String token) {
        String nodeId = nodeId(host, port, token);
        Optional<String> rejectMessage = _registrationValidator.accept(host, port, token);
        if (rejectMessage.isPresent()) {
            LOG.warn("Rejecting node '{}': {}", nodeId, rejectMessage.get());
            throw new NodeRejectedException("Rejected node - " + rejectMessage.get());
        }

        String nodeName = _nameProvider.getName(host, port, token);
        if (_nodeAddressMap.containsKey(nodeName)) {
            LOG.warn("Rejecting node '{}' named as '{}' since a node with that name already exists!", nodeId, nodeName);
            throw new NodeRejectedException("Rejected node - Name already given: " + nodeName);
        }
        LOG.info("Accepted node '{}' as '{}'", nodeId, nodeName);
        _nodeAddressMap.put(nodeName, new NodeAddress(host, port, token));
    }

    private String nodeId(String host, int port, String token) {
        return new StringBuilder(token).append(':').append(host).append(':').append(':').toString();
    }
}

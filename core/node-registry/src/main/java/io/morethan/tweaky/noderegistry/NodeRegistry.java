package io.morethan.tweaky.noderegistry;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.morethan.tweaky.noderegistry.util.Try;

/**
 * Takes care of registering nodes.
 */
public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRegistry.class);

    private final NodeAcceptor _nodeAcceptor;
    private final List<NodeContact> _nodeContacts = new CopyOnWriteArrayList<>();
    private final Set<NodeListener> _nodeListeners;

    public NodeRegistry(NodeAcceptor nodeAcceptor, Set<NodeListener> nodeListeners) {
        _nodeAcceptor = nodeAcceptor;
        _nodeListeners = nodeListeners;
    }

    public int registeredNodes() {
        return _nodeContacts.size();
    }

    public void registerNode(String host, int port, String token) {
        String nodeId = new StringBuilder(token).append(':').append(host).append(':').append(port).toString();
        Try<NodeContact, String> registrationTry = _nodeAcceptor.accept(host, port, token);
        if (!registrationTry.isSuccess()) {
            LOG.warn("Rejecting node '{}': {}", nodeId, registrationTry.failure());
            throw new NodeRejectedException("Rejected node - " + registrationTry.failure());
        }

        NodeContact nodeContact = registrationTry.get();
        LOG.info("Accepted node '{}' as '{}'", nodeId, nodeContact.name());

        // Node listeners should have been executed before nodeCount is increased (otherwise awaitNodes is not reliable)
        for (NodeListener nodeListener : _nodeListeners) {
            try {
                nodeListener.addNode(nodeContact);
            } catch (Exception e) {
                LOG.error("Failed to call node listener " + nodeListener + " with node " + nodeContact, e);
            }
        }

        _nodeContacts.add(nodeContact);
    }

}

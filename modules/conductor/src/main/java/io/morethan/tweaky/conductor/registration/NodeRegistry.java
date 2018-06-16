package io.morethan.tweaky.conductor.registration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import io.morethan.tweaky.conductor.channel.NodeChannel;
import io.morethan.tweaky.conductor.channel.NodeChannelProvider;
import io.morethan.tweaky.conductor.util.Try;

/**
 * Takes care of registering nodes for a conductor.
 */
public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRegistry.class);

    private final NodeRegistrationValidator _registrationValidator;
    private final NodeNameProvider _nameProvider;
    private final NodeChannelProvider _nodeChannelProvider;

    private final Set<String> _nodeNames = ConcurrentHashMap.newKeySet();
    private final List<NodeContact> _nodeContacts = new CopyOnWriteArrayList<>();
    private final Set<NodeListener> _nodeListeners;

    public NodeRegistry(NodeRegistrationValidator registrationValidator, NodeNameProvider nameProvider, NodeChannelProvider nodeChannelProvider, Set<NodeListener> nodeListeners) {
        _registrationValidator = registrationValidator;
        _nameProvider = nameProvider;
        _nodeChannelProvider = nodeChannelProvider;
        _nodeListeners = nodeListeners;
    }

    public int registeredNodes() {
        return _nodeContacts.size();
    }

    public void registerNode(String host, int port, String token) {
        String nodeId = nodeId(host, port, token);
        Try<NodeContact, String> registrationTry = _registrationValidator.accept(host, port, token, () -> {
            // Assign name
            String nodeName = _nameProvider.getName(host, port, token);
            if (_nodeNames.contains(nodeName)) {
                return Try.failure("Rejected node - Name already given: " + nodeName);
            }

            // Establish node channel
            NodeChannel nodeChannel;
            String remoteToken;
            try {
                nodeChannel = _nodeChannelProvider.get(host, port);
                remoteToken = nodeChannel.nodeClient().token();
            } catch (Exception e) {
                return Try.failure("Rejected node - Could not establish channel to  '" + host + ":" + port + "': " + Throwables.getRootCause(e).getMessage());
            }

            // Check remote token
            if (!token.equals(remoteToken)) {
                try {
                    nodeChannel.getManagedChannel().shutdown();
                } catch (Exception closeException) {
                    LOG.warn("Failed to shutfown failed node channel", closeException);
                }
                return Try.failure("Rejected node - Remote token does not match: " + token + " / " + remoteToken);
            }

            _nodeNames.add(nodeName);
            return Try.success(new NodeContact(nodeName, host, port, token, nodeChannel.getManagedChannel()));
        });
        if (!registrationTry.isSuccess()) {
            LOG.warn("Rejecting node '{}': {}", nodeId, registrationTry.failure());
            throw new NodeRejectedException("Rejected node - " + registrationTry.failure());
        }

        NodeContact nodeContact = registrationTry.get();
        LOG.info("Accepted node '{}' as '{}'", nodeId, nodeContact.name());
        for (NodeListener nodeListener : _nodeListeners) {
            try {
                nodeListener.addNode(nodeContact);
            } catch (Exception e) {
                LOG.error("Failed call node listener " + nodeListener + " with node " + nodeContact, e);
            }
        }

        // Node listeners should have been executed before nodeCount is increased (otherwise awaitNodes is not reliable)
        _nodeContacts.add(nodeContact);
    }

    private String nodeId(String host, int port, String token) {
        return new StringBuilder(token).append(':').append(host).append(':').append(port).toString();
    }

}

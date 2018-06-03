package io.morethan.tweaky.conductor.registration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

import io.morethan.tweaky.conductor.Conductor;
import io.morethan.tweaky.conductor.channel.NodeChannel;
import io.morethan.tweaky.conductor.channel.NodeChannelProvider;
import io.morethan.tweaky.node.NodeClient;

/**
 * Takes care of registering nodes for a {@link Conductor}.
 */
public class NodeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(NodeRegistry.class);

    private final NodeRegistrationValidator _registrationValidator;
    private final NodeNameProvider _nameProvider;
    private final NodeChannelProvider _nodeChannelProvider;
    private final Map<String, NodeAddress> _nodeAddressMap = new HashMap<>();
    private final Map<String, NodeChannel> _nodeChannelMap = new HashMap<>();

    public NodeRegistry(NodeRegistrationValidator registrationValidator, NodeNameProvider nameProvider, NodeChannelProvider nodeChannelProvider) {
        _registrationValidator = registrationValidator;
        _nameProvider = nameProvider;
        _nodeChannelProvider = nodeChannelProvider;
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

        NodeChannel nodeChannel;
        String remoteToken;
        try {
            nodeChannel = _nodeChannelProvider.get(host, port);
            try (NodeClient nodeClient = nodeChannel.open()) {
                remoteToken = nodeClient.token();
            }
        } catch (Exception e) {
            _registrationValidator.release(token);
            LOG.warn("Failed to establish channel to node " + nodeId, e);
            throw new NodeRejectedException("Rejected node - Could not establish channel to  '" + host + ":" + port + "': " + Throwables.getRootCause(e).getMessage());
        }
        if (!token.equals(remoteToken)) {
            _registrationValidator.release(token);
            nodeChannel.getManagedChannel().shutdown();// TODO fail safe
            LOG.warn("Rejecting node '{}' since transferred token '{}' and remote token '{}' do not match!", nodeName, token, remoteToken);
            throw new NodeRejectedException("Rejected node - Remote token does not match: " + token + " / " + remoteToken);
        }

        LOG.info("Accepted node '{}' as '{}'", nodeId, nodeName);
        _nodeAddressMap.put(nodeName, new NodeAddress(host, port, token));
        _nodeChannelMap.put(nodeName, nodeChannel);

        // TODO think about synchronization - up from name ?
        // TODO announce node arrival through listener or event ?
    }

    private String nodeId(String host, int port, String token) {
        return new StringBuilder(token).append(':').append(host).append(':').append(port).append(':').toString();
    }
}

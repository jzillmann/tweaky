package io.morethan.tweaky.conductor.registration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Striped;

import io.grpc.ManagedChannel;
import io.morethan.tweaky.conductor.util.Try;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.node.NodeClient;

/**
 * Accept nodes which request registration.
 */
public class NodeAcceptor {

    private final Striped<Lock> _nodeNameLock = Striped.lock(16);
    private final Set<String> _registeredNodeNames = new HashSet<>();
    private final NodeRegistrationValidator _registrationValidator;
    private final NodeNameProvider _nameProvider;
    private final ChannelProvider _channelProvider;
    private final NodeClientProvider _nodeClientProvider;

    public NodeAcceptor(NodeRegistrationValidator registrationValidator, NodeNameProvider nameProvider, ChannelProvider channelProvider, NodeClientProvider nodeClientProvider) {
        _registrationValidator = registrationValidator;
        _nameProvider = nameProvider;
        _channelProvider = channelProvider;
        _nodeClientProvider = nodeClientProvider;
    }

    public Try<NodeContact, String> accept(String host, int port, String token) {
        return _registrationValidator.accept(host, port, token, () -> {
            // Assign name
            String nodeName = _nameProvider.getName(host, port, token);

            // Establish node channel
            ManagedChannel nodeChannel;
            try {
                nodeChannel = _channelProvider.get(host, port);
            } catch (Exception e) {
                return Try.failure("Rejected node - Could not establish channel to  '" + host + ":" + port + "': " + Throwables.getRootCause(e).getMessage());
            }

            NodeClient nodeClient = _nodeClientProvider.get(nodeChannel);
            String remoteToken;
            try {
                remoteToken = nodeClient.token();
            } catch (Exception e) {
                return Try.failure("Rejected node - Could not get remote node token from  '" + host + ":" + port + "': " + Throwables.getRootCause(e).getMessage());
            }

            // Check remote token
            if (!token.equals(remoteToken)) {
                nodeChannel.shutdown();
                return Try.failure("Rejected node - Remote token does not match: " + token + " / " + remoteToken);
            }
            Lock lock = _nodeNameLock.get(nodeName);
            try {
                lock.lock();
                if (_registeredNodeNames.contains(nodeName)) {
                    nodeChannel.shutdown();
                    return Try.failure("Rejected node - Name already taken: " + nodeName);
                }
                _registeredNodeNames.add(nodeName);
            } finally {
                lock.unlock();
            }
            return Try.success(new NodeContact(nodeName, host, port, token, nodeChannel));
        });
    }

}

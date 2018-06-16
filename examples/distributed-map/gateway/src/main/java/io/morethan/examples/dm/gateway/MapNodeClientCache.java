package io.morethan.examples.dm.gateway;

import com.google.common.base.Verify;

import io.morethan.examples.dm.MapNodeClient;
import io.morethan.tweaky.conductor.registration.NodeContact;
import io.morethan.tweaky.conductor.registration.NodeListener;

/**
 * Cache of {@link MapNodeClient}s.
 */
public class MapNodeClientCache implements NodeListener {

    // TODO rename + make this common
    private final MapNodeClient[] _clients;
    private int _registeredClients;

    public MapNodeClientCache(int numNodes) {
        _clients = new MapNodeClient[numNodes];
    }

    public int clientCount() {
        return _clients.length;
    }

    @Override
    public void addNode(NodeContact nodeContact) {
        _clients[_registeredClients++] = MapNodeClient.on(nodeContact.channel());
    }

    public MapNodeClient client(int node) {
        MapNodeClient mapNodeClient = _clients[node];
        Verify.verifyNotNull(mapNodeClient, "Client for node %s has not yet been set!", node);
        // TODO if we would have managed channels, we could investigate the channels state for shutdown...
        return mapNodeClient;
    }

}

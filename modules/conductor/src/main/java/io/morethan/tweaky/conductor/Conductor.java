package io.morethan.tweaky.conductor;

import io.morethan.tweaky.conductor.registration.NodeRegistry;

/**
 * The conductor manages a set of Nodes.
 */
public class Conductor {

    private final NodeRegistry _nodeRegistry;

    public Conductor(NodeRegistry nodeRegistry) {
        _nodeRegistry = nodeRegistry;
    }

    public int nodeCount() {
        return _nodeRegistry.registeredNodes();
    }

}

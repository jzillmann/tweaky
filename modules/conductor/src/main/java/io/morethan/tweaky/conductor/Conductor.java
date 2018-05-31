package io.morethan.tweaky.conductor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.morethan.tweaky.conductor.registration.NodeRegistry;

/**
 * The conductor manages a set of Nodes.
 */
public class Conductor {

    private static final Logger LOG = LoggerFactory.getLogger(Conductor.class);

    private final NodeRegistry _nodeRegistry;

    public Conductor(NodeRegistry nodeRegistry) {
        _nodeRegistry = nodeRegistry;
    }

    public int nodeCount() {
        return _nodeRegistry.registeredNodes();
    }

}

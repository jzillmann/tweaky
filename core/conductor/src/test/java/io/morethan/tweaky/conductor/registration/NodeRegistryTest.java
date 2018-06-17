package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.google.common.collect.ImmutableSet;

import io.morethan.tweaky.conductor.util.Try;

class NodeRegistryTest {

    @Test
    void testNodeRegistration() {
        CapturingNodeListener nodeListener = new CapturingNodeListener();

        NodeAcceptor nodeAcceptor = mock(NodeAcceptor.class);
        when(nodeAcceptor.accept("localhost", 23, "token")).thenReturn(Try.success(mock(NodeContact.class)));
        when(nodeAcceptor.accept("localhost", 24, "token")).thenReturn(Try.failure("failed for some reason"));

        NodeRegistry registry = new NodeRegistry(nodeAcceptor, ImmutableSet.of(nodeListener));
        assertThat(registry.registeredNodes()).isEqualTo(0);

        // successful node
        registry.registerNode("localhost", 23, "token");
        assertThat(registry.registeredNodes()).isEqualTo(1);
        assertThat(nodeListener.getNodeContacts()).hasSize(1);

        // failing node
        try {
            registry.registerNode("localhost", 24, "token");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            // expected
        }
        assertThat(registry.registeredNodes()).isEqualTo(1);
        assertThat(nodeListener.getNodeContacts()).hasSize(1);
    }

    @Test
    void testFailingListener() {
        NodeListener nodeListener1 = mock(NodeListener.class);
        NodeListener nodeListener2 = mock(NodeListener.class);
        NodeListener nodeListener3 = mock(NodeListener.class);
        doThrow(new RuntimeException("test failure")).when(nodeListener2).addNode(ArgumentMatchers.any());

        NodeAcceptor nodeAcceptor = mock(NodeAcceptor.class);
        when(nodeAcceptor.accept("localhost", 23, "token")).thenReturn(Try.success(mock(NodeContact.class)));
        NodeRegistry registry = new NodeRegistry(nodeAcceptor, ImmutableSet.of(nodeListener1, nodeListener2, nodeListener3));
        registry.registerNode("localhost", 23, "token");
        assertThat(registry.registeredNodes()).isEqualTo(1);

        verify(nodeListener1).addNode(ArgumentMatchers.any());
        verify(nodeListener2).addNode(ArgumentMatchers.any());
    }

    private class CapturingNodeListener implements NodeListener {

        private final List<NodeContact> _nodeContacts = new ArrayList<>();

        public List<NodeContact> getNodeContacts() {
            return _nodeContacts;
        }

        @Override
        public void addNode(NodeContact nodeContact) {
            _nodeContacts.add(nodeContact);
        }

    }

}

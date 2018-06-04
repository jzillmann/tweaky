package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.ConnectException;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.conductor.channel.NodeChannel;
import io.morethan.tweaky.conductor.channel.NodeChannelProvider;
import io.morethan.tweaky.node.NodeClient;

class NodeRegistryTest {

    @Test
    void testNodeRegistration() {
        NodeChannelProvider channelProvider = mock(NodeChannelProvider.class);
        mockNode(channelProvider, "localhost", 23, "my-cluster");
        mockNode(channelProvider, "localhost", 24, "my-cluster");
        mockUnreachableNode(channelProvider, "localhost", 25);
        mockNode(channelProvider, "localhost", 26, "my-cluster2");

        SingleTokenValidator tokenValidator = new SingleTokenValidator("my-cluster");
        NodeRegistry registry = new NodeRegistry(tokenValidator, new HostPortNameProvider(), channelProvider);
        assertThat(registry.registeredNodes()).isEqualTo(0);

        // Register node with wrong token
        try {
            registry.registerNode("localhost", 23, "jerries-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Invalid token");
        }
        assertThat(registry.registeredNodes()).isEqualTo(0);

        // Register 2 nodes successfully
        registry.registerNode("localhost", 23, "my-cluster");
        assertThat(registry.registeredNodes()).isEqualTo(1);
        registry.registerNode("localhost", 24, "my-cluster");
        assertThat(registry.registeredNodes()).isEqualTo(2);

        // Register already registered node again
        try {
            registry.registerNode("localhost", 24, "my-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Name already given");
        }

        // Register node with unreachable server
        try {
            registry.registerNode("localhost", 25, "my-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Could not establish channel").hasMessageContaining("localhost:25");
        }

        // Register node with wrong token of server
        try {
            registry.registerNode("localhost", 26, "my-cluster");
            fail("should throw exception");
        } catch (NodeRejectedException e) {
            assertThat(e).hasMessageContaining("Remote token does not match").hasMessageContaining("my-cluster / my-cluster2");
        }
    }

    private void mockUnreachableNode(NodeChannelProvider channelProvider, String host, int port) {
        when(channelProvider.get(host, port)).thenThrow(new RuntimeException(new ConnectException("Connection refused")));
    }

    private void mockNode(NodeChannelProvider channelProvider, String host, int port, String token) {
        NodeClient nodeClient = mock(NodeClient.class);
        when(nodeClient.token()).thenReturn(token);

        NodeChannel nodeChannel = mock(NodeChannel.class, RETURNS_DEEP_STUBS);
        when(nodeChannel.nodeClient()).thenReturn(nodeClient);
        when(channelProvider.get(host, port)).thenReturn(nodeChannel);
    }

}

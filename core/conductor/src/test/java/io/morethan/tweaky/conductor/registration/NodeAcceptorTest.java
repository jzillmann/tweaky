package io.morethan.tweaky.conductor.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ConnectException;

import org.junit.jupiter.api.Test;

import io.grpc.ManagedChannel;
import io.morethan.tweaky.conductor.util.Try;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.node.NodeClient;

class NodeAcceptorTest {

    @Test
    void test() {
        ChannelProvider channelProvider = mock(ChannelProvider.class);
        NodeClientProvider clientProvider = mock(NodeClientProvider.class);
        mockNode(channelProvider, clientProvider, "localhost", 23, "my-cluster");
        mockNode(channelProvider, clientProvider, "localhost", 24, "my-cluster");
        mockUnreachableNode(channelProvider, "localhost", 25);
        mockNode(channelProvider, clientProvider, "localhost", 26, "my-cluster2");

        SingleTokenValidator tokenValidator = new SingleTokenValidator("my-cluster");
        NodeAcceptor nodeAcceptor = new NodeAcceptor(tokenValidator, NodeNameProvider.hostPort(), channelProvider, clientProvider);

        // Register node with wrong token
        Try<NodeContact, String> tryAccept = nodeAcceptor.accept("localhost", 23, "jerries-cluster");
        assertThat(tryAccept.failure()).contains("Invalid token");

        // Register 2 nodes successfully
        assertThat(nodeAcceptor.accept("localhost", 23, "my-cluster").isSuccess()).isTrue();
        assertThat(nodeAcceptor.accept("localhost", 24, "my-cluster").isSuccess()).isTrue();

        // Register already registered node again
        tryAccept = nodeAcceptor.accept("localhost", 24, "my-cluster");
        assertThat(tryAccept.failure()).contains("Name already taken");

        // Register node with unreachable server
        tryAccept = nodeAcceptor.accept("localhost", 25, "my-cluster");
        assertThat(tryAccept.failure()).contains("Could not establish channel").contains("localhost:25");

        // Register node with wrong token of server
        tryAccept = nodeAcceptor.accept("localhost", 26, "my-cluster");
        assertThat(tryAccept.failure()).contains("Remote token does not match").contains("my-cluster / my-cluster2");
    }

    private void mockUnreachableNode(ChannelProvider channelProvider, String host, int port) {
        when(channelProvider.get(host, port)).thenThrow(new RuntimeException(new ConnectException("Connection refused")));
    }

    private void mockNode(ChannelProvider channelProvider, NodeClientProvider clientProvider, String host, int port, String token) {
        ManagedChannel nodeChannel = mock(ManagedChannel.class);
        when(channelProvider.get(host, port)).thenReturn(nodeChannel);

        NodeClient nodeClient = mock(NodeClient.class);
        when(nodeClient.token()).thenReturn(token);

        when(clientProvider.get(nodeChannel)).thenReturn(nodeClient);
    }

}

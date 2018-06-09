package io.morethan.tweaky.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.proto.NodeGrpc;

class NodeIntegrationTest {

    // TODO shutdown helper ?

    @Test
    void test() {
        NodeComponent component = NodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token("token9")
                .conductorHost("localhost")
                .conductorPort(1234)
                .channelProvider(ChannelProvider.plaintext())
                .autoRegister(false)
                .build();
        GrpcServer server = component.server();
        server.startAsync().awaitRunning();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", server.getPort()));) {
            NodeClient client = NodeClient.on(channel);
            assertThat(client.token()).isEqualTo("token9");
            assertThat(client.serverServices()).contains(NodeGrpc.SERVICE_NAME);
        }

        try {
            component.nodeRegisterer().register();
            fail("should throw exception");
        } catch (Exception e) {
            assertThat(e).hasRootCauseInstanceOf(java.net.ConnectException.class);
        }
        server.stopAsync().awaitTerminated();

    }

    @Test
    void testAutoRegister() {
        NodeComponent component = NodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token("token9")
                .conductorHost("localhost")
                .conductorPort(1234)
                .channelProvider(ChannelProvider.plaintext())
                .autoRegister(true)
                .build();
        GrpcServer server = component.server();
        server.startAsync().awaitRunning();

        // NodeRegisterer fails because of missing master
        server.awaitTerminated();
    }

}
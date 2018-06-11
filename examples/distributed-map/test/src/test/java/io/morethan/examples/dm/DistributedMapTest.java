package io.morethan.examples.dm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.morethan.examples.dm.gateway.GatewayComponent;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.examples.dm.node.MapNodeComponent;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.node.NodeComponent;

public class DistributedMapTest {

    // TODO shutdown helper ?

    @Test
    void test() {
        ConductorComponent gatewayComponent = GatewayComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();
        GrpcServer gatewayServer = gatewayComponent.server();
        gatewayServer.startAsync().awaitRunning();

        NodeComponent node1Component = MapNodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token("doesn't matter")
                .conductorHost("localhost")
                .conductorPort(gatewayServer.getPort())
                .channelProvider(ChannelProvider.plaintext())
                .autoRegister(true)
                .build();

        NodeComponent node2Component = MapNodeComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .token("doesn't matter")
                .conductorHost("localhost")
                .conductorPort(gatewayServer.getPort())
                .channelProvider(ChannelProvider.plaintext())
                .autoRegister(true)
                .build();

        node1Component.server().startAsync().awaitRunning();
        node2Component.server().startAsync().awaitRunning();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", gatewayServer.getPort()));) {
            assertThat(ConductorClient.on(channel).serverServices()).contains(GatewayGrpc.SERVICE_NAME);
            assertThat(NodeRegistryClient.on(channel).nodeCount()).isEqualTo(2);

            GatewayClient gatewayClient = GatewayClient.on(channel);
            try (Inserter inserter = gatewayClient.createInserter();) {
                inserter.put("A", "1");
                inserter.put("A", "2");
                inserter.put("B", "3");
            }
        }

        gatewayServer.stopAsync().awaitTerminated();
    }
}

package io.morethan.examples.dm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.morethan.examples.dm.gateway.GatewayComponent;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

class GatewayIntegrationTest {

    // TODO shutdown helper ?

    @Test
    void test() {
        ConductorComponent gatewayComponent = GatewayComponent.builder()
                .nodeCount(0)
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();
        GrpcServer server = gatewayComponent.server();
        server.startAsync().awaitRunning();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", server.getPort()));) {
            assertThat(ConductorClient.on(channel).serverServices()).contains(GatewayGrpc.SERVICE_NAME);
        }

        server.stopAsync().awaitTerminated();
    }

}

package io.morethan.examples.dm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.morethan.examples.dm.GatewayClient.Inserter;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.grpc.GrpcServicesModule;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

class GatewayIntegrationTest {
    // TODO shutdown helper ?
    @Test
    void test() {
        GrpcServer conductorServer = ConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .grpcServiceModule(new GrpcServicesModule().with(new GatewayGrpcService()))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build()
                .conductorServer();
        conductorServer.startAsync().awaitRunning();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", conductorServer.getPort()));) {
            assertThat(ConductorClient.on(channel).serverServices()).contains(GatewayGrpc.SERVICE_NAME);

            GatewayClient gatewayClient = GatewayClient.on(channel);
            try (Inserter inserter = gatewayClient.createInserter();) {
                inserter.put("A", "1");
                inserter.put("A", "2");
                inserter.put("B", "3");
            }
        }

        conductorServer.stopAsync().awaitTerminated();
    }

}

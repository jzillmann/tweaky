package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.proto.ConductorGrpc;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.proto.NodeRegistryGrpc;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ConductorIntegrationTest {

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testServices() throws Exception {
        GrpcServer conductorServer = _shutdownHelper.register(ConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build()
                .conductorServer());
        conductorServer.startAsync().awaitRunning();

        try (ClosableChannel channel = ClosableChannel.of(ChannelProvider.plaintext().get("localhost", conductorServer.getPort()));) {
            assertThat(ConductorClient.on(channel).serverServices()).containsOnly(ConductorGrpc.SERVICE_NAME, NodeRegistryGrpc.SERVICE_NAME);
            assertThat(NodeRegistryClient.on(channel).nodeCount()).isEqualTo(0);
        }
    }
}

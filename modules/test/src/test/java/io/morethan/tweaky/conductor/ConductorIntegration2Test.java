package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.grpc.GrpcClient;
import io.morethan.tweaky.grpc.GrpcServer;
import io.morethan.tweaky.grpc.GrpcServerModule;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class ConductorIntegration2Test {

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void testNodeCount() throws Exception {
        GrpcServer conductorServer = _shutdownHelper.register(ConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build()
                .conductorServer());
        conductorServer.startAsync().awaitRunning();

        try (ConductorClient client = new ConductorClient(GrpcClient.standalone("localhost", conductorServer.getPort()))) {
            assertThat(client.nodeCount()).isEqualTo(0);
        }
    }
}

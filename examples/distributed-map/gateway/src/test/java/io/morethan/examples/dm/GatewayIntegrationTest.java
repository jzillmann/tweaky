package io.morethan.examples.dm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.morethan.examples.dm.GatewayClient.Inserter;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.ConductorComponent;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

// Dagger composition : 2 possibilities
// - (A) Have that Set<BindableService> services(ConductorContext context) in an abstract module
//      - In there, we can create another component with the apps object graph, injecting the context
//      - (+) Control over plug-ability / well defined API (maybe easier for the user to grasp, but also more restricted)
//      - (-) Design effort of plug-ability
//      - (-) Hacky dagger usage (a lot of context dependencies or have a context de-construction module)
// - (B) Re-do the component, just re-use the modules
//      - See if we can extend ConductorComponent
//      - (+) Full power of dagger at your hand
//      - (-) Harder to get started, Repeating stuff already done
//

class GatewayIntegrationTest {

    // TODO shutdown helper ?

    @Test
    void test() {
        ConductorComponent conductorComponent = ConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.plaintext(0))
                // .grpcServiceModule(new GrpcServicesModule().with(new GatewayGrpcService()))
                .appModule(new GatewayModule())
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();
        GrpcServer conductorServer = conductorComponent
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

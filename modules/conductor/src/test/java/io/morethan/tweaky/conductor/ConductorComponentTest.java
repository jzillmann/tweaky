package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.Service.State;

import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.conductor.registration.NodeRegistry;
import io.morethan.tweaky.grpc.GrpcClient;
import io.morethan.tweaky.grpc.GrpcServer;
import io.morethan.tweaky.grpc.GrpcServerModule;

class ConductorComponentTest {

    @Test
    void test() {
        ConductorComponent conductorCompenent = DaggerConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.inProcess("server"))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();

        NodeRegistry nodeRegistry = conductorCompenent.nodeRegistry();
        Conductor conductor = conductorCompenent.conductor();
        GrpcServer conductorServer = conductorCompenent.conductorServer();
        assertThat(nodeRegistry).isNotNull();
        assertThat(conductor).isNotNull();
        assertThat(conductorServer).isNotNull();

        // check singletons
        assertThat(nodeRegistry).isSameAs(conductorCompenent.nodeRegistry());
        assertThat(conductor).isSameAs(conductorCompenent.conductor());
        assertThat(conductorServer).isSameAs(conductorCompenent.conductorServer());

        // check state
        assertThat(nodeRegistry.registeredNodes()).isEqualTo(0);
        assertThat(conductor.nodeCount()).isEqualTo(0);
        assertThat(conductorServer.state()).isEqualTo(State.NEW);

        // start server
        conductorCompenent.conductorServer().startAsync().awaitRunning();
        assertThat(nodeRegistry.registeredNodes()).isEqualTo(0);
        assertThat(conductor.nodeCount()).isEqualTo(0);
        assertThat(conductorServer.state()).isEqualTo(State.RUNNING);

        // talk to server
        try (ConductorClient conductorClient = new ConductorClient(GrpcClient.standaloneInProcess("server"))) {
            assertThat(conductorClient.nodeCount()).isEqualTo(0);
        }

        // stop server
        conductorCompenent.conductorServer().stopAsync().awaitTerminated();
        assertThat(conductorServer.state()).isEqualTo(State.TERMINATED);
    }

}

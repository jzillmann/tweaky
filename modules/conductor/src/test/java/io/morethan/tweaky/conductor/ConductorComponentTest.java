package io.morethan.tweaky.conductor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.Service.State;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;

class ConductorComponentTest {

    @Test
    void test() {
        ConductorComponent conductorCompenent = DaggerConductorComponent.builder()
                .grpcServerModule(GrpcServerModule.inProcess("server"))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();

        GrpcServer conductorServer = conductorCompenent.server();
        assertThat(conductorServer).isNotNull();

        // check singletons
        assertThat(conductorServer).isSameAs(conductorCompenent.server());

        // check state
        assertThat(conductorServer.state()).isEqualTo(State.NEW);

        // start server
        conductorCompenent.server().startAsync().awaitRunning();
        assertThat(conductorServer.state()).isEqualTo(State.RUNNING);

        // talk to server
        try (ClosableChannel channel = ClosableChannel.of(InProcessChannelBuilder.forName("server").build());) {
            ConductorClient conductorClient = ConductorClient.on(channel);
            assertThat(conductorClient.serverServices()).isNotEmpty();
        }

        // stop server
        conductorCompenent.server().stopAsync().awaitTerminated();
        assertThat(conductorServer.state()).isEqualTo(State.TERMINATED);
    }

}

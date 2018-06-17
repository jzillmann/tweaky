package io.morethan.tweaky.noderegistry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.google.common.util.concurrent.Service.State;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.client.ServiceRegistryClient;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.noderegistry.DaggerNodeRegistryComponent;
import io.morethan.tweaky.noderegistry.NodeNameProvider;
import io.morethan.tweaky.noderegistry.NodeRegistrationValidator;
import io.morethan.tweaky.noderegistry.NodeRegistryComponent;

class NodeRegistryComponentTest {

    @Test
    void test() {
        NodeRegistryComponent compenent = DaggerNodeRegistryComponent.builder()
                .grpcServerModule(GrpcServerModule.inProcess("server"))
                .nodeNameProvider(NodeNameProvider.hostPort())
                .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                .build();

        GrpcServer server = compenent.server();
        assertThat(server).isNotNull();

        // check singletons
        assertThat(server).isSameAs(compenent.server());

        // check state
        assertThat(server.state()).isEqualTo(State.NEW);

        // start server
        compenent.server().startAsync().awaitRunning();
        assertThat(server.state()).isEqualTo(State.RUNNING);

        // talk to server
        try (ClosableChannel channel = ClosableChannel.of(InProcessChannelBuilder.forName("server").build());) {
            ServiceRegistryClient servicesClient = ServiceRegistryClient.on(channel);
            assertThat(servicesClient.services()).isNotEmpty();
        }

        // stop server
        compenent.server().stopAsync().awaitTerminated();
        assertThat(server.state()).isEqualTo(State.TERMINATED);
    }

}

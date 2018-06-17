package io.morethan.tweaky.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.morethan.examples.dm.GatewayClient;
import io.morethan.examples.dm.Inserter;
import io.morethan.examples.dm.gateway.GatewayComponent;
import io.morethan.tweaky.conductor.ConductorClient;
import io.morethan.tweaky.conductor.NodeRegistryClient;
import io.morethan.tweaky.conductor.registration.NodeNameProvider;
import io.morethan.tweaky.conductor.registration.NodeRegistrationValidator;
import io.morethan.tweaky.examples.dm.gateway.proto.GatewayGrpc;
import io.morethan.tweaky.examples.dm.node.MapNodeComponent;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.grpc.server.GrpcServer;
import io.morethan.tweaky.grpc.server.GrpcServerModule;
import io.morethan.tweaky.test.TestCluster;
import io.morethan.tweaky.testsupport.ShutdownHelper;

public class DistributedMapTest {

    @RegisterExtension
    ShutdownHelper _shutdownHelper = new ShutdownHelper();

    @Test
    void test() throws InterruptedException {
        TestCluster cluster = _shutdownHelper.register(new TestCluster(2, ChannelProvider.plaintext()) {

            @Override
            protected GrpcServer createConductor() {
                return GatewayComponent.builder()
                        .nodeCount(2)
                        .grpcServerModule(GrpcServerModule.plaintext(0))
                        .nodeNameProvider(NodeNameProvider.hostPort())
                        .nodeRegistrationValidator(NodeRegistrationValidator.acceptAll())
                        .build().server();
            }

            @Override
            protected GrpcServer createNode(int number, int conductorPort, ChannelProvider channelProvider) {
                return MapNodeComponent.builder()
                        .grpcServerModule(GrpcServerModule.plaintext(0))
                        .token("doesn't matter")
                        .conductorHost("localhost")
                        .conductorPort(conductorPort)
                        .autoRegister(true)
                        .build().server();
            }
        });

        cluster.boot().awaitNodes();
        try (ClosableChannel channel = cluster.channelToConductor();) {
            assertThat(ConductorClient.on(channel).serverServices()).contains(GatewayGrpc.SERVICE_NAME);
            assertThat(NodeRegistryClient.on(channel).nodeCount()).isEqualTo(2);

            GatewayClient gatewayClient = GatewayClient.on(channel);
            try (Inserter inserter = gatewayClient.createInserter();) {
                inserter.put("key1", "1");
                inserter.put("key2", "2");
                inserter.put("key3", "3");
                inserter.put("key4", "4");

                inserter.put("key1", "1.1");
            }
            assertThat(gatewayClient.get("key1")).isEqualTo("1.1");
            assertThat(gatewayClient.get("key2")).isEqualTo("2");
            assertThat(gatewayClient.get("key3")).isEqualTo("3");
            assertThat(gatewayClient.get("key4")).isEqualTo("4");
        }
    }
}

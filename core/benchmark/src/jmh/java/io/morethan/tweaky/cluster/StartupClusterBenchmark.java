package io.morethan.tweaky.cluster;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.noderegistry.NodeRegistryClient;
import io.morethan.tweaky.test.cluster.PlainMultiTokenCluster;
import io.morethan.tweaky.test.cluster.PlainSingleTokenCluster;
import io.morethan.tweaky.test.cluster.TestCluster;

@Fork(value = 5)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class StartupClusterBenchmark {

    @Param({ "1", "5", "10", "20" })
    public int nodes;

    @Param({ "single", "perNode" })
    public String tokenType;

    private TestCluster _cluster;

    @Setup(Level.Iteration)
    public void before() throws Exception {
        if (tokenType.equals("single")) {
            _cluster = new PlainSingleTokenCluster(nodes, ChannelProvider.plaintext(), UUID.randomUUID().toString());
        } else if (tokenType.equals("perNode")) {
            _cluster = new PlainMultiTokenCluster(nodes, ChannelProvider.plaintext());
        } else {
            throw new UnsupportedOperationException(tokenType);
        }
    }

    @TearDown(Level.Iteration)
    public void after() throws Exception {
        _cluster.close();
    }

    @Benchmark
    public void startup() throws Exception {
        _cluster.boot().awaitNodes();
        NodeRegistryClient registryClient = NodeRegistryClient.on(_cluster.channelToNodeRegistry());
        registryClient.nodeCount();
    }
}

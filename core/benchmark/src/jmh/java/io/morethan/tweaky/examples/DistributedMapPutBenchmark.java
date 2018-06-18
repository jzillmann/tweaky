package io.morethan.tweaky.examples;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import io.morethan.examples.dm.GatewayClient;
import io.morethan.examples.dm.Inserter;
import io.morethan.tweaky.grpc.client.ChannelProvider;
import io.morethan.tweaky.grpc.client.ClosableChannel;
import io.morethan.tweaky.test.cluster.DistributedMapCluster;
import io.morethan.tweaky.test.cluster.TestCluster;
import io.morethan.tweaky.test.datagen.DataGen;

@Fork(value = 2)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class DistributedMapPutBenchmark {

    private final Iterator<String> _randomStrings = DataGen.stringsOfLength(new Random(23), 5).iterator();

    @Param({ "10", "100", "1000" })
    public int insertCounts;

    private TestCluster _cluster;
    private ClosableChannel _channelToGateway;
    private GatewayClient _gatewayClient;

    @Setup
    public void before() throws Exception {
        _cluster = new DistributedMapCluster(10, ChannelProvider.plaintext(), UUID.randomUUID().toString());
        _cluster.boot().awaitNodes();
        _channelToGateway = _cluster.channelToNodeRegistry();
        _gatewayClient = GatewayClient.on(_channelToGateway);
    }

    @TearDown
    public void after() throws Exception {
        _channelToGateway.close();
        _cluster.close();
    }

    @Benchmark
    public void inserter() throws Exception {
        try (Inserter inserter = _gatewayClient.createInserter();) {
            for (int i = 0; i < insertCounts; i++) {
                inserter.put(_randomStrings.next(), _randomStrings.next());
            }
        }
    }

    @Benchmark
    public void put() throws Exception {
        for (int i = 0; i < insertCounts; i++) {
            _gatewayClient.put(_randomStrings.next(), _randomStrings.next());
        }
    }

}

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
import org.openjdk.jmh.infra.Control;

import com.google.common.base.Stopwatch;

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
public class DistributedMapInserterBenchmark {

    private final Iterator<String> _randomStrings = DataGen.stringsOfLength(new Random(23), 5).iterator();

    @Param({ "1", "5", "10" })
    public int nodes;

    private TestCluster _cluster;
    private ClosableChannel _channelToGateway;
    private Inserter _inserter;

    @Setup
    public void before() throws Exception {
        _cluster = new DistributedMapCluster(nodes, ChannelProvider.plaintext(), UUID.randomUUID().toString());
        _cluster.boot().awaitNodes();
        _channelToGateway = _cluster.channelToNodeRegistry();
        _inserter = GatewayClient.on(_channelToGateway).createInserter();
    }

    @TearDown
    public void after() throws Exception {
        _channelToGateway.close();
        _cluster.close();
        Stopwatch stopwatch = Stopwatch.createStarted();
        _inserter.close();
        System.out.println("Took " + stopwatch + " to close the inserter");
    }

    @Benchmark
    public void inserter(Control control) throws Exception {
        if (control.stopMeasurement) {
            throw new IllegalStateException(",,,");
        }
        _inserter.put(_randomStrings.next(), _randomStrings.next());
    }

}

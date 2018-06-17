package io.morethan.tweaky.noderegistry;

/**
 * A {@link NodeNameProvider} which assigns 'host:port' as node name.
 */
public class HostPortNameProvider implements NodeNameProvider {

    @Override
    public String getName(String host, int port, String token) {
        return new StringBuilder(host).append(':').append(port).toString();
    }

}

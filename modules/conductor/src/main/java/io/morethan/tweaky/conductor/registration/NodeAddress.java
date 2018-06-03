package io.morethan.tweaky.conductor.registration;

import com.google.common.base.MoreObjects;

/**
 * Contains information about a node.
 */
public class NodeAddress {

    private final String _host;
    private final int _port;
    private final String _token;

    public NodeAddress(String host, int port, String token) {
        _host = host;
        _port = port;
        _token = token;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public String getToken() {
        return _token;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("host", _host).add("port", _port).add("token", _token).toString();
    }

}

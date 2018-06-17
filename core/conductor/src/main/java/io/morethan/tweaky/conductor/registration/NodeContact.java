package io.morethan.tweaky.conductor.registration;

import com.google.common.base.MoreObjects;

import io.grpc.Channel;

/**
 * Contains information about a node.
 */
public class NodeContact {

    private final String _name;
    private final String _host;
    private final int _port;
    private final String _token;
    private final Channel _channel;

    public NodeContact(String name, String host, int port, String token, Channel channel) {
        _name = name;
        _host = host;
        _port = port;
        _token = token;
        _channel = channel;
    }

    public String name() {
        return _name;
    }

    public String host() {
        return _host;
    }

    public int port() {
        return _port;
    }

    public String token() {
        return _token;
    }

    public Channel channel() {
        return _channel;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", _name)
                .add("token", _token)
                .add("host", _host)
                .add("port", _port)
                .toString();
    }

}

package io.morethan.tweaky.noderegistry;

/**
 * Responsible to give a registering node a name. Provides names should be unique.
 */
public interface NodeNameProvider {

    String getName(String host, int port, String token);

    public static NodeNameProvider hostPort() {
        return new HostPortNameProvider();
    }
}

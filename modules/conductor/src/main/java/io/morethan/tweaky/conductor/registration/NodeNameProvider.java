package io.morethan.tweaky.conductor.registration;

/**
 * Responsible to give a registering node a name. Provides names should be unique.
 */
public interface NodeNameProvider {

    String getName(String host, int port, String token);

}

package io.morethan.tweaky.conductor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

//import org.apache.log4j.Logger;

import io.grpc.Metadata;
import io.grpc.Metadata.BinaryMarshaller;
import io.grpc.Metadata.Key;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Utility that helps piping exceptions from the server to client with GRPC.
 */
public class Errors {

    // private static final Logger LOG = Logger.getLogger(Errors.class);

    private static final Key<RuntimeException> CAUSING_EXCEPTION = Key.of("causing-exception-bin", new BinaryMarshaller<RuntimeException>() {

        @Override
        public byte[] toBytes(RuntimeException value) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                out.writeObject(value);
                return outputStream.toByteArray();
            } catch (IOException e) {
                // TODO logging
                // LOG.warn("Could not serialize exception", e);
                return new byte[0];
            }
        }

        @Override
        public RuntimeException parseBytes(byte[] serialized) {
            if (serialized.length == 0) {
                return null;
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(serialized);
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                return (RuntimeException) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                // TODO logging
                // LOG.warn("Could not de-serialize exception", e);
            }
            return null;
        }

    });

    /**
     * You can use this to wrap an exception happening within a server RPC call and send it back to the client. The client can then unwrap the exception via xxx.
     * 
     * @param status
     * @param cause
     * @return
     */
    public static RuntimeException newRpcError(Status status, RuntimeException cause) {
        Metadata metadata = new Metadata();
        metadata.put(Errors.CAUSING_EXCEPTION, cause);
        return status.withDescription(cause.getMessage()).asRuntimeException(metadata);
    }

    public static RuntimeException unwrap(StatusRuntimeException statusRuntimeException) {
        RuntimeException exception = statusRuntimeException.getTrailers().get(CAUSING_EXCEPTION);
        if (exception == null) {
            return statusRuntimeException;
        }
        return exception;
    }
}

package io.morethan.tweaky.grpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.Metadata.BinaryMarshaller;
import io.grpc.Metadata.Key;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

/**
 * Utility that helps piping exceptions from the server to client with GRPC.
 */
public class Errors {

    private static final Logger LOG = LoggerFactory.getLogger(Errors.class);

    private static final Key<RuntimeException> CAUSING_EXCEPTION = Key.of("causing-exception-bin", new BinaryMarshaller<RuntimeException>() {

        @Override
        public byte[] toBytes(RuntimeException value) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                out.writeObject(value);
                return outputStream.toByteArray();
            } catch (IOException e) {
                LOG.warn("Could not serialize exception", e);
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
                LOG.warn("Could not de-serialize exception", e);
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

    public static RuntimeException unwrapped(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            return unwrapped((StatusRuntimeException) throwable);
        }
        if (throwable instanceof StatusException) {
            return unwrapped((StatusException) throwable);
        }
        return new RemoteCallFailure(throwable);
    }

    public static RuntimeException unwrapped(StatusException exception) {
        return new RemoteCallFailure(unwrap(exception, exception.getTrailers()));
    }

    public static RuntimeException unwrapped(StatusRuntimeException exception) {
        return new RemoteCallFailure(unwrap(exception, exception.getTrailers()));
    }

    private static Throwable unwrap(Throwable throwable, Metadata metadata) {
        RuntimeException exception = metadata.get(CAUSING_EXCEPTION);
        if (exception == null) {
            return throwable;
        }
        return exception;
    }

    public static boolean isCancelled(Throwable throwable) {
        Optional<Code> statusCode = getStatusCode(throwable);
        return statusCode.isPresent() && statusCode.get() == Status.Code.CANCELLED;
    }

    public static Optional<Status.Code> getStatusCode(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException) {
            return Optional.of(((StatusRuntimeException) throwable).getStatus().getCode());
        }
        if (throwable instanceof StatusException) {
            return Optional.of(((StatusException) throwable).getStatus().getCode());
        }
        return Optional.empty();
    }
}

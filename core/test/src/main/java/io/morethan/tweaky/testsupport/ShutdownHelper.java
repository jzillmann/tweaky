package io.morethan.tweaky.testsupport;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;

/**
 * A {@link ExternalResource} which takes care of closing registered {@link Closeable}s and sorts alike.
 */
public class ShutdownHelper extends ExternalResource {

    private static final Logger LOG = LoggerFactory.getLogger(ShutdownHelper.class);
    private final List<AutoCloseable> _closables = new ArrayList<>();
    private final List<Service> _services = new ArrayList<>();

    @Override
    protected void before(ExtensionContext context) throws Exception {
        // nothing to do
    }

    @Override
    protected void after(ExtensionContext context) throws Exception {
        for (AutoCloseable closeable : _closables) {
            try {
                closeable.close();
            } catch (Exception e) {
                LOG.error("Failed to close " + closeable, e);
            }
        }
        _closables.clear();

        for (Service service : _services) {
            try {
                service.stopAsync().awaitTerminated();
            } catch (Exception e) {
                LOG.error("Failed to close " + service, e);
            }
        }
        _services.clear();
    }

    public <T extends AutoCloseable> T register(T closeable) {
        _closables.add(closeable);
        return closeable;
    }

    public <T extends Service> T register(T service) {
        _services.add(service);
        return service;
    }

}

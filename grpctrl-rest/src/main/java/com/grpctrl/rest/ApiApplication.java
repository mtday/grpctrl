package com.grpctrl.rest;

import com.grpctrl.common.supplier.ObjectMapperSupplier;
import com.grpctrl.rest.providers.GenericExceptionMapper;
import com.grpctrl.rest.providers.MemoryUsageLogger;
import com.grpctrl.rest.providers.RequestLoggingFilter;
import com.grpctrl.rest.resource.auth.Login;
import com.grpctrl.rest.resource.auth.Logout;
import com.grpctrl.rest.resource.v1.account.AccountAdd;
import com.grpctrl.rest.resource.v1.account.AccountGet;
import com.grpctrl.rest.resource.v1.account.AccountGetAll;
import com.grpctrl.rest.resource.v1.account.AccountRemove;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.ApplicationPath;

/**
 * Provides the infrastructure (dependency injection and configuration) for the Jersey REST-based application that
 * hosts the API for this system.
 */
@ApplicationPath("/")
public class ApiApplication extends ResourceConfig {
    /**
     * Default constructor.
     */
    public ApiApplication() {
        // So Jersey will use our ObjectMapper configuration.
        register(ObjectMapperSupplier.class);

        // Resource classes.
        register(AccountAdd.class);
        register(AccountRemove.class);
        register(AccountGet.class);
        register(AccountGetAll.class);
        register(Login.class);
        register(Logout.class);

        // Define who can access which resources using annotations.
        register(RolesAllowedDynamicFeature.class);

        // Log requests as they come in.
        register(RequestLoggingFilter.class);

        // Log memory usage stats.
        register(MemoryUsageLogger.class);

        // Handle exceptions.
        register(GenericExceptionMapper.class);

        // Turn on gzip compression.
        EncodingFilter.enableFor(this, GZipEncoder.class);
    }
}

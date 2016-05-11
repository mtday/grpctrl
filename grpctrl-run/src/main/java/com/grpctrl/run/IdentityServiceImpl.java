package com.grpctrl.run;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.security.auth.Subject;

public class IdentityServiceImpl implements IdentityService {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityServiceImpl.class);

    @Override
    public Object associate(@Nonnull final UserIdentity userIdentity) {
        LOG.info("associate");
        LOG.info("  userIdentity: {}", userIdentity);
        return null;
    }

    @Override
    public void disassociate(@Nonnull final Object previous) {
        LOG.info("disassociate");
        LOG.info("  previous: {}", previous);
    }

    @Override
    public Object setRunAs(@Nonnull final UserIdentity userIdentity, @Nonnull final RunAsToken token) {
        LOG.info("setRunAs");
        LOG.info("  userIdentity: {}", userIdentity);
        LOG.info("  token: {}", token);
        return null;
    }

    @Override
    public void unsetRunAs(@Nonnull final Object token) {
        LOG.info("unsetRunAs");
        LOG.info("  token: {}", token);
    }

    @Override
    public UserIdentity newUserIdentity(
            @Nonnull final Subject subject, @Nonnull final Principal userPrincipal, @Nonnull final String[] roles) {
        LOG.info("newUserIdentity");
        LOG.info("  subject: {}", subject);
        LOG.info("  userPrincipal: {}", userPrincipal);
        LOG.info("  roles: {}", Arrays.asList(roles));
        return null;
    }

    @Override
    public RunAsToken newRunAsToken(@Nonnull final String runAsName) {
        LOG.info("newRunAsToken");
        LOG.info("  runAsName: {}", runAsName);
        return null;
    }

    @Override
    public UserIdentity getSystemUserIdentity() {
        LOG.info("getSystemUserIdentity");
        return null;
    }
}

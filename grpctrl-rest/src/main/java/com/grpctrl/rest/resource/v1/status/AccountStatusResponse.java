package com.grpctrl.rest.resource.v1.status;

import com.grpctrl.common.model.Account;
import com.grpctrl.common.model.User;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;

public class AccountStatusResponse {
    @Nullable
    private final Account account;

    @Nullable
    private final User user;

    public AccountStatusResponse() {
        this(null, null);
    }

    public AccountStatusResponse(@Nullable final Account account, @Nullable final User user) {
        this.account = account;
        this.user = user;
    }

    @Nullable
    public Account getAccount() {
        return this.account;
    }

    @Nullable
    public User getUser() {
        return this.user;
    }

    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("account", getAccount());
        str.append("user", getUser());
        return str.build();
    }
}

package com.tsu.namespace.record;

import com.tsu.auth.api.AuthLogin;
import com.tsu.auth.api.BasePrincipal;
import com.tsu.common.jpa.JsonValueUtils;
import com.tsu.auth.security.AppSecurityContext;
import com.tsu.namespace.entities.LoginTb;
import lombok.Getter;
import lombok.ToString;

import java.util.function.Consumer;


@ToString(onlyExplicitlyIncluded = true)
public class LoginRecord {

    @ToString.Include
    private final LoginTb tb;
    private final Consumer<LoginTb> persist;

    @Getter
    private final AuthLogin authLogin;

    public LoginRecord(LoginTb tb, Consumer<LoginTb> persist) {
        this.tb = tb;
        this.authLogin = new AuthLogin(tb.getProvider(), tb.getAuthId());
        this.persist = persist;
    }

    public BasePrincipal getUserId() {
        return BasePrincipal.of(tb.getUserId());
    }

    public boolean isActive() {
        return tb.isActive();
    }

    public <T> T getProps(Class<T> type) {
        return JsonValueUtils.getInstance().decode(tb.getProps(), type);
    }

    public <T> void setProps(T props) {
        tb.setProps(JsonValueUtils.getInstance().encodeAsJson(props));
    }

    public void persist(AppSecurityContext context) {
        persist.accept(tb);
    }
}

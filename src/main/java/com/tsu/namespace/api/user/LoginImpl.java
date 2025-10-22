package com.tsu.namespace.api.user;

import com.tsu.auth.api.BasePrincipal;
import com.tsu.auth.api.AuthLogin;
import com.tsu.auth.api.Login;
import com.tsu.namespace.record.LoginRecord;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
public class LoginImpl implements Login {

    @ToString.Include
    private final LoginRecord record;


    @Override
    public BasePrincipal getUser() {
        return record.getUserId();
    }

    @Override
    public AuthLogin getLogin() {
        return record.getAuthLogin();
    }


}

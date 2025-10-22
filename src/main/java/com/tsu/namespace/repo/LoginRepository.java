package com.tsu.namespace.repo;

import com.tsu.auth.api.AuthProvider;
import com.tsu.namespace.entities.LoginTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginRepository extends JpaRepository<LoginTb, Integer> {


    Optional<LoginTb> findByProviderAndAuthId(AuthProvider providerId, String authId);


}

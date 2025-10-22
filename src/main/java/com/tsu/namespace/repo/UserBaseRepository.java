package com.tsu.namespace.repo;

import com.tsu.namespace.entities.UserBaseTb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBaseRepository extends JpaRepository<UserBaseTb, UUID> {


    Optional<UserBaseTb> findByEmail(String email);

}

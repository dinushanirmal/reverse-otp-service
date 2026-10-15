package com.example.reverseotp.repository;

import com.example.reverseotp.entity.TokenRequest;
import com.example.reverseotp.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRequestRepository extends JpaRepository<TokenRequest, String> {

    Optional<TokenRequest> findByRequestId(String requestId);

    List<TokenRequest> findByPhoneNumberAndStatus(String phoneNumber, TokenStatus status);
}

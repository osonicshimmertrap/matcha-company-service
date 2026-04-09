package az.matcha.company.repository;

import az.matcha.company.domain.HrInvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HrInvitationTokenRepository extends JpaRepository<HrInvitationToken, UUID> {

    Optional<HrInvitationToken> findByToken(String token);
}

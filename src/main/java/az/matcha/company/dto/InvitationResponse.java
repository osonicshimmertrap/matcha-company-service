package az.matcha.company.dto;

import az.matcha.company.domain.CompanyMemberRole;
import az.matcha.company.domain.HrInvitationToken;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID companyId,
        String token,
        UUID invitedByUserId,
        String inviteeEmail,
        CompanyMemberRole role,
        boolean used,
        Instant expiresAt,
        Instant createdAt
) {
    public static InvitationResponse from(HrInvitationToken t) {
        return new InvitationResponse(
                t.getId(),
                t.getCompany().getId(),
                t.getToken(),
                t.getInvitedByUserId(),
                t.getInviteeEmail(),
                t.getRole(),
                t.isUsed(),
                t.getExpiresAt(),
                t.getCreatedAt()
        );
    }
}

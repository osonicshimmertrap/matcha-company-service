package az.matcha.company.dto;

import az.matcha.company.domain.CompanyMember;
import az.matcha.company.domain.CompanyMemberRole;

import java.time.Instant;
import java.util.UUID;

public record CompanyMemberResponse(
        UUID id,
        UUID companyId,
        UUID userId,
        CompanyMemberRole role,
        Instant joinedAt
) {
    public static CompanyMemberResponse from(CompanyMember m) {
        return new CompanyMemberResponse(
                m.getId(),
                m.getCompany().getId(),
                m.getUserId(),
                m.getRole(),
                m.getJoinedAt()
        );
    }
}

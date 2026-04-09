package az.matcha.company.event;

import az.matcha.company.domain.CompanyMemberRole;

import java.time.Instant;
import java.util.UUID;

/**
 * Published to matcha.events exchange with routing key "company.member.joined"
 * so user-service (and audit-service) can react to new employer members.
 */
public record CompanyMemberJoinedEvent(
        UUID companyId,
        UUID userId,
        CompanyMemberRole role,
        Instant occurredAt
) {}

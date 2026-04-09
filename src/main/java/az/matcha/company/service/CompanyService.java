package az.matcha.company.service;

import az.matcha.company.client.UserServiceClient;
import az.matcha.company.domain.*;
import az.matcha.company.dto.*;
import az.matcha.company.event.CompanyMemberJoinedEvent;
import az.matcha.company.exception.BusinessException;
import az.matcha.company.exception.ForbiddenException;
import az.matcha.company.exception.ResourceNotFoundException;
import az.matcha.company.repository.CompanyMemberRepository;
import az.matcha.company.repository.CompanyRepository;
import az.matcha.company.repository.HrInvitationTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long INVITATION_TTL_HOURS = 72;

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository memberRepository;
    private final HrInvitationTokenRepository tokenRepository;
    private final UserServiceClient userServiceClient;
    private final EventPublisher eventPublisher;

    public CompanyService(CompanyRepository companyRepository,
                          CompanyMemberRepository memberRepository,
                          HrInvitationTokenRepository tokenRepository,
                          UserServiceClient userServiceClient,
                          EventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.memberRepository = memberRepository;
        this.tokenRepository = tokenRepository;
        this.userServiceClient = userServiceClient;
        this.eventPublisher = eventPublisher;
    }

    // ── Create company ────────────────────────────────────────────────────────

    public CompanyResponse createCompany(CreateCompanyRequest req, JwtAuthenticationToken auth) {
        UUID userId = UUID.fromString(auth.getName());

        Company company = Company.builder()
                .name(req.name())
                .description(req.description())
                .industry(req.industry())
                .sizeRange(req.sizeRange())
                .website(req.website())
                .logoUrl(req.logoUrl())
                .country(req.country())
                .city(req.city())
                .foundedYear(req.foundedYear())
                .headApprovesPostings(req.headApprovesPostings())
                .createdByUserId(userId)
                .build();

        company = companyRepository.save(company);

        // Founding member — OWNER
        CompanyMember owner = CompanyMember.builder()
                .company(company)
                .userId(userId)
                .role(CompanyMemberRole.OWNER)
                .build();
        memberRepository.save(owner);

        // Sync companyId back to user-service employer profile (non-fatal)
        userServiceClient.assignCompanyToEmployer(userId, company.getId());

        log.info("Company created: id={}, name={}, owner={}", company.getId(), company.getName(), userId);

        eventPublisher.publishMemberJoined(new CompanyMemberJoinedEvent(
                company.getId(), userId, CompanyMemberRole.OWNER, Instant.now()));

        return CompanyResponse.from(company);
    }

    // ── Get company ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(UUID companyId) {
        return CompanyResponse.from(findCompanyOrThrow(companyId));
    }

    // ── Update company ────────────────────────────────────────────────────────

    public CompanyResponse updateCompany(UUID companyId, UpdateCompanyRequest req, JwtAuthenticationToken auth) {
        UUID userId = UUID.fromString(auth.getName());
        Company company = findCompanyOrThrow(companyId);
        requireMembership(companyId, userId);

        if (req.name() != null) company.setName(req.name());
        if (req.description() != null) company.setDescription(req.description());
        if (req.industry() != null) company.setIndustry(req.industry());
        if (req.sizeRange() != null) company.setSizeRange(req.sizeRange());
        if (req.website() != null) company.setWebsite(req.website());
        if (req.logoUrl() != null) company.setLogoUrl(req.logoUrl());
        if (req.country() != null) company.setCountry(req.country());
        if (req.city() != null) company.setCity(req.city());
        if (req.foundedYear() != null) company.setFoundedYear(req.foundedYear());
        if (req.headApprovesPostings() != null) company.setHeadApprovesPostings(req.headApprovesPostings());

        return CompanyResponse.from(companyRepository.save(company));
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CompanyMemberResponse> listMembers(UUID companyId) {
        findCompanyOrThrow(companyId); // existence check
        return memberRepository.findAllByCompany_Id(companyId)
                .stream()
                .map(CompanyMemberResponse::from)
                .toList();
    }

    // ── Invitations ───────────────────────────────────────────────────────────

    public InvitationResponse createInvitation(UUID companyId, CreateInvitationRequest req,
                                               JwtAuthenticationToken auth) {
        UUID userId = UUID.fromString(auth.getName());
        Company company = findCompanyOrThrow(companyId);
        requireMembership(companyId, userId);

        String token = generateSecureToken();
        HrInvitationToken invitation = HrInvitationToken.builder()
                .company(company)
                .token(token)
                .invitedByUserId(userId)
                .inviteeEmail(req.inviteeEmail())
                .role(CompanyMemberRole.HR)
                .expiresAt(Instant.now().plus(INVITATION_TTL_HOURS, ChronoUnit.HOURS))
                .build();

        return InvitationResponse.from(tokenRepository.save(invitation));
    }

    public CompanyMemberResponse acceptInvitation(String token, JwtAuthenticationToken auth) {
        UUID userId = UUID.fromString(auth.getName());

        HrInvitationToken invitation = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invitation token"));

        if (invitation.isUsed()) {
            throw new BusinessException("Invitation token has already been used");
        }
        if (invitation.isExpired()) {
            throw new BusinessException("Invitation token has expired");
        }

        UUID companyId = invitation.getCompany().getId();
        if (memberRepository.existsByCompany_IdAndUserId(companyId, userId)) {
            throw new BusinessException("You are already a member of this company");
        }

        invitation.setUsed(true);
        tokenRepository.save(invitation);

        CompanyMember member = CompanyMember.builder()
                .company(invitation.getCompany())
                .userId(userId)
                .role(invitation.getRole())
                .build();
        member = memberRepository.save(member);

        // Sync companyId to user-service employer profile (non-fatal)
        userServiceClient.assignCompanyToEmployer(userId, companyId);

        eventPublisher.publishMemberJoined(new CompanyMemberJoinedEvent(
                companyId, userId, invitation.getRole(), Instant.now()));

        log.info("User {} joined company {} via invitation as {}", userId, companyId, invitation.getRole());

        return CompanyMemberResponse.from(member);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Company findCompanyOrThrow(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + companyId));
    }

    private void requireMembership(UUID companyId, UUID userId) {
        if (!memberRepository.existsByCompany_IdAndUserId(companyId, userId)) {
            throw new ForbiddenException("You are not a member of this company");
        }
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}

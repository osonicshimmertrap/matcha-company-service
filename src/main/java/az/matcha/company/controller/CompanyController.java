package az.matcha.company.controller;

import az.matcha.company.dto.*;
import az.matcha.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Create a new company. Caller must be an EMPLOYER — they become OWNER.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    public CompanyResponse createCompany(@Valid @RequestBody CreateCompanyRequest req,
                                         JwtAuthenticationToken auth) {
        return companyService.createCompany(req, auth);
    }

    /**
     * Get company details. Any authenticated user may view.
     */
    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(@PathVariable UUID companyId) {
        return companyService.getCompany(companyId);
    }

    /**
     * Update company. Caller must be a member (OWNER or HR).
     */
    @PutMapping("/{companyId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public CompanyResponse updateCompany(@PathVariable UUID companyId,
                                          @Valid @RequestBody UpdateCompanyRequest req,
                                          JwtAuthenticationToken auth) {
        return companyService.updateCompany(companyId, req, auth);
    }

    /**
     * List all members of a company.
     */
    @GetMapping("/{companyId}/members")
    public List<CompanyMemberResponse> listMembers(@PathVariable UUID companyId) {
        return companyService.listMembers(companyId);
    }

    /**
     * Create an HR invitation token (single-use, 72h TTL). Caller must be a member.
     */
    @PostMapping("/{companyId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPLOYER')")
    public InvitationResponse createInvitation(@PathVariable UUID companyId,
                                                @Valid @RequestBody CreateInvitationRequest req,
                                                JwtAuthenticationToken auth) {
        return companyService.createInvitation(companyId, req, auth);
    }
}

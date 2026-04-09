package az.matcha.company.controller;

import az.matcha.company.dto.AcceptInvitationRequest;
import az.matcha.company.dto.CompanyMemberResponse;
import az.matcha.company.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final CompanyService companyService;

    public InvitationController(CompanyService companyService) {
        this.companyService = companyService;
    }

    /**
     * Accept an HR invitation. Caller must be an EMPLOYER who is not yet a member.
     */
    @PostMapping("/accept")
    @PreAuthorize("hasRole('EMPLOYER')")
    public CompanyMemberResponse acceptInvitation(@Valid @RequestBody AcceptInvitationRequest req,
                                                   JwtAuthenticationToken auth) {
        return companyService.acceptInvitation(req.token(), auth);
    }
}

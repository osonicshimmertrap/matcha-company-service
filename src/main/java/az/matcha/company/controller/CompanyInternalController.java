package az.matcha.company.controller;

import az.matcha.company.dto.CompanyResponse;
import az.matcha.company.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal endpoints consumed by other services (job-service, application-service).
 * Requires authentication — no specific role check (service-to-service via forwarded JWT).
 */
@RestController
@RequestMapping("/api/v1/internal/companies")
public class CompanyInternalController {

    private final CompanyService companyService;

    public CompanyInternalController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{companyId}")
    public CompanyResponse getCompany(@PathVariable UUID companyId) {
        return companyService.getCompany(companyId);
    }
}

package az.matcha.company.dto;

import az.matcha.company.domain.Company;
import az.matcha.company.domain.CompanySizeRange;

import java.time.Instant;
import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String name,
        String description,
        String industry,
        CompanySizeRange sizeRange,
        String website,
        String logoUrl,
        String country,
        String city,
        Integer foundedYear,
        boolean headApprovesPostings,
        UUID createdByUserId,
        Instant createdAt,
        Instant updatedAt
) {
    public static CompanyResponse from(Company c) {
        return new CompanyResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getIndustry(),
                c.getSizeRange(),
                c.getWebsite(),
                c.getLogoUrl(),
                c.getCountry(),
                c.getCity(),
                c.getFoundedYear(),
                c.isHeadApprovesPostings(),
                c.getCreatedByUserId(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}

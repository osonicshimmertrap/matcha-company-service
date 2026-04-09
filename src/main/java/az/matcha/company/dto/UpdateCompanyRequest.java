package az.matcha.company.dto;

import az.matcha.company.domain.CompanySizeRange;
import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
        @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @Size(max = 100) String industry,
        CompanySizeRange sizeRange,
        @Size(max = 500) String website,
        @Size(max = 500) String logoUrl,
        @Size(max = 100) String country,
        @Size(max = 100) String city,
        Integer foundedYear,
        Boolean headApprovesPostings
) {}

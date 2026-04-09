package az.matcha.company.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(
        @Email @Size(max = 254) String inviteeEmail
) {}

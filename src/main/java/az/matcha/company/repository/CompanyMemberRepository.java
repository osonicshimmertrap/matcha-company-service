package az.matcha.company.repository;

import az.matcha.company.domain.CompanyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    Optional<CompanyMember> findByCompany_IdAndUserId(UUID companyId, UUID userId);

    List<CompanyMember> findAllByCompany_Id(UUID companyId);

    boolean existsByCompany_IdAndUserId(UUID companyId, UUID userId);
}

package kr.dagagomap.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.dagagomap.entity.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {

	Optional<Company> findByNameAndSourceAddress(String name, String sourceAddress);

	List<Company> findByCoordinatesUpdateRequired(Boolean coordinatesUpdateRequired, Limit limit);

}

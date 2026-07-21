package kr.dagagomap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.dagagomap.entity.Company;

import java.util.Collection;
import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long>, CompanyRepositoryCustom {

	List<Company> findAllByTaxIdNotIn(Collection<Long> taxIds);

	List<Company> findAllByCoordinatesUpdateRequired(Boolean coordinatesUpdateRequired);

}

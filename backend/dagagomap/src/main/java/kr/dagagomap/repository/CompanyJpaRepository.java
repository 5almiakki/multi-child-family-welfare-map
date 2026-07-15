package kr.dagagomap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.dagagomap.entity.Company;

import java.util.List;

public interface CompanyJpaRepository extends JpaRepository<Company, Long> {

	List<Company> findAllByTaxIdNotIn(List<Long> taxIds);

}

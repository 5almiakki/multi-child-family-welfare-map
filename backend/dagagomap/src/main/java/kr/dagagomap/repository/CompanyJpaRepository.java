package kr.dagagomap.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.dagagomap.entity.Company;

public interface CompanyJpaRepository extends JpaRepository<Company, Integer> {

}

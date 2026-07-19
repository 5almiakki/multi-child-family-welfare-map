package kr.dagagomap.repository;

import kr.dagagomap.entity.Company;

import java.util.List;
import java.util.Set;

public interface CompanyRepositoryCustom {

	List<Company> retrieveCompanies(String name, Set<String> gus, Set<String> categories);

}

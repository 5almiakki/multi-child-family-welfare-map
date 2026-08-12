package kr.dagagomap.repository;

import kr.dagagomap.entity.Company;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CompanyRepositoryCustom {

	List<Company> findCompanies(String name, Set<String> gus, Set<String> categories, double latitude, double longitude);

	List<Company> findAllMatchingNameAndAddress(Collection<Company.NaturalKey> naturalKeys);

	List<Company> findAllNotMatchingNameAndAddress(Collection<Company.NaturalKey> naturalKeys);

	List<String> autocompleteNames(String keyword, int limit);

}

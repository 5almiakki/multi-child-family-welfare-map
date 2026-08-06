package kr.dagagomap.repository;

import kr.dagagomap.entity.Company;

import java.util.List;
import java.util.Set;

public interface CompanyRepositoryCustom {

	List<Company> findCompanies(String name, Set<String> gus, Set<String> categories, double latitude, double longitude);

	List<Company> findAllMatchingNameAndAddress(Iterable<List<String>> nameAddressPairs);

	List<Company> findAllNotMatchingNameAndAddress(Iterable<List<String>> nameAddressPairs);

}

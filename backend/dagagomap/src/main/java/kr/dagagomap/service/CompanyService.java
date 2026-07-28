package kr.dagagomap.service;

import kr.dagagomap.controller.dto.response.CompanyRetrievalResponseDto;
import kr.dagagomap.entity.Company;
import kr.dagagomap.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class CompanyService {

	private final CompanyRepository companyRepository;

	@Transactional(readOnly = true)
	public List<CompanyRetrievalResponseDto> retrieveCompanies(
			String name, Set<String> gus, Set<String> categories, double latitude, double longitude) {
		List<Company> companies = companyRepository.retrieveCompanies(name, gus, categories, latitude, longitude);
		return companies.stream()
				.map(CompanyRetrievalResponseDto::new)
				.toList();
	}

}

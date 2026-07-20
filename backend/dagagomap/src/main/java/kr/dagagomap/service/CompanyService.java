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
		List<Company> companies = companyRepository.retrieveCompanies(name, gus, categories);
		return companies.stream()
				.sorted((c1, c2) -> {
					double dLatitude1 = c1.getLatitude() - latitude;
					double dLongitude1 = c1.getLongitude() - longitude;
					double distanceSquare1 = dLatitude1 * dLatitude1 + dLongitude1 * dLongitude1;
					double dLatitude2 = c2.getLatitude() - latitude;
					double dLongitude2 = c2.getLongitude() - longitude;
					double distanceSquare2 = dLatitude2 * dLatitude2 + dLongitude2 * dLongitude2;
					return Double.compare(distanceSquare1, distanceSquare2);
				})
				.map(CompanyRetrievalResponseDto::new)
				.toList();
	}

}

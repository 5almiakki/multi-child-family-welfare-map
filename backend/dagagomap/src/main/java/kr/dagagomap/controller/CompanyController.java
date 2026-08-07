package kr.dagagomap.controller;

import kr.dagagomap.controller.dto.response.CompanyRetrievalResponseDto;
import kr.dagagomap.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

	private final CompanyService companyService;

	@GetMapping
	public ResponseEntity<List<CompanyRetrievalResponseDto>> search(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) Set<String> gus,
			@RequestParam(required = false) Set<String> categories,
			@RequestParam double latitude,
			@RequestParam double longitude) {
		List<CompanyRetrievalResponseDto> companies =
				companyService.retrieveCompanies(name, gus, categories, latitude, longitude);
		return ResponseEntity.ok(companies);
	}

	@GetMapping("/autocomplete")
	public ResponseEntity<List<String>> autocomplete(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "10") int limit) {
		List<String> names = companyService.autocompleteNames(keyword, limit);
		return ResponseEntity.ok(names);
	}

}

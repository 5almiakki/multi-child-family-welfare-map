package kr.dagagomap.controller;

import kr.dagagomap.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/v1/dev/companies")
@Profile("dev")
public class CompanyDevController {

	private final CompanyService companyService;

	@PatchMapping("/trigger-company-sync")
	public ResponseEntity<Void> triggerCompanySync() {
		companyService.triggerCompanySync();
		return ResponseEntity.noContent().build();
	}

}

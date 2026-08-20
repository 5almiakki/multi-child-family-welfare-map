package kr.dagagomap.controller;

import kr.dagagomap.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	@PatchMapping("/trigger-save-pub-data-to-db")
	public ResponseEntity<Void> triggerSavePubDataToDb() {
		companyService.triggerSavePubDataToDb();
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/trigger-address-to-coords-conversion")
	public ResponseEntity<Void> triggerAddressToCoordsConversion(
			@RequestParam(defaultValue = "0") int count) {
		companyService.triggerAddressToCoordsConversion(count);
		return ResponseEntity.noContent().build();
	}

}

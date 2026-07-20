package kr.dagagomap.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.dagagomap.entity.Company;
import kr.dagagomap.entity.QCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Repository
public class CompanyRepositoryCustomImpl implements CompanyRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QCompany company = QCompany.company;

	@Override
	public List<Company> retrieveCompanies(String name, Set<String> gus, Set<String> categories) {
		return queryFactory.selectFrom(company)
				.where(where(name, gus, categories))
				.fetch();
	}

	private BooleanBuilder where(
			String name, Set<String> gus, Set<String> categories) {
		BooleanBuilder resultPredicate = new BooleanBuilder();
		if (name != null) {
			resultPredicate.and(company.name.contains(name));
		}
		if (gus != null && !gus.isEmpty()) {
			resultPredicate.and(company.gu.in(gus));
		}
		if (categories != null && !categories.isEmpty()) {
			resultPredicate.and(company.category.in(categories));
		}
		return resultPredicate;
	}

}

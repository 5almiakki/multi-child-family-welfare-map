package kr.dagagomap.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
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
	public List<Company> retrieveCompanies(
			String name, Set<String> gus, Set<String> categories, double latitude, double longitude) {
		return queryFactory.selectFrom(company)
				.where(contains(name, gus, categories))
				.orderBy(distance(latitude, longitude).asc().nullsLast())
				.fetch();
	}

	private BooleanBuilder contains(
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

	private NumberTemplate<Double> distance(double latitude, double longitude) {
		String template = "6371 * acos("
				+ "cos(radians({0})) * cos(radians({1})) * "
				+ "cos(radians({2}) - radians({3})) + "
				+ "sin(radians({0})) * sin(radians({1})))";
		return Expressions.numberTemplate(
				Double.class, template, latitude, company.latitude, longitude, company.longitude);
	}

}

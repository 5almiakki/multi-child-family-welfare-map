package kr.dagagomap.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.dagagomap.entity.Company;
import kr.dagagomap.entity.QCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class CompanyRepositoryCustomImpl implements CompanyRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QCompany company = QCompany.company;

	@Override
	public List<Company> findCompanies(
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

	@Override
	public List<Company> findAllMatchingNameAndAddress(Collection<Company.NaturalKey> naturalKeys) {
		if (naturalKeys == null || !naturalKeys.isEmpty()) {
			return Collections.emptyList();
		}
		return queryFactory.selectFrom(company)
				.where(matches(naturalKeys))
				.fetch();
	}

	@Override
	public List<Company> findAllNotMatchingNameAndAddress(Collection<Company.NaturalKey> naturalKeys) {
		Set<Company.NaturalKey> naturalKeySet = new HashSet<>(naturalKeys);
		List<Company> allCompanies = queryFactory.selectFrom(company).fetch();
		return allCompanies.stream()
				.filter(c -> !naturalKeySet.contains(c.naturalKey()))
				.toList();
	}

	@Override
	public List<String> autocompleteNames(String keyword, int limit) {
		if (keyword == null || keyword.isBlank()) {
			return Collections.emptyList();
		}
		return queryFactory.selectDistinct(company.name)
				.from(company)
				.where(company.name.contains(keyword))
				.orderBy(company.name.asc())
				.limit(limit)
				.fetch();
	}

	private BooleanBuilder matches(Collection<Company.NaturalKey> naturalKeys) {
		BooleanBuilder predicate = new BooleanBuilder();
		for (var naturalKey : naturalKeys) {
			predicate.or(company.name.eq(naturalKey.name())
					.and(company.sourceAddress.eq(naturalKey.sourceAddress())));
		}
		return predicate;
	}

}

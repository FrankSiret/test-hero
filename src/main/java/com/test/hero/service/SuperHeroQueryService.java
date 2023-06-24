package com.test.hero.service;

import com.test.hero.domain.*; // for static metamodels
import com.test.hero.domain.SuperHero;
import com.test.hero.repository.SuperHeroRepository;
import com.test.hero.service.criteria.SuperHeroCriteria;
import com.test.hero.service.dto.SuperHeroDTO;
import com.test.hero.service.mapper.SuperHeroMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link SuperHero} entities in the database.
 * The main input is a {@link SuperHeroCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SuperHeroDTO} or a {@link Page} of {@link SuperHeroDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class SuperHeroQueryService extends QueryService<SuperHero> {

    private final Logger log = LoggerFactory.getLogger(SuperHeroQueryService.class);

    private final SuperHeroRepository superHeroRepository;

    private final SuperHeroMapper superHeroMapper;

    public SuperHeroQueryService(SuperHeroRepository superHeroRepository, SuperHeroMapper superHeroMapper) {
        this.superHeroRepository = superHeroRepository;
        this.superHeroMapper = superHeroMapper;
    }

    /**
     * Return a {@link List} of {@link SuperHeroDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<SuperHeroDTO> findByCriteria(SuperHeroCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<SuperHero> specification = createSpecification(criteria);
        return superHeroMapper.toDto(superHeroRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link SuperHeroDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SuperHeroDTO> findByCriteria(SuperHeroCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<SuperHero> specification = createSpecification(criteria);
        return superHeroRepository.findAll(specification, page).map(superHeroMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SuperHeroCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<SuperHero> specification = createSpecification(criteria);
        return superHeroRepository.count(specification);
    }

    /**
     * Function to convert {@link SuperHeroCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SuperHero> createSpecification(SuperHeroCriteria criteria) {
        Specification<SuperHero> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SuperHero_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), SuperHero_.name));
            }
            if (criteria.getAge() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAge(), SuperHero_.age));
            }
            if (criteria.getSuperpower() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSuperpower(), SuperHero_.superpower));
            }
        }
        return specification;
    }
}

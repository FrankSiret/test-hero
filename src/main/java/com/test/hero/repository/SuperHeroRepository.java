package com.test.hero.repository;

import com.test.hero.domain.SuperHero;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the SuperHero entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SuperHeroRepository extends JpaRepository<SuperHero, Long>, JpaSpecificationExecutor<SuperHero> {}

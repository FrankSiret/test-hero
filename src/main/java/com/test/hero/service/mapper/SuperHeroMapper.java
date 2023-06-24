package com.test.hero.service.mapper;

import com.test.hero.domain.SuperHero;
import com.test.hero.service.dto.SuperHeroDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link SuperHero} and its DTO {@link SuperHeroDTO}.
 */
@Mapper(componentModel = "spring")
public interface SuperHeroMapper extends EntityMapper<SuperHeroDTO, SuperHero> {}

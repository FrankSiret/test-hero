package com.test.hero.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SuperHeroMapperTest {

    private SuperHeroMapper superHeroMapper;

    @BeforeEach
    public void setUp() {
        superHeroMapper = new SuperHeroMapperImpl();
    }
}

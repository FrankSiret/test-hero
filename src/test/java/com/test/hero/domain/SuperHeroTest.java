package com.test.hero.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.hero.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SuperHeroTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(SuperHero.class);
        SuperHero superHero1 = new SuperHero();
        superHero1.setId(1L);
        SuperHero superHero2 = new SuperHero();
        superHero2.setId(superHero1.getId());
        assertThat(superHero1).isEqualTo(superHero2);
        superHero2.setId(2L);
        assertThat(superHero1).isNotEqualTo(superHero2);
        superHero1.setId(null);
        assertThat(superHero1).isNotEqualTo(superHero2);
    }
}

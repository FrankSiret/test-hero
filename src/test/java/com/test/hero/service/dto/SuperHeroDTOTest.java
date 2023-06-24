package com.test.hero.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.hero.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class SuperHeroDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(SuperHeroDTO.class);
        SuperHeroDTO superHeroDTO1 = new SuperHeroDTO();
        superHeroDTO1.setId(1L);
        SuperHeroDTO superHeroDTO2 = new SuperHeroDTO();
        assertThat(superHeroDTO1).isNotEqualTo(superHeroDTO2);
        superHeroDTO2.setId(superHeroDTO1.getId());
        assertThat(superHeroDTO1).isEqualTo(superHeroDTO2);
        superHeroDTO2.setId(2L);
        assertThat(superHeroDTO1).isNotEqualTo(superHeroDTO2);
        superHeroDTO1.setId(null);
        assertThat(superHeroDTO1).isNotEqualTo(superHeroDTO2);
    }
}

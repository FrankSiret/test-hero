package com.test.hero.service.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link com.test.hero.domain.SuperHero} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SuperHeroDTO implements Serializable {

    private Long id;

    private String name;

    private Integer age;

    private String superpower;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSuperpower() {
        return superpower;
    }

    public void setSuperpower(String superpower) {
        this.superpower = superpower;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SuperHeroDTO)) {
            return false;
        }

        SuperHeroDTO superHeroDTO = (SuperHeroDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, superHeroDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SuperHeroDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", age=" + getAge() +
            ", superpower='" + getSuperpower() + "'" +
            "}";
    }
}

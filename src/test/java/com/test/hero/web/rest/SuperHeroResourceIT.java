package com.test.hero.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.test.hero.IntegrationTest;
import com.test.hero.domain.SuperHero;
import com.test.hero.repository.SuperHeroRepository;
import com.test.hero.service.criteria.SuperHeroCriteria;
import com.test.hero.service.dto.SuperHeroDTO;
import com.test.hero.service.mapper.SuperHeroMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link SuperHeroResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class SuperHeroResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_AGE = 1;
    private static final Integer UPDATED_AGE = 2;
    private static final Integer SMALLER_AGE = 1 - 1;

    private static final String DEFAULT_SUPERPOWER = "AAAAAAAAAA";
    private static final String UPDATED_SUPERPOWER = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/super-heroes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private SuperHeroRepository superHeroRepository;

    @Autowired
    private SuperHeroMapper superHeroMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restSuperHeroMockMvc;

    private SuperHero superHero;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SuperHero createEntity(EntityManager em) {
        SuperHero superHero = new SuperHero().name(DEFAULT_NAME).age(DEFAULT_AGE).superpower(DEFAULT_SUPERPOWER);
        return superHero;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SuperHero createUpdatedEntity(EntityManager em) {
        SuperHero superHero = new SuperHero().name(UPDATED_NAME).age(UPDATED_AGE).superpower(UPDATED_SUPERPOWER);
        return superHero;
    }

    @BeforeEach
    public void initTest() {
        superHero = createEntity(em);
    }

    @Test
    @Transactional
    void createSuperHero() throws Exception {
        int databaseSizeBeforeCreate = superHeroRepository.findAll().size();
        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);
        restSuperHeroMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(superHeroDTO)))
            .andExpect(status().isCreated());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeCreate + 1);
        SuperHero testSuperHero = superHeroList.get(superHeroList.size() - 1);
        assertThat(testSuperHero.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testSuperHero.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testSuperHero.getSuperpower()).isEqualTo(DEFAULT_SUPERPOWER);
    }

    @Test
    @Transactional
    void createSuperHeroWithExistingId() throws Exception {
        // Create the SuperHero with an existing ID
        superHero.setId(1L);
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        int databaseSizeBeforeCreate = superHeroRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restSuperHeroMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(superHeroDTO)))
            .andExpect(status().isBadRequest());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllSuperHeroes() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(superHero.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].superpower").value(hasItem(DEFAULT_SUPERPOWER)));
    }

    @Test
    @Transactional
    void getSuperHero() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get the superHero
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL_ID, superHero.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(superHero.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.age").value(DEFAULT_AGE))
            .andExpect(jsonPath("$.superpower").value(DEFAULT_SUPERPOWER));
    }

    @Test
    @Transactional
    void getSuperHeroesByIdFiltering() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        Long id = superHero.getId();

        defaultSuperHeroShouldBeFound("id.equals=" + id);
        defaultSuperHeroShouldNotBeFound("id.notEquals=" + id);

        defaultSuperHeroShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultSuperHeroShouldNotBeFound("id.greaterThan=" + id);

        defaultSuperHeroShouldBeFound("id.lessThanOrEqual=" + id);
        defaultSuperHeroShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where name equals to DEFAULT_NAME
        defaultSuperHeroShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the superHeroList where name equals to UPDATED_NAME
        defaultSuperHeroShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByNameIsInShouldWork() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where name in DEFAULT_NAME or UPDATED_NAME
        defaultSuperHeroShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the superHeroList where name equals to UPDATED_NAME
        defaultSuperHeroShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where name is not null
        defaultSuperHeroShouldBeFound("name.specified=true");

        // Get all the superHeroList where name is null
        defaultSuperHeroShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllSuperHeroesByNameContainsSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where name contains DEFAULT_NAME
        defaultSuperHeroShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the superHeroList where name contains UPDATED_NAME
        defaultSuperHeroShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByNameNotContainsSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where name does not contain DEFAULT_NAME
        defaultSuperHeroShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the superHeroList where name does not contain UPDATED_NAME
        defaultSuperHeroShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsEqualToSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age equals to DEFAULT_AGE
        defaultSuperHeroShouldBeFound("age.equals=" + DEFAULT_AGE);

        // Get all the superHeroList where age equals to UPDATED_AGE
        defaultSuperHeroShouldNotBeFound("age.equals=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsInShouldWork() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age in DEFAULT_AGE or UPDATED_AGE
        defaultSuperHeroShouldBeFound("age.in=" + DEFAULT_AGE + "," + UPDATED_AGE);

        // Get all the superHeroList where age equals to UPDATED_AGE
        defaultSuperHeroShouldNotBeFound("age.in=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsNullOrNotNull() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age is not null
        defaultSuperHeroShouldBeFound("age.specified=true");

        // Get all the superHeroList where age is null
        defaultSuperHeroShouldNotBeFound("age.specified=false");
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age is greater than or equal to DEFAULT_AGE
        defaultSuperHeroShouldBeFound("age.greaterThanOrEqual=" + DEFAULT_AGE);

        // Get all the superHeroList where age is greater than or equal to UPDATED_AGE
        defaultSuperHeroShouldNotBeFound("age.greaterThanOrEqual=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age is less than or equal to DEFAULT_AGE
        defaultSuperHeroShouldBeFound("age.lessThanOrEqual=" + DEFAULT_AGE);

        // Get all the superHeroList where age is less than or equal to SMALLER_AGE
        defaultSuperHeroShouldNotBeFound("age.lessThanOrEqual=" + SMALLER_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsLessThanSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age is less than DEFAULT_AGE
        defaultSuperHeroShouldNotBeFound("age.lessThan=" + DEFAULT_AGE);

        // Get all the superHeroList where age is less than UPDATED_AGE
        defaultSuperHeroShouldBeFound("age.lessThan=" + UPDATED_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesByAgeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where age is greater than DEFAULT_AGE
        defaultSuperHeroShouldNotBeFound("age.greaterThan=" + DEFAULT_AGE);

        // Get all the superHeroList where age is greater than SMALLER_AGE
        defaultSuperHeroShouldBeFound("age.greaterThan=" + SMALLER_AGE);
    }

    @Test
    @Transactional
    void getAllSuperHeroesBySuperpowerIsEqualToSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where superpower equals to DEFAULT_SUPERPOWER
        defaultSuperHeroShouldBeFound("superpower.equals=" + DEFAULT_SUPERPOWER);

        // Get all the superHeroList where superpower equals to UPDATED_SUPERPOWER
        defaultSuperHeroShouldNotBeFound("superpower.equals=" + UPDATED_SUPERPOWER);
    }

    @Test
    @Transactional
    void getAllSuperHeroesBySuperpowerIsInShouldWork() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where superpower in DEFAULT_SUPERPOWER or UPDATED_SUPERPOWER
        defaultSuperHeroShouldBeFound("superpower.in=" + DEFAULT_SUPERPOWER + "," + UPDATED_SUPERPOWER);

        // Get all the superHeroList where superpower equals to UPDATED_SUPERPOWER
        defaultSuperHeroShouldNotBeFound("superpower.in=" + UPDATED_SUPERPOWER);
    }

    @Test
    @Transactional
    void getAllSuperHeroesBySuperpowerIsNullOrNotNull() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where superpower is not null
        defaultSuperHeroShouldBeFound("superpower.specified=true");

        // Get all the superHeroList where superpower is null
        defaultSuperHeroShouldNotBeFound("superpower.specified=false");
    }

    @Test
    @Transactional
    void getAllSuperHeroesBySuperpowerContainsSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where superpower contains DEFAULT_SUPERPOWER
        defaultSuperHeroShouldBeFound("superpower.contains=" + DEFAULT_SUPERPOWER);

        // Get all the superHeroList where superpower contains UPDATED_SUPERPOWER
        defaultSuperHeroShouldNotBeFound("superpower.contains=" + UPDATED_SUPERPOWER);
    }

    @Test
    @Transactional
    void getAllSuperHeroesBySuperpowerNotContainsSomething() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        // Get all the superHeroList where superpower does not contain DEFAULT_SUPERPOWER
        defaultSuperHeroShouldNotBeFound("superpower.doesNotContain=" + DEFAULT_SUPERPOWER);

        // Get all the superHeroList where superpower does not contain UPDATED_SUPERPOWER
        defaultSuperHeroShouldBeFound("superpower.doesNotContain=" + UPDATED_SUPERPOWER);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSuperHeroShouldBeFound(String filter) throws Exception {
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(superHero.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].age").value(hasItem(DEFAULT_AGE)))
            .andExpect(jsonPath("$.[*].superpower").value(hasItem(DEFAULT_SUPERPOWER)));

        // Check, that the count call also returns 1
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSuperHeroShouldNotBeFound(String filter) throws Exception {
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restSuperHeroMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingSuperHero() throws Exception {
        // Get the superHero
        restSuperHeroMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingSuperHero() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();

        // Update the superHero
        SuperHero updatedSuperHero = superHeroRepository.findById(superHero.getId()).get();
        // Disconnect from session so that the updates on updatedSuperHero are not directly saved in db
        em.detach(updatedSuperHero);
        updatedSuperHero.name(UPDATED_NAME).age(UPDATED_AGE).superpower(UPDATED_SUPERPOWER);
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(updatedSuperHero);

        restSuperHeroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, superHeroDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isOk());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
        SuperHero testSuperHero = superHeroList.get(superHeroList.size() - 1);
        assertThat(testSuperHero.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSuperHero.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testSuperHero.getSuperpower()).isEqualTo(UPDATED_SUPERPOWER);
    }

    @Test
    @Transactional
    void putNonExistingSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, superHeroDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(superHeroDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateSuperHeroWithPatch() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();

        // Update the superHero using partial update
        SuperHero partialUpdatedSuperHero = new SuperHero();
        partialUpdatedSuperHero.setId(superHero.getId());

        partialUpdatedSuperHero.name(UPDATED_NAME);

        restSuperHeroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSuperHero.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSuperHero))
            )
            .andExpect(status().isOk());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
        SuperHero testSuperHero = superHeroList.get(superHeroList.size() - 1);
        assertThat(testSuperHero.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSuperHero.getAge()).isEqualTo(DEFAULT_AGE);
        assertThat(testSuperHero.getSuperpower()).isEqualTo(DEFAULT_SUPERPOWER);
    }

    @Test
    @Transactional
    void fullUpdateSuperHeroWithPatch() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();

        // Update the superHero using partial update
        SuperHero partialUpdatedSuperHero = new SuperHero();
        partialUpdatedSuperHero.setId(superHero.getId());

        partialUpdatedSuperHero.name(UPDATED_NAME).age(UPDATED_AGE).superpower(UPDATED_SUPERPOWER);

        restSuperHeroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedSuperHero.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedSuperHero))
            )
            .andExpect(status().isOk());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
        SuperHero testSuperHero = superHeroList.get(superHeroList.size() - 1);
        assertThat(testSuperHero.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testSuperHero.getAge()).isEqualTo(UPDATED_AGE);
        assertThat(testSuperHero.getSuperpower()).isEqualTo(UPDATED_SUPERPOWER);
    }

    @Test
    @Transactional
    void patchNonExistingSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, superHeroDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamSuperHero() throws Exception {
        int databaseSizeBeforeUpdate = superHeroRepository.findAll().size();
        superHero.setId(count.incrementAndGet());

        // Create the SuperHero
        SuperHeroDTO superHeroDTO = superHeroMapper.toDto(superHero);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restSuperHeroMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(superHeroDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the SuperHero in the database
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteSuperHero() throws Exception {
        // Initialize the database
        superHeroRepository.saveAndFlush(superHero);

        int databaseSizeBeforeDelete = superHeroRepository.findAll().size();

        // Delete the superHero
        restSuperHeroMockMvc
            .perform(delete(ENTITY_API_URL_ID, superHero.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SuperHero> superHeroList = superHeroRepository.findAll();
        assertThat(superHeroList).hasSize(databaseSizeBeforeDelete - 1);
    }
}

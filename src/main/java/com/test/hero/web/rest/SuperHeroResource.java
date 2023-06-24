package com.test.hero.web.rest;

import com.test.hero.repository.SuperHeroRepository;
import com.test.hero.service.SuperHeroQueryService;
import com.test.hero.service.SuperHeroService;
import com.test.hero.service.criteria.SuperHeroCriteria;
import com.test.hero.service.dto.SuperHeroDTO;
import com.test.hero.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.test.hero.domain.SuperHero}.
 */
@RestController
@RequestMapping("/api")
public class SuperHeroResource {

    private final Logger log = LoggerFactory.getLogger(SuperHeroResource.class);

    private static final String ENTITY_NAME = "superHero";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SuperHeroService superHeroService;

    private final SuperHeroRepository superHeroRepository;

    private final SuperHeroQueryService superHeroQueryService;

    public SuperHeroResource(
        SuperHeroService superHeroService,
        SuperHeroRepository superHeroRepository,
        SuperHeroQueryService superHeroQueryService
    ) {
        this.superHeroService = superHeroService;
        this.superHeroRepository = superHeroRepository;
        this.superHeroQueryService = superHeroQueryService;
    }

    /**
     * {@code POST  /super-heroes} : Create a new superHero.
     *
     * @param superHeroDTO the superHeroDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new superHeroDTO, or with status {@code 400 (Bad Request)} if the superHero has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/super-heroes")
    public ResponseEntity<SuperHeroDTO> createSuperHero(@RequestBody SuperHeroDTO superHeroDTO) throws URISyntaxException {
        log.debug("REST request to save SuperHero : {}", superHeroDTO);
        if (superHeroDTO.getId() != null) {
            throw new BadRequestAlertException("A new superHero cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SuperHeroDTO result = superHeroService.save(superHeroDTO);
        return ResponseEntity
            .created(new URI("/api/super-heroes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /super-heroes/:id} : Updates an existing superHero.
     *
     * @param id the id of the superHeroDTO to save.
     * @param superHeroDTO the superHeroDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated superHeroDTO,
     * or with status {@code 400 (Bad Request)} if the superHeroDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the superHeroDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/super-heroes/{id}")
    public ResponseEntity<SuperHeroDTO> updateSuperHero(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SuperHeroDTO superHeroDTO
    ) throws URISyntaxException {
        log.debug("REST request to update SuperHero : {}, {}", id, superHeroDTO);
        if (superHeroDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, superHeroDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!superHeroRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SuperHeroDTO result = superHeroService.update(superHeroDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, superHeroDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /super-heroes/:id} : Partial updates given fields of an existing superHero, field will ignore if it is null
     *
     * @param id the id of the superHeroDTO to save.
     * @param superHeroDTO the superHeroDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated superHeroDTO,
     * or with status {@code 400 (Bad Request)} if the superHeroDTO is not valid,
     * or with status {@code 404 (Not Found)} if the superHeroDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the superHeroDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/super-heroes/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SuperHeroDTO> partialUpdateSuperHero(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SuperHeroDTO superHeroDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update SuperHero partially : {}, {}", id, superHeroDTO);
        if (superHeroDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, superHeroDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!superHeroRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SuperHeroDTO> result = superHeroService.partialUpdate(superHeroDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, superHeroDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /super-heroes} : get all the superHeroes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of superHeroes in body.
     */
    @GetMapping("/super-heroes")
    public ResponseEntity<List<SuperHeroDTO>> getAllSuperHeroes(
        SuperHeroCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get SuperHeroes by criteria: {}", criteria);
        Page<SuperHeroDTO> page = superHeroQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /super-heroes/count} : count all the superHeroes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/super-heroes/count")
    public ResponseEntity<Long> countSuperHeroes(SuperHeroCriteria criteria) {
        log.debug("REST request to count SuperHeroes by criteria: {}", criteria);
        return ResponseEntity.ok().body(superHeroQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /super-heroes/:id} : get the "id" superHero.
     *
     * @param id the id of the superHeroDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the superHeroDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/super-heroes/{id}")
    public ResponseEntity<SuperHeroDTO> getSuperHero(@PathVariable Long id) {
        log.debug("REST request to get SuperHero : {}", id);
        Optional<SuperHeroDTO> superHeroDTO = superHeroService.findOne(id);
        return ResponseUtil.wrapOrNotFound(superHeroDTO);
    }

    /**
     * {@code DELETE  /super-heroes/:id} : delete the "id" superHero.
     *
     * @param id the id of the superHeroDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/super-heroes/{id}")
    public ResponseEntity<Void> deleteSuperHero(@PathVariable Long id) {
        log.debug("REST request to delete SuperHero : {}", id);
        superHeroService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}

package com.test.hero.service;

import com.test.hero.domain.SuperHero;
import com.test.hero.repository.SuperHeroRepository;
import com.test.hero.service.dto.SuperHeroDTO;
import com.test.hero.service.mapper.SuperHeroMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link SuperHero}.
 */
@Service
@Transactional
public class SuperHeroService {

    private final Logger log = LoggerFactory.getLogger(SuperHeroService.class);

    private final SuperHeroRepository superHeroRepository;

    private final SuperHeroMapper superHeroMapper;

    public SuperHeroService(SuperHeroRepository superHeroRepository, SuperHeroMapper superHeroMapper) {
        this.superHeroRepository = superHeroRepository;
        this.superHeroMapper = superHeroMapper;
    }

    /**
     * Save a superHero.
     *
     * @param superHeroDTO the entity to save.
     * @return the persisted entity.
     */
    public SuperHeroDTO save(SuperHeroDTO superHeroDTO) {
        log.debug("Request to save SuperHero : {}", superHeroDTO);
        SuperHero superHero = superHeroMapper.toEntity(superHeroDTO);
        superHero = superHeroRepository.save(superHero);
        return superHeroMapper.toDto(superHero);
    }

    /**
     * Update a superHero.
     *
     * @param superHeroDTO the entity to save.
     * @return the persisted entity.
     */
    public SuperHeroDTO update(SuperHeroDTO superHeroDTO) {
        log.debug("Request to update SuperHero : {}", superHeroDTO);
        SuperHero superHero = superHeroMapper.toEntity(superHeroDTO);
        superHero = superHeroRepository.save(superHero);
        Objects.requireNonNull(cacheManager.getCache("superHero")).evict(superHero.id);
        return superHeroMapper.toDto(superHero);
    }

    /**
     * Partially update a superHero.
     *
     * @param superHeroDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SuperHeroDTO> partialUpdate(SuperHeroDTO superHeroDTO) {
        log.debug("Request to partially update SuperHero : {}", superHeroDTO);

        return superHeroRepository
            .findById(superHeroDTO.getId())
            .map(existingSuperHero -> {
                superHeroMapper.partialUpdate(existingSuperHero, superHeroDTO);

                return existingSuperHero;
            })
            .map(superHero -> {
                superHero = superHeroRepository.save(superHero)
                Objects.requireNonNull(cacheManager.getCache("superHero")).evict(superHero.id);
                return superHero;
            })
            .map(superHeroMapper::toDto);
    }

    /**
     * Get all the superHeroes.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<SuperHeroDTO> findAll(Pageable pageable) {
        log.debug("Request to get all SuperHeroes");
        return superHeroRepository.findAll(pageable).map(superHeroMapper::toDto);
    }

    /**
     * Get one superHero by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Cacheable("superHero")
    @Transactional(readOnly = true)
    public Optional<SuperHeroDTO> findOne(Long id) {
        log.debug("Request to get SuperHero : {}", id);
        return superHeroRepository.findById(id).map(superHeroMapper::toDto);
    }

    /**
     * Delete the superHero by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete SuperHero : {}", id);
        superHeroRepository.deleteById(id);
    }
}

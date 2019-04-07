package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Stadium;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

// You must put Model and Model Identity for Repository
public interface StadiumRepository extends CrudRepository<Stadium, Long> {
    Optional<Stadium> findByKey(String key);
}

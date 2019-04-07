package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Stadium;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StadiumRepository extends CrudRepository {
    Optional<Stadium> findByKey(String key);

}

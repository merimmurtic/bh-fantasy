package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.Round;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoundRepository extends CrudRepository<Round, Long> {
    List<Round> findByNameContaining(String name);
}

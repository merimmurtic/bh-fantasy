package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.Card;
import org.springframework.data.repository.CrudRepository;

public interface CardRepository extends CrudRepository<Card, Long> {
}

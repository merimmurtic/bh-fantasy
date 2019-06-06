package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
    User findByPrincipalId(String principalId);

    @EntityGraph(value = "User.detail", type = EntityGraph.EntityGraphType.LOAD)
    User getByPrincipalId(String principalId);
}

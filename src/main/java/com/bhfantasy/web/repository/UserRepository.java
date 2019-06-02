package com.bhfantasy.web.repository;

import com.bhfantasy.web.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
    User findByPrincipalId(String principalId);
}

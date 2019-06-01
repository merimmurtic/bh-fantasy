package com.fifa.wolrdcup.repository;

import com.fifa.wolrdcup.model.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
    User findByPrincipalId(String principalId);
}

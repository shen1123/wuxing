package com.carl.tu.service;


import com.carl.tu.entity.User;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Transactional
public interface UserService {

    User selectById(Integer id);
}

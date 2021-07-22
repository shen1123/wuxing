package com.carl.tu.dao;

import com.carl.tu.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao {

    User selectById(Integer id);
}

package com.carl.tu.service.impl;


import com.carl.tu.entity.User;
import com.carl.tu.dao.UserDao;
import com.carl.tu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User selectById(Integer id) {
        return userDao.selectById(id);
    }
}

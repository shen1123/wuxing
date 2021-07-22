package com.carl.mu.sup;

import java.io.Serializable;

/**
 * @author WIN10
 * @date 2021-07-14 16:16
 */
public class ApiSup implements Serializable {

    private Integer id;
    private String msg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

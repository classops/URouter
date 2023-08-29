package io.github.classops.urouter.demo.bean;

import java.io.Serializable;

/**
 * SA
 *
 * @author wangmingshuo
 * @since 2023/04/26 17:31
 */
public class SA implements Serializable {

    private String name;

    public SA() {
    }

    public SA(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

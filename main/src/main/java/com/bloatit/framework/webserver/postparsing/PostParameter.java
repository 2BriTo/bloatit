package com.bloatit.framework.webserver.postparsing;

/**
 * <p>Simple class to describe a post parameter</p>
 * <p>Post parameters are described as name -> value</p>
 */
public class PostParameter {
    private String name;
    private String value;

    public PostParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

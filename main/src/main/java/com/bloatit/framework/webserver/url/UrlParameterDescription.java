package com.bloatit.framework.webserver.url;

import com.bloatit.framework.webserver.annotations.Message;
import com.bloatit.framework.webserver.annotations.Message.Level;
import com.bloatit.framework.webserver.annotations.RequestParam.Role;

public final class UrlParameterDescription<T> {

    private final String name;
    private final Class<T> valueClass;
    private final Role role;
    private final String defaultValue;
    private final String conversionErrorMsg;
    private final Message.Level level;

    public UrlParameterDescription(final String name,
                                   final Class<T> valueClass,
                                   final Role role,
                                   final String defaultValue,
                                   final String conversionErrorMsg,
                                   final Level level) {
        super();
        this.name = name;
        this.valueClass = valueClass;
        this.role = role;
        this.defaultValue = defaultValue;
        this.conversionErrorMsg = conversionErrorMsg;
        this.level = level;
    }

    public final String getName() {
        return name;
    }

    public final Class<T> getValueClass() {
        return valueClass;
    }

    public final Role getRole() {
        return role;
    }

    public final String getDefaultValue() {
        return defaultValue;
    }

    public final String getConversionErrorMsg() {
        return conversionErrorMsg;
    }

    public final Message.Level getLevel() {
        return level;
    }

}
package com.su.mall.security.component;

import java.util.Objects;

public record CustomConfigAttribute(String attribute) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CustomConfigAttribute other = (CustomConfigAttribute) obj;
        return Objects.equals(attribute, other.attribute);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return attribute;
    }
}
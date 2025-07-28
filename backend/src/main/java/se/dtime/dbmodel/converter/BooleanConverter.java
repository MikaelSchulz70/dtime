package se.dtime.dbmodel.converter;

import jakarta.persistence.AttributeConverter;

public class BooleanConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        return attribute == null ? null : (attribute ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : (dbData != 0);
    }
}
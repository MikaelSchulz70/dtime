package se.dtime.dbmodel.converter;

import jakarta.persistence.AttributeConverter;

public class BooleanConverter implements AttributeConverter<Boolean, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Boolean attribute) {
        return attribute == null ? 0 : (attribute ? 1 : 0);
    }

    @Override
    public Boolean convertToEntityAttribute(Integer dbData) {
        return dbData == null ? false : (dbData != 0);
    }
}
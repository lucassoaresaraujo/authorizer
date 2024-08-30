package com.issuingbank.authorizer.infra.converters;

import com.issuingbank.authorizer.domain.transaction.TransactionType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeConverter implements AttributeConverter<TransactionType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TransactionType attribute) {
        return attribute != null ? attribute.getId() : null;
    }

    @Override
    public TransactionType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? TransactionType.getById(dbData) : null;
    }
}

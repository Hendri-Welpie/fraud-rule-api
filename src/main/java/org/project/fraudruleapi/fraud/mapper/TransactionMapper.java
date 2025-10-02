package org.project.fraudruleapi.fraud.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.project.fraudruleapi.fraud.entity.TransactionEntity;
import org.project.fraudruleapi.fraud.model.TransactionDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper
public interface TransactionMapper {
    TransactionMapper INSTANCE = Mappers.getMapper(TransactionMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "timeStamp", target = "timeStamp", qualifiedByName = "convertToInstant")
    TransactionEntity mapToEntity(TransactionDto transaction);

    @Named("convertToInstant")
    default Instant convertToInstant(final LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.UTC);
    }
}
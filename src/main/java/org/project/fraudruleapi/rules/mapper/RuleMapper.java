package org.project.fraudruleapi.rules.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.project.fraudruleapi.rules.entity.RuleEntity;
import org.project.fraudruleapi.rules.model.RuleDto;

@Mapper
public interface RuleMapper {
    RuleMapper INSTANCE = Mappers.getMapper(RuleMapper.class);

    RuleDto mapToRuleDto(RuleEntity ruleEntity);
}


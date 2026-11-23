package com.example.authservice.transformer;

import com.example.authservice.dto.ProfileDto;
import com.example.authservice.entity.Credential;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

/**
 * @author Timur Khabibullin
 */
@Mapper(componentModel = SPRING)
public interface CredentialTransformer {

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    ProfileDto toProfile(Credential entity);
}

package com.teamflow.mapper;

import com.teamflow.domain.User;
import com.teamflow.dto.user.UserDto;
import org.mapstruct.Mapper;

import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    Set<UserDto> toDtoSet(Set<User> users);
}

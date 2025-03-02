package com.example.sms.mapper;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.entity.Employee;
import com.example.sms.dto.response.EmployeeResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeResponse toEmployeeResponse(Employee employee);

    List<EmployeeResponse> toEmployeeResponseList(List<Employee> employees);

    @Mapping(target = "id", ignore = true)
    Employee toEmployee(EmployeeRequest employeeRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    Employee partialUpdate(EmployeeRequest employeeRequest, @MappingTarget Employee employee);
}

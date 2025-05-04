package com.example.sms.service.implementation;

import com.example.sms.dto.request.EmployeeRequest;
import com.example.sms.dto.response.EmployeeResponse;
import com.example.sms.entity.Assignment;
import com.example.sms.entity.Employee;
import com.example.sms.exception.ConflictException;
import com.example.sms.mapper.EmployeeMapper;
import com.example.sms.repository.AssignmentRepository;
import com.example.sms.repository.EmployeeRepository;
import com.example.sms.service.EmployeeService;
import com.example.sms.utils.cache.Cache;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final Cache<Long, EmployeeResponse> cache;
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final AssignmentRepository assignmentRepository;

    public EmployeeServiceImpl(Cache<Long, EmployeeResponse> cache,
                               EmployeeRepository employeesRepository,
                               EmployeeMapper employeeMapper,
                               AssignmentRepository assignmentRepository) {

        this.cache = cache;
        this.employeeRepository = employeesRepository;
        this.employeeMapper = employeeMapper;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public List<EmployeeResponse> getAll() {
        return employeeMapper.toEmployeeResponseList(employeeRepository.findAll());
    }

    @Override
    public EmployeeResponse getById(Long id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + id));

        cache.put(id, employeeMapper.toEmployeeResponse(employee));
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse create(EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException("Phone number " + employeeRequest.phoneNumber()
                    + " is already in use");
        } else if (employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException("Email " + employeeRequest.email() + " is already in use");
        }
        return employeeMapper.toEmployeeResponse(
                employeeRepository.save(employeeMapper.toEmployee(employeeRequest)));
    }

    @Override
    public EmployeeResponse update(Long id, EmployeeRequest employeeRequest) {
        // Получаем текущего сотрудника
        Employee targetEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id = " + id));

        // Проверяем уникальность phoneNumber, исключая текущего сотрудника
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber()) &&
                !targetEmployee.getPhoneNumber().equals(employeeRequest.phoneNumber())) {
            throw new ConflictException("Phone number " + employeeRequest.phoneNumber() + " is already in use");
        }

        // Проверяем уникальность email, исключая текущего сотрудника
        if (employeeRepository.existsByEmail(employeeRequest.email()) &&
                !targetEmployee.getEmail().equals(employeeRequest.email())) {
            throw new ConflictException("Email " + employeeRequest.email() + " is already in use");
        }

        // Обновляем сотрудника
        Employee employeeToUpdate = employeeMapper.partialUpdate(employeeRequest, targetEmployee);
        Employee updatedEmployee = saveUpdates(employeeToUpdate);

        // Преобразуем в DTO и возвращаем
        return employeeMapper.toEmployeeResponse(updatedEmployee);
    }

    @Override
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Employee not found with id = " + id);
        }
        cache.remove(id);
        employeeRepository.deleteById(id);
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByFirstName(String firstName) {
        var employees = employeeMapper
                .toEmployeeResponseList(employeeRepository.findByFirstName(firstName));

        return getEmployeeResponses(employees);
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByAssignmentId(Long id) {
        if (!assignmentRepository.existsById(id)) {
            return Collections.emptyList();
        }

        var employees = employeeMapper.toEmployeeResponseList(
                employeeRepository.findEmployeesByAssignmentId(id));

        return getEmployeeResponses(employees);
    }

    @Override
    public EmployeeResponse addAssignmentToEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with such id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with id = " + assignmentId));

        if (employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " already has assignment with id = " + assignmentId);
        }

        employee.getAssignments().add(assignment);

        return employeeMapper.toEmployeeResponse(saveUpdates(employee));
    }

    @Override
    public List<EmployeeResponse> searchEmployeesByLastName(String lastName) {
        var employees = employeeMapper.toEmployeeResponseList(
                employeeRepository.findByLastName(lastName));

        return getEmployeeResponses(employees);
    }

    private List<EmployeeResponse> getEmployeeResponses(List<EmployeeResponse> employees) {
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }

        List<EmployeeResponse> cachedEmployees = employees.stream()
                .map(employee -> cache.get(employee.id()))
                .filter(Objects::nonNull)
                .toList();

        if (cachedEmployees.size() == employees.size()) {
            return cachedEmployees;
        }

        employees.forEach(employee -> cache.put(employee.id(), employee));
        return employees;
    }

    @Override
    public EmployeeResponse deleteAssignmentFromEmployee(Long employeeId, Long assignmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with Id = " + employeeId));

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Assignment not found with Id = " + assignmentId));

        if (!employee.getAssignments().contains(assignment)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Employee with id = " + employeeId
                            + " does not have assignment with id = " + assignmentId);
        }

        employee.getAssignments().remove(assignment);

        return employeeMapper.toEmployeeResponse(saveUpdates(employee));
    }

    @Override
    public List<EmployeeResponse> bulkUpsertEmployees(List<EmployeeRequest> employeeRequests) {
        return employeeRequests.stream()
                .map(this::processEmployeeRequest)
                .collect(Collectors.toList());
    }

    private EmployeeResponse processEmployeeRequest(EmployeeRequest employeeRequest) {
        Employee existingEmployee = findExistingEmployee(employeeRequest);
        if (existingEmployee != null) {
            return updateExistingEmployee(employeeRequest, existingEmployee);
        } else {
            return createNewEmployee(employeeRequest);
        }
    }

    private Employee findExistingEmployee(EmployeeRequest employeeRequest) {
        boolean existsByPhone = employeeRepository
                .existsByPhoneNumber(employeeRequest.phoneNumber());
        boolean existsByEmail = employeeRepository
                .existsByEmail(employeeRequest.email());

        if (existsByPhone || existsByEmail) {
            return employeeRepository.findAll().stream()
                    .filter(emp -> emp.getPhoneNumber().equals(employeeRequest.phoneNumber())
                            || emp.getEmail().equals(employeeRequest.email()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private EmployeeResponse updateExistingEmployee(
            EmployeeRequest employeeRequest, Employee existingEmployee) {
        if (!existingEmployee.getPhoneNumber().equals(employeeRequest.phoneNumber())
                && employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException(
                    "Phone number " + employeeRequest.phoneNumber()
                            + " is already in use by another employee");
        }
        if (!existingEmployee.getEmail().equals(employeeRequest.email())
                && employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException(
                    "Email " + employeeRequest.email() + " is already in use by another employee");
        }

        Employee employeeToUpdate = employeeMapper.partialUpdate(employeeRequest, existingEmployee);
        Employee updatedEmployee = saveUpdates(employeeToUpdate);
        return employeeMapper.toEmployeeResponse(updatedEmployee);
    }

    private EmployeeResponse createNewEmployee(EmployeeRequest employeeRequest) {
        if (employeeRepository.existsByPhoneNumber(employeeRequest.phoneNumber())) {
            throw new ConflictException(
                    "Phone number " + employeeRequest.phoneNumber() + " is already in use");
        }
        if (employeeRepository.existsByEmail(employeeRequest.email())) {
            throw new ConflictException(
                    "Email " + employeeRequest.email() + " is already in use");
        }

        Employee newEmployee = employeeMapper.toEmployee(employeeRequest);
        Employee savedEmployee = employeeRepository.save(newEmployee);
        return employeeMapper.toEmployeeResponse(savedEmployee);
    }

    private Employee saveUpdates(Employee employee) {
        if (cache.containsKey(employee.getId())) {
            cache.put(employee.getId(), employeeMapper.toEmployeeResponse(employee));
        }
        return employeeRepository.save(employee);
    }

    @Override
    public List<EmployeeResponse> searchByInitials(String firstName, String lastName) {
        try {
            // Очищаем входные данные
            String trimmedFirstName = firstName != null ? firstName.trim() : null;
            String trimmedLastName = lastName != null ? lastName.trim() : null;

            // Получаем поток сотрудников в зависимости от входных данных
            Stream<Employee> employeeStream;

            // Если оба поля пустые или null, возвращаем всех сотрудников
            if ((trimmedFirstName == null || trimmedFirstName.isEmpty()) &&
                    (trimmedLastName == null || trimmedLastName.isEmpty())) {
                employeeStream = employeeRepository.findAll().stream();
            } else if (trimmedFirstName != null && trimmedLastName != null) {
                // Если оба поля указаны, ищем по обоим
                employeeStream = employeeRepository.findByFirstNameAndLastName(trimmedFirstName, trimmedLastName).stream();
            } else {
                // Создаём потоки для поиска по firstName и lastName
                Stream<Employee> firstNameStream = trimmedFirstName != null && !trimmedFirstName.isEmpty()
                        ? employeeRepository.findByFirstName(trimmedFirstName).stream()
                        : Stream.empty();

                Stream<Employee> lastNameStream = trimmedLastName != null && !trimmedLastName.isEmpty()
                        ? employeeRepository.findByLastName(trimmedLastName).stream()
                        : Stream.empty();

                // Объединяем результаты поиска, удаляем дубликаты
                employeeStream = Stream.concat(firstNameStream, lastNameStream)
                        .distinct();
            }

            // Преобразуем поток в список
            List<Employee> employees = employeeStream.toList();

            // Проверяем, что список не пустой, используя Optional
            return Optional.of(employees)
                    .filter(list -> !list.isEmpty())
                    .map(employeeMapper::toEmployeeResponseList)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Сотрудники с указанными данными не найдены"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Произошла ошибка при поиске сотрудников: " + e.getMessage());
        }
    }
}
package com.accesscontrol.employee.service;

import com.accesscontrol.employee.dto.EmployeeDTO;
import com.accesscontrol.employee.model.Employee;
import com.accesscontrol.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service - Pruebas de Caja Blanca")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        // Configurar empleado de prueba
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setDocument("12345678");
        testEmployee.setEmployeeCode("EMP001");
        testEmployee.setFirstname("Juan");
        testEmployee.setLastname("Perez");
        testEmployee.setEmail("juan@example.com");
        testEmployee.setPhone("3001234567");
        testEmployee.setDepartment("IT");
        testEmployee.setPosition("Developer");
        testEmployee.setStatus(true);
        testEmployee.setCreatedAt(LocalDateTime.now());
        testEmployee.setUpdatedAt(LocalDateTime.now());

        // Configurar DTO de prueba
        testEmployeeDTO = new EmployeeDTO(testEmployee);
    }

    // ============================================
    // PRUEBAS: getAllEmployees()
    // ============================================
    @Test
    @DisplayName("getAllEmployees - Retorna lista de empleados")
    void testGetAllEmployees() {
        // Arrange
        List<Employee> employees = Arrays.asList(testEmployee);
        when(employeeRepository.findAll()).thenReturn(employees);

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertThat(result).isNotNull().hasSize(1);
        assertThat(result.get(0).getDocument()).isEqualTo("12345678");
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllEmployees - Retorna lista vacía cuando no hay empleados")
    void testGetAllEmployeesEmpty() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<EmployeeDTO> result = employeeService.getAllEmployees();

        // Assert
        assertThat(result).isEmpty();
        verify(employeeRepository, times(1)).findAll();
    }

    // ============================================
    // PRUEBAS: getEmployeeById(Long id)
    // ============================================
    @Test
    @DisplayName("getEmployeeById - Obtiene empleado por ID válido")
    void testGetEmployeeByIdValid() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDocument()).isEqualTo("12345678");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getEmployeeById - Lanza excepción cuando ID no existe")
    void testGetEmployeeByIdNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id: 999");
        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("getEmployeeById - Lanza excepción cuando ID es null")
    void testGetEmployeeByIdNull() {
        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Employee id is required");
    }

    // ============================================
    // PRUEBAS: getEmployeeByCode(String code)
    // ============================================
    @Test
    @DisplayName("getEmployeeByCode - Obtiene empleado por código válido")
    void testGetEmployeeByCodeValid() {
        // Arrange
        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByCode("EMP001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmployeeCode()).isEqualTo("EMP001");
        verify(employeeRepository, times(1)).findByEmployeeCode("EMP001");
    }

    @Test
    @DisplayName("getEmployeeByCode - Lanza excepción cuando código no existe")
    void testGetEmployeeByCodeNotFound() {
        // Arrange
        when(employeeRepository.findByEmployeeCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeByCode("INVALID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with code: INVALID");
    }

    // ============================================
    // PRUEBAS: getEmployeeByDocument(String doc)
    // ============================================
    @Test
    @DisplayName("getEmployeeByDocument - Obtiene empleado por documento válido")
    void testGetEmployeeByDocumentValid() {
        // Arrange
        when(employeeRepository.findByDocument("12345678")).thenReturn(Optional.of(testEmployee));

        // Act
        EmployeeDTO result = employeeService.getEmployeeByDocument("12345678");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDocument()).isEqualTo("12345678");
        verify(employeeRepository, times(1)).findByDocument("12345678");
    }

    @Test
    @DisplayName("getEmployeeByDocument - Lanza excepción cuando documento no existe")
    void testGetEmployeeByDocumentNotFound() {
        // Arrange
        when(employeeRepository.findByDocument("99999999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeByDocument("99999999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with document: 99999999");
    }

    // ============================================
    // PRUEBAS: createEmployee(EmployeeDTO dto) - COMPLEJIDAD ALTA
    // ============================================
    @Test
    @DisplayName("createEmployee - Crea empleado exitosamente")
    void testCreateEmployeeSuccess() {
        // Arrange
        when(employeeRepository.existsByDocument(testEmployeeDTO.getDocument())).thenReturn(false);
        when(employeeRepository.existsByEmail(testEmployeeDTO.getEmail())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDocument()).isEqualTo("12345678");
        assertThat(result.getStatus()).isTrue();
        verify(employeeRepository, times(1)).existsByDocument("12345678");
        verify(employeeRepository, times(1)).existsByEmail("juan@example.com");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - Lanza excepción cuando documento ya existe")
    void testCreateEmployeeDocumentExists() {
        // Arrange
        when(employeeRepository.existsByDocument("12345678")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.createEmployee(testEmployeeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Document already exists: 12345678");
        verify(employeeRepository, times(1)).existsByDocument("12345678");
        verify(employeeRepository, never()).existsByEmail(anyString());
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - Lanza excepción cuando email ya existe")
    void testCreateEmployeeEmailExists() {
        // Arrange
        when(employeeRepository.existsByDocument("12345678")).thenReturn(false);
        when(employeeRepository.existsByEmail("juan@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.createEmployee(testEmployeeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists: juan@example.com");
        verify(employeeRepository, times(1)).existsByDocument("12345678");
        verify(employeeRepository, times(1)).existsByEmail("juan@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee - Establece status por defecto a true cuando es null")
    void testCreateEmployeeStatusDefault() {
        // Arrange
        testEmployeeDTO.setStatus(null);
        when(employeeRepository.existsByDocument(anyString())).thenReturn(false);
        when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.createEmployee(testEmployeeDTO);

        // Assert
        assertThat(result.getStatus()).isTrue();
    }

    // ============================================
    // PRUEBAS: updateEmployee(Long id, EmployeeDTO) - COMPLEJIDAD ALTA
    // ============================================
    @Test
    @DisplayName("updateEmployee - Actualiza empleado exitosamente (sin cambiar código ni email)")
    void testUpdateEmployeeSuccess() {
        // Arrange
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setDocument("87654321");
        updateDTO.setEmployeeCode("EMP001");
        updateDTO.setFirstname("Juan");
        updateDTO.setLastname("Perez");
        updateDTO.setEmail("juan@example.com");
        updateDTO.setPhone("3009999999");
        updateDTO.setDepartment("HR");
        updateDTO.setPosition("Manager");
        updateDTO.setStatus(false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployee(1L, updateDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee - Lanza excepción cuando empleado no existe")
    void testUpdateEmployeeNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(999L, testEmployeeDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id: 999");
        verify(employeeRepository, times(1)).findById(999L);
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee - Lanza excepción cuando ID es null")
    void testUpdateEmployeeIdNull() {
        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(null, testEmployeeDTO))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Employee id is required");
    }

    @Test
    @DisplayName("updateEmployee - Lanza excepción cuando nuevo código ya existe")
    void testUpdateEmployeeCodeExists() {
        // Arrange
        testEmployee.setEmployeeCode("EMP001");
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setEmployeeCode("EMP002");
        updateDTO.setDocument("87654321");
        updateDTO.setFirstname("Juan");
        updateDTO.setLastname("Perez");
        updateDTO.setEmail("juan@example.com");
        updateDTO.setStatus(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmployeeCode("EMP002")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee code already exists: EMP002");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee - Lanza excepción cuando nuevo email ya existe")
    void testUpdateEmployeeEmailExists() {
        // Arrange
        testEmployee.setEmail("juan@example.com");
        EmployeeDTO updateDTO = new EmployeeDTO();
        updateDTO.setEmployeeCode("EMP001");
        updateDTO.setDocument("87654321");
        updateDTO.setFirstname("Juan");
        updateDTO.setLastname("Perez");
        updateDTO.setEmail("newemail@example.com");
        updateDTO.setStatus(true);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.existsByEmployeeCode("EMP001")).thenReturn(false);
        when(employeeRepository.existsByEmail("newemail@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, updateDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists: newemail@example.com");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // ============================================
    // PRUEBAS: deleteEmployee(Long id)
    // ============================================
    @Test
    @DisplayName("deleteEmployee - Siempre lanza UnsupportedOperationException")
    void testDeleteEmployee() {
        // Act & Assert
        assertThatThrownBy(() -> employeeService.deleteEmployee(1L))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Employee deletion is disabled");
    }

    // ============================================
    // PRUEBAS: updateEmployeeStatus(Long id, String status) - COBERTURA DE DECISIÓN
    // ============================================
    @Test
    @DisplayName("updateEmployeeStatus - Establece status a true con 'ACTIVE'")
    void testUpdateEmployeeStatusActive() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeStatus(1L, "ACTIVE");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployeeStatus - Establece status a true con 'true'")
    void testUpdateEmployeeStatusTrue() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeStatus(1L, "true");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployeeStatus - Establece status a false con 'INACTIVE'")
    void testUpdateEmployeeStatusInactive() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeStatus(1L, "INACTIVE");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployeeStatus - Establece status a false con 'false'")
    void testUpdateEmployeeStatusFalse() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        // Act
        EmployeeDTO result = employeeService.updateEmployeeStatus(1L, "false");

        // Assert
        verify(employeeRepository, times(1)).findById(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployeeStatus - Lanza excepción cuando ID es null")
    void testUpdateEmployeeStatusIdNull() {
        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(null, "ACTIVE"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Employee id is required");
    }

    @Test
    @DisplayName("updateEmployeeStatus - Lanza excepción cuando empleado no existe")
    void testUpdateEmployeeStatusNotFound() {
        // Arrange
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.updateEmployeeStatus(999L, "ACTIVE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Employee not found with id: 999");
    }
}

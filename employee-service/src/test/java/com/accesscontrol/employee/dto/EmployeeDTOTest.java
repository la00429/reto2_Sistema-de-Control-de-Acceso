package com.accesscontrol.employee.dto;

import com.accesscontrol.employee.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Employee DTO - Pruebas de Caja Blanca")
class EmployeeDTOTest {

    private Employee testEmployee;
    private EmployeeDTO employeeDTO;

    @BeforeEach
    void setUp() {
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
    }

    // ============================================
    // PRUEBAS: Constructor sin argumentos
    // ============================================
    @Test
    @DisplayName("Constructor sin argumentos - Crea DTO vacío")
    void testConstructorNoArgs() {
        // Act
        EmployeeDTO dto = new EmployeeDTO();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isNull();
        assertThat(dto.getDocument()).isNull();
        assertThat(dto.getFirstname()).isNull();
        assertThat(dto.getLastname()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getStatus()).isNull();
    }

    // ============================================
    // PRUEBAS: Constructor con Employee
    // ============================================
    @Test
    @DisplayName("Constructor con Employee - Mapea todos los campos correctamente")
    void testConstructorWithEmployee() {
        // Act
        EmployeeDTO dto = new EmployeeDTO(testEmployee);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDocument()).isEqualTo("12345678");
        assertThat(dto.getEmployeeCode()).isEqualTo("EMP001");
        assertThat(dto.getFirstname()).isEqualTo("Juan");
        assertThat(dto.getLastname()).isEqualTo("Perez");
        assertThat(dto.getEmail()).isEqualTo("juan@example.com");
        assertThat(dto.getPhone()).isEqualTo("3001234567");
        assertThat(dto.getDepartment()).isEqualTo("IT");
        assertThat(dto.getPosition()).isEqualTo("Developer");
        assertThat(dto.getStatus()).isTrue();
    }

    @Test
    @DisplayName("Constructor con Employee - Maneja valores null correctamente")
    void testConstructorWithEmployeeNullValues() {
        // Arrange
        testEmployee.setPhone(null);
        testEmployee.setDepartment(null);
        testEmployee.setPosition(null);
        testEmployee.setEmployeeCode(null);

        // Act
        EmployeeDTO dto = new EmployeeDTO(testEmployee);

        // Assert
        assertThat(dto.getPhone()).isNull();
        assertThat(dto.getDepartment()).isNull();
        assertThat(dto.getPosition()).isNull();
        assertThat(dto.getEmployeeCode()).isNull();
    }

    @Test
    @DisplayName("Constructor con Employee - Maneja status false")
    void testConstructorWithEmployeeStatusFalse() {
        // Arrange
        testEmployee.setStatus(false);

        // Act
        EmployeeDTO dto = new EmployeeDTO(testEmployee);

        // Assert
        assertThat(dto.getStatus()).isFalse();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Id
    // ============================================
    @Test
    @DisplayName("getId/setId - Get y set de ID funciona correctamente")
    void testIdGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setId(42L);

        // Assert
        assertThat(employeeDTO.getId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("getId/setId - Maneja ID null")
    void testIdGetterSetterNull() {
        // Arrange
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(42L);

        // Act
        employeeDTO.setId(null);

        // Assert
        assertThat(employeeDTO.getId()).isNull();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Document
    // ============================================
    @Test
    @DisplayName("getDocument/setDocument - Get y set de Document funciona correctamente")
    void testDocumentGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setDocument("87654321");

        // Assert
        assertThat(employeeDTO.getDocument()).isEqualTo("87654321");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - EmployeeCode
    // ============================================
    @Test
    @DisplayName("getEmployeeCode/setEmployeeCode - Funciona correctamente")
    void testEmployeeCodeGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setEmployeeCode("EMP999");

        // Assert
        assertThat(employeeDTO.getEmployeeCode()).isEqualTo("EMP999");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Firstname
    // ============================================
    @Test
    @DisplayName("getFirstname/setFirstname - Funciona correctamente")
    void testFirstnameGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setFirstname("Carlos");

        // Assert
        assertThat(employeeDTO.getFirstname()).isEqualTo("Carlos");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Lastname
    // ============================================
    @Test
    @DisplayName("getLastname/setLastname - Funciona correctamente")
    void testLastnameGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setLastname("Garcia");

        // Assert
        assertThat(employeeDTO.getLastname()).isEqualTo("Garcia");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Email
    // ============================================
    @Test
    @DisplayName("getEmail/setEmail - Funciona correctamente")
    void testEmailGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setEmail("test@example.com");

        // Assert
        assertThat(employeeDTO.getEmail()).isEqualTo("test@example.com");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Phone
    // ============================================
    @Test
    @DisplayName("getPhone/setPhone - Funciona correctamente")
    void testPhoneGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setPhone("3049999999");

        // Assert
        assertThat(employeeDTO.getPhone()).isEqualTo("3049999999");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Department
    // ============================================
    @Test
    @DisplayName("getDepartment/setDepartment - Funciona correctamente")
    void testDepartmentGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setDepartment("Sales");

        // Assert
        assertThat(employeeDTO.getDepartment()).isEqualTo("Sales");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Position
    // ============================================
    @Test
    @DisplayName("getPosition/setPosition - Funciona correctamente")
    void testPositionGetterSetter() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setPosition("Manager");

        // Assert
        assertThat(employeeDTO.getPosition()).isEqualTo("Manager");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Status
    // ============================================
    @Test
    @DisplayName("getStatus/setStatus - Establece status true")
    void testStatusGetterSetterTrue() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setStatus(true);

        // Assert
        assertThat(employeeDTO.getStatus()).isTrue();
    }

    @Test
    @DisplayName("getStatus/setStatus - Establece status false")
    void testStatusGetterSetterFalse() {
        // Arrange
        employeeDTO = new EmployeeDTO();

        // Act
        employeeDTO.setStatus(false);

        // Assert
        assertThat(employeeDTO.getStatus()).isFalse();
    }

    @Test
    @DisplayName("getStatus/setStatus - Maneja status null")
    void testStatusGetterSetterNull() {
        // Arrange
        employeeDTO = new EmployeeDTO();
        employeeDTO.setStatus(true);

        // Act
        employeeDTO.setStatus(null);

        // Assert
        assertThat(employeeDTO.getStatus()).isNull();
    }

    // ============================================
    // PRUEBAS: Mapeo completo (ida y vuelta)
    // ============================================
    @Test
    @DisplayName("Mapeo completo - DTO a Entity y de vuelta es consistente")
    void testCompleteMappingCycle() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO(testEmployee);

        // Act - Crear un nuevo Employee a partir del DTO y convertirlo de vuelta
        Employee newEmployee = new Employee();
        newEmployee.setId(dto.getId());
        newEmployee.setDocument(dto.getDocument());
        newEmployee.setEmployeeCode(dto.getEmployeeCode());
        newEmployee.setFirstname(dto.getFirstname());
        newEmployee.setLastname(dto.getLastname());
        newEmployee.setEmail(dto.getEmail());
        newEmployee.setPhone(dto.getPhone());
        newEmployee.setDepartment(dto.getDepartment());
        newEmployee.setPosition(dto.getPosition());
        newEmployee.setStatus(dto.getStatus());

        EmployeeDTO dto2 = new EmployeeDTO(newEmployee);

        // Assert - Comparar que los valores sean iguales
        assertThat(dto.getId()).isEqualTo(dto2.getId());
        assertThat(dto.getDocument()).isEqualTo(dto2.getDocument());
        assertThat(dto.getEmployeeCode()).isEqualTo(dto2.getEmployeeCode());
        assertThat(dto.getFirstname()).isEqualTo(dto2.getFirstname());
        assertThat(dto.getLastname()).isEqualTo(dto2.getLastname());
        assertThat(dto.getEmail()).isEqualTo(dto2.getEmail());
        assertThat(dto.getPhone()).isEqualTo(dto2.getPhone());
        assertThat(dto.getDepartment()).isEqualTo(dto2.getDepartment());
        assertThat(dto.getPosition()).isEqualTo(dto2.getPosition());
        assertThat(dto.getStatus()).isEqualTo(dto2.getStatus());
    }
}

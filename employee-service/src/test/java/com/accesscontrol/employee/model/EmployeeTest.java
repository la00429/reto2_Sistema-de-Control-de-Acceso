package com.accesscontrol.employee.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Employee Model - Pruebas de Caja Blanca")
class EmployeeTest {

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
    }

    // ============================================
    // PRUEBAS: Constructor y creación
    // ============================================
    @Test
    @DisplayName("Constructor - Crea empleado sin argumentos")
    void testConstructorNoArgs() {
        // Assert
        assertThat(employee).isNotNull();
        assertThat(employee.getId()).isNull();
        assertThat(employee.getDocument()).isNull();
        assertThat(employee.getFirstname()).isNull();
        assertThat(employee.getLastname()).isNull();
        assertThat(employee.getStatus()).isNull();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Id
    // ============================================
    @Test
    @DisplayName("getId/setId - Get y set de ID funciona correctamente")
    void testIdGetterSetter() {
        // Act
        employee.setId(1L);

        // Assert
        assertThat(employee.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getId/setId - Maneja valores grandes de ID")
    void testIdGetterSetterLarge() {
        // Act
        employee.setId(Long.MAX_VALUE);

        // Assert
        assertThat(employee.getId()).isEqualTo(Long.MAX_VALUE);
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Document
    // ============================================
    @Test
    @DisplayName("getDocument/setDocument - Funciona correctamente")
    void testDocumentGetterSetter() {
        // Act
        employee.setDocument("12345678");

        // Assert
        assertThat(employee.getDocument()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("getDocument/setDocument - Maneja documento vacío")
    void testDocumentGetterSetterEmpty() {
        // Act
        employee.setDocument("");

        // Assert
        assertThat(employee.getDocument()).isEmpty();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Firstname
    // ============================================
    @Test
    @DisplayName("getFirstname/setFirstname - Funciona correctamente")
    void testFirstnameGetterSetter() {
        // Act
        employee.setFirstname("Juan");

        // Assert
        assertThat(employee.getFirstname()).isEqualTo("Juan");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Lastname
    // ============================================
    @Test
    @DisplayName("getLastname/setLastname - Funciona correctamente")
    void testLastnameGetterSetter() {
        // Act
        employee.setLastname("Perez");

        // Assert
        assertThat(employee.getLastname()).isEqualTo("Perez");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Email
    // ============================================
    @Test
    @DisplayName("getEmail/setEmail - Funciona correctamente")
    void testEmailGetterSetter() {
        // Act
        employee.setEmail("juan@example.com");

        // Assert
        assertThat(employee.getEmail()).isEqualTo("juan@example.com");
    }

    @Test
    @DisplayName("getEmail/setEmail - Maneja emails con caracteres especiales")
    void testEmailGetterSetterSpecialChars() {
        // Act
        employee.setEmail("juan+test@sub.example.co.uk");

        // Assert
        assertThat(employee.getEmail()).isEqualTo("juan+test@sub.example.co.uk");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Phone
    // ============================================
    @Test
    @DisplayName("getPhone/setPhone - Funciona correctamente")
    void testPhoneGetterSetter() {
        // Act
        employee.setPhone("3001234567");

        // Assert
        assertThat(employee.getPhone()).isEqualTo("3001234567");
    }

    @Test
    @DisplayName("getPhone/setPhone - Maneja phone null")
    void testPhoneGetterSetterNull() {
        // Act
        employee.setPhone(null);

        // Assert
        assertThat(employee.getPhone()).isNull();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Status
    // ============================================
    @Test
    @DisplayName("getStatus/setStatus - Establece status true")
    void testStatusGetterSetterTrue() {
        // Act
        employee.setStatus(true);

        // Assert
        assertThat(employee.getStatus()).isTrue();
    }

    @Test
    @DisplayName("getStatus/setStatus - Establece status false")
    void testStatusGetterSetterFalse() {
        // Act
        employee.setStatus(false);

        // Assert
        assertThat(employee.getStatus()).isFalse();
    }

    // ============================================
    // PRUEBAS: Getters y Setters - EmployeeCode
    // ============================================
    @Test
    @DisplayName("getEmployeeCode/setEmployeeCode - Funciona correctamente")
    void testEmployeeCodeGetterSetter() {
        // Act
        employee.setEmployeeCode("EMP001");

        // Assert
        assertThat(employee.getEmployeeCode()).isEqualTo("EMP001");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Department
    // ============================================
    @Test
    @DisplayName("getDepartment/setDepartment - Funciona correctamente")
    void testDepartmentGetterSetter() {
        // Act
        employee.setDepartment("IT");

        // Assert
        assertThat(employee.getDepartment()).isEqualTo("IT");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - Position
    // ============================================
    @Test
    @DisplayName("getPosition/setPosition - Funciona correctamente")
    void testPositionGetterSetter() {
        // Act
        employee.setPosition("Developer");

        // Assert
        assertThat(employee.getPosition()).isEqualTo("Developer");
    }

    // ============================================
    // PRUEBAS: Getters y Setters - CreatedAt
    // ============================================
    @Test
    @DisplayName("getCreatedAt/setCreatedAt - Funciona correctamente")
    void testCreatedAtGetterSetter() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        employee.setCreatedAt(now);

        // Assert
        assertThat(employee.getCreatedAt()).isEqualTo(now);
    }

    // ============================================
    // PRUEBAS: Getters y Setters - UpdatedAt
    // ============================================
    @Test
    @DisplayName("getUpdatedAt/setUpdatedAt - Funciona correctamente")
    void testUpdatedAtGetterSetter() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        employee.setUpdatedAt(now);

        // Assert
        assertThat(employee.getUpdatedAt()).isEqualTo(now);
    }

    // ============================================
    // PRUEBAS: @PrePersist - onCreate()
    // ============================================
    @Test
    @DisplayName("onCreate - Establece createdAt y updatedAt cuando se persiste")
    void testOnCreate() {
        // Arrange
        employee.setDocument("12345678");
        employee.setFirstname("Juan");
        employee.setLastname("Perez");
        employee.setEmail("juan@example.com");
        employee.setStatus(true);

        LocalDateTime beforeCreate = LocalDateTime.now();

        // Act
        employee.onCreate();

        LocalDateTime afterCreate = LocalDateTime.now();

        // Assert
        assertThat(employee.getCreatedAt()).isNotNull()
                .isAfterOrEqualTo(beforeCreate)
                .isBeforeOrEqualTo(afterCreate);
        assertThat(employee.getUpdatedAt()).isNotNull()
                .isAfterOrEqualTo(beforeCreate)
                .isBeforeOrEqualTo(afterCreate);
        assertThat(employee.getCreatedAt()).isEqualTo(employee.getUpdatedAt());
    }

    @Test
    @DisplayName("onCreate - Sobrescribe fechas previamente establecidas")
    void testOnCreateOverwrite() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        employee.setCreatedAt(oldDate);
        employee.setUpdatedAt(oldDate);

        // Act
        employee.onCreate();

        // Assert
        assertThat(employee.getCreatedAt()).isAfter(oldDate);
        assertThat(employee.getUpdatedAt()).isAfter(oldDate);
    }

    // ============================================
    // PRUEBAS: @PreUpdate - onUpdate()
    // ============================================
    @Test
    @DisplayName("onUpdate - Actualiza solo updatedAt sin cambiar createdAt")
    void testOnUpdate() throws InterruptedException {
        // Arrange
        LocalDateTime createdTime = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        employee.setCreatedAt(createdTime);
        employee.setUpdatedAt(createdTime);

        LocalDateTime beforeUpdate = LocalDateTime.now();
        Thread.sleep(10); // Pequeño delay para asegurar diferencia de tiempo

        // Act
        employee.onUpdate();

        LocalDateTime afterUpdate = LocalDateTime.now();

        // Assert
        assertThat(employee.getCreatedAt()).isEqualTo(createdTime); // Sin cambios
        assertThat(employee.getUpdatedAt()).isNotNull()
                .isAfter(createdTime)
                .isAfterOrEqualTo(beforeUpdate)
                .isBeforeOrEqualTo(afterUpdate);
    }

    @Test
    @DisplayName("onUpdate - Actualiza correctamente múltiples veces")
    void testOnUpdateMultipleTimes() throws InterruptedException {
        // Arrange
        LocalDateTime createdTime = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        employee.setCreatedAt(createdTime);
        employee.setUpdatedAt(createdTime);

        // Act - Primera actualización
        employee.onUpdate();
        LocalDateTime firstUpdate = employee.getUpdatedAt();

        Thread.sleep(10);

        // Act - Segunda actualización
        employee.onUpdate();
        LocalDateTime secondUpdate = employee.getUpdatedAt();

        // Assert
        assertThat(employee.getCreatedAt()).isEqualTo(createdTime);
        assertThat(secondUpdate).isAfter(firstUpdate);
    }

    // ============================================
    // PRUEBAS: Integración completa
    // ============================================
    @Test
    @DisplayName("Integración - Asigna todos los campos correctamente")
    void testCompleteEmployeeSetup() {
        // Arrange & Act
        employee.setId(1L);
        employee.setDocument("12345678");
        employee.setEmployeeCode("EMP001");
        employee.setFirstname("Juan");
        employee.setLastname("Perez");
        employee.setEmail("juan@example.com");
        employee.setPhone("3001234567");
        employee.setDepartment("IT");
        employee.setPosition("Developer");
        employee.setStatus(true);

        // Assert
        assertThat(employee)
                .extracting(Employee::getId, Employee::getDocument, Employee::getEmployeeCode,
                           Employee::getFirstname, Employee::getLastname, Employee::getEmail,
                           Employee::getPhone, Employee::getDepartment, Employee::getPosition,
                           Employee::getStatus)
                .containsExactly(1L, "12345678", "EMP001", "Juan", "Perez",
                               "juan@example.com", "3001234567", "IT", "Developer", true);
    }

    @Test
    @DisplayName("Integración - Maneja campos opcionales como null")
    void testEmployeeWithOptionalFields() {
        // Arrange & Act
        employee.setId(1L);
        employee.setDocument("12345678");
        employee.setFirstname("Juan");
        employee.setLastname("Perez");
        employee.setEmail("juan@example.com");
        employee.setStatus(true);
        // Phone, Department, Position se dejan sin asignar (null)

        // Assert
        assertThat(employee.getId()).isEqualTo(1L);
        assertThat(employee.getPhone()).isNull();
        assertThat(employee.getDepartment()).isNull();
        assertThat(employee.getPosition()).isNull();
        assertThat(employee.getStatus()).isTrue();
    }
}

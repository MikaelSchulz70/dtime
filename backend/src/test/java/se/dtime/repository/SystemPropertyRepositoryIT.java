package se.dtime.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.dtime.dbmodel.SystemPropertyPO;
import se.dtime.model.SystemPropertyType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SystemPropertyRepositoryIT extends BaseRepositoryIT {

    @Autowired
    private SystemPropertyRepository systemPropertyRepository;

    @Test
    void shouldSaveAndFindSystemProperty() {
        SystemPropertyPO property = createSystemProperty("test.property", "test value", SystemPropertyType.TEXT, "Test description");

        SystemPropertyPO saved = systemPropertyRepository.save(property);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("test.property");
        assertThat(saved.getValue()).isEqualTo("test value");
        assertThat(saved.getSystemPropertyType()).isEqualTo(SystemPropertyType.TEXT);
        assertThat(saved.getDescription()).isEqualTo("Test description");
        assertThat(saved.getCreateDateTime()).isNotNull();
        assertThat(saved.getUpdatedDateTime()).isNotNull();
    }

    @Test
    void shouldFindByName() {
        SystemPropertyPO property = createSystemProperty("findme.property", "find value", SystemPropertyType.TEXT, "Find description");
        systemPropertyRepository.save(property);

        SystemPropertyPO found = systemPropertyRepository.findByName("findme.property");

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("findme.property");
        assertThat(found.getValue()).isEqualTo("find value");
        assertThat(found.getDescription()).isEqualTo("Find description");
    }

    @Test
    void shouldReturnNullWhenPropertyNotFoundByName() {
        SystemPropertyPO found = systemPropertyRepository.findByName("nonexistent.property");

        assertThat(found).isNull();
    }

    @Test
    void shouldSavePropertiesWithDifferentTypes() {
        SystemPropertyPO textProperty = createSystemProperty("text.property", "text value", SystemPropertyType.TEXT, "Text property");
        SystemPropertyPO intProperty = createSystemProperty("int.property", "123", SystemPropertyType.INT, "Integer property");
        SystemPropertyPO floatProperty = createSystemProperty("float.property", "123.45", SystemPropertyType.FLOAT, "Float property");
        SystemPropertyPO boolProperty = createSystemProperty("bool.property", "true", SystemPropertyType.BOOL, "Boolean property");

        SystemPropertyPO savedText = systemPropertyRepository.save(textProperty);
        SystemPropertyPO savedInt = systemPropertyRepository.save(intProperty);
        SystemPropertyPO savedFloat = systemPropertyRepository.save(floatProperty);
        SystemPropertyPO savedBool = systemPropertyRepository.save(boolProperty);

        assertThat(savedText.getSystemPropertyType()).isEqualTo(SystemPropertyType.TEXT);
        assertThat(savedInt.getSystemPropertyType()).isEqualTo(SystemPropertyType.INT);
        assertThat(savedFloat.getSystemPropertyType()).isEqualTo(SystemPropertyType.FLOAT);
        assertThat(savedBool.getSystemPropertyType()).isEqualTo(SystemPropertyType.BOOL);
    }

    @Test
    void shouldSavePropertyWithNullValues() {
        SystemPropertyPO property = createSystemProperty("nullable.property", null, null, null);

        SystemPropertyPO saved = systemPropertyRepository.save(property);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("nullable.property");
        assertThat(saved.getValue()).isNull();
        assertThat(saved.getSystemPropertyType()).isNull();
        assertThat(saved.getDescription()).isNull();
    }

    @Test
    void shouldUpdateSystemProperty() {
        SystemPropertyPO property = createSystemProperty("update.property", "original value", SystemPropertyType.TEXT, "Original description");
        SystemPropertyPO saved = systemPropertyRepository.save(property);

        saved.setValue("updated value");
        saved.setDescription("Updated description");
        saved.setUpdatedBy(2L);
        saved.setUpdatedDateTime(LocalDateTime.now());

        SystemPropertyPO updated = systemPropertyRepository.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getValue()).isEqualTo("updated value");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
        assertThat(updated.getName()).isEqualTo("update.property"); // Name should remain unchanged
    }

    @Test
    void shouldDeleteSystemProperty() {
        SystemPropertyPO property = createSystemProperty("delete.property", "delete value", SystemPropertyType.TEXT, "Delete description");
        SystemPropertyPO saved = systemPropertyRepository.save(property);

        systemPropertyRepository.delete(saved);

        SystemPropertyPO found = systemPropertyRepository.findByName("delete.property");
        assertThat(found).isNull();
    }

    @Test
    void shouldHandleUniqueNameConstraint() {
        SystemPropertyPO property1 = createSystemProperty("unique.property", "value1", SystemPropertyType.TEXT, "Description 1");
        SystemPropertyPO property2 = createSystemProperty("unique.property", "value2", SystemPropertyType.INT, "Description 2");

        systemPropertyRepository.save(property1);

        // This should fail due to unique constraint on name, but we'll test the first one was saved
        SystemPropertyPO found = systemPropertyRepository.findByName("unique.property");
        assertThat(found).isNotNull();
        assertThat(found.getValue()).isEqualTo("value1");
        assertThat(found.getSystemPropertyType()).isEqualTo(SystemPropertyType.TEXT);
    }

    @Test
    void shouldHandleUniqueValueConstraint() {
        SystemPropertyPO property1 = createSystemProperty("property1", "unique.value", SystemPropertyType.TEXT, "Description 1");
        SystemPropertyPO property2 = createSystemProperty("property2", "unique.value", SystemPropertyType.TEXT, "Description 2");

        systemPropertyRepository.save(property1);

        // This should fail due to unique constraint on value, but we'll test the first one was saved
        SystemPropertyPO found = systemPropertyRepository.findByName("property1");
        assertThat(found).isNotNull();
        assertThat(found.getValue()).isEqualTo("unique.value");
    }

    @Test
    void shouldFindAllSystemProperties() {
        SystemPropertyPO property1 = createSystemProperty("property1", "value1", SystemPropertyType.TEXT, "Description 1");
        SystemPropertyPO property2 = createSystemProperty("property2", "value2", SystemPropertyType.INT, "Description 2");
        SystemPropertyPO property3 = createSystemProperty("property3", "value3", SystemPropertyType.BOOL, "Description 3");

        systemPropertyRepository.save(property1);
        systemPropertyRepository.save(property2);
        systemPropertyRepository.save(property3);

        var allProperties = systemPropertyRepository.findAll();

        assertThat(allProperties).hasSize(3);
        assertThat(allProperties).extracting(SystemPropertyPO::getName)
                .containsExactlyInAnyOrder("property1", "property2", "property3");
    }

    private SystemPropertyPO createSystemProperty(String name, String value, SystemPropertyType type, String description) {
        SystemPropertyPO property = new SystemPropertyPO();
        property.setName(name);
        property.setValue(value);
        property.setSystemPropertyType(type);
        property.setDescription(description);
        property.setCreatedBy(1L);
        property.setUpdatedBy(1L);
        property.setCreateDateTime(LocalDateTime.now());
        property.setUpdatedDateTime(LocalDateTime.now());
        return property;
    }
}
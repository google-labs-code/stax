# App specifics

## Table of Contents

- [Enum Converters](#enum-converters)
- [Tagging of entities](#tagging-of-entities)

## Enum Converters

To avoid the `@Enumerated` annotation and the automatic generation of database constraints (such as `CHECK` constraints) by Hibernate, we use custom JPA attribute converters for all enums.

### How to Create a Converter for a New Enum

To create a converter for a new enum, follow these steps:

1. **Create a new class** that extends `AbstractEnumConverter<E>`, where `E` is the type of your enum.
2. **Provide a constructor** that passes the enum class type to the superclass.
3. **Annotate the class with `@Converter(autoApply = false)`** to avoid Hibernate automatically applying it globally (you'll apply the converter explicitly in your entities).

Example:

```java
package com.example.util.converter;

import com.example.model.MyEnum;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MyEnumConverter extends AbstractEnumConverter<MyEnum> {

    public MyEnumConverter() {
        super(MyEnum.class);
    }
}
```

### How to use converter in your entity

Once the converter is created, you can use it in any entity where the enum field needs to be persisted. Simply annotate the enum field with @Convert(converter = MyEnumConverter.class).
Example:

```java
@Entity
public class MyEntity {

    @Id
    private Long id;

    @Convert(converter = MyEnumConverter.class)
    private MyEnum status;
}
```

### Location of Converters

All enum converters should be placed in the com.planck.planck.util.converter package

## Tagging of Entities

To enable tagging, all that needs to be done is:

1. Update the enum TagLinkTargetType
2. Implement a new fetcher (@TaggableEntityFetcher, check ChatTurnFetcher for example)
3. In case of a custom implementation needed, for example work with tagging of child entities from parent entity, add then it would be needed to add respective API methods to TagLinkController & Service.
4. Update TaggedEntitiesResponse to include the new entity

## Adding Tags to response DTOs
1. On the DTO, add List<TagDTO> tags field
2. Implement the com.planck.planck.dto.Taggable interface
3. Override the getId and getTagLinkTargetType as needed
4. The Tags will now appear on the API responses, as long as a TagLink was set for the entity.
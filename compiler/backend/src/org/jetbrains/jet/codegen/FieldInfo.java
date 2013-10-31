/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.codegen;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.asm4.Type;
import org.jetbrains.jet.codegen.state.JetTypeMapper;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ClassKind;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;

public class FieldInfo {
    @NotNull
    public static FieldInfo createForSingleton(@NotNull ClassDescriptor fieldClassDescriptor, @NotNull JetTypeMapper typeMapper) {
        ClassKind kind = fieldClassDescriptor.getKind();
        if (kind != ClassKind.CLASS_OBJECT) {
            throw new IllegalStateException("Only class objects are allowed here: " + fieldClassDescriptor);
        }

        ClassDescriptor container = (ClassDescriptor) fieldClassDescriptor.getContainingDeclaration();

        if (container.getKind() == ClassKind.ENUM_ENTRY) {
            ClassDescriptor enumClass = (ClassDescriptor) container.getContainingDeclaration();
            Type enumType = typeMapper.mapType(enumClass);
            return new FieldInfo(enumType, enumType, container.getName().asString(), true);
        }

        return new FieldInfo(typeMapper.mapType(container), typeMapper.mapType(fieldClassDescriptor), JvmAbi.CLASS_OBJECT_FIELD, true);
    }

    @NotNull
    public static FieldInfo createForHiddenField(@NotNull Type owner, @NotNull Type fieldType, @NotNull String fieldName) {
        return new FieldInfo(owner, fieldType, fieldName, false);
    }

    private final Type ownerType;
    private final Type fieldType;
    private final String fieldName;
    private final boolean isStatic;

    private FieldInfo(@NotNull Type ownerType, @NotNull Type fieldType, @NotNull String fieldName, boolean isStatic) {
        this.ownerType = ownerType;
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.isStatic = isStatic;
    }

    @NotNull
    public Type getFieldType() {
        return fieldType;
    }

    @NotNull
    public Type getOwnerType() {
        return ownerType;
    }

    @NotNull
    public String getOwnerInternalName() {
        return ownerType.getInternalName();
    }

    @NotNull
    public String getFieldName() {
        return fieldName;
    }

    public boolean isStatic() {
        return isStatic;
    }
}

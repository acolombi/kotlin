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

package org.jetbrains.jet.descriptors.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.CallableMemberDescriptor;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;

public abstract class SerializerExtension {
    public static final SerializerExtension DEFAULT = new SerializerExtension() {};

    public boolean hasSupertypes(@NotNull ClassDescriptor descriptor) {
        return true;
    }

    public boolean isNonTrivialEnumEntry(@NotNull ClassDescriptor enumEntry) {
        return false;
    }

    public void serializeCallable(
            @NotNull CallableMemberDescriptor callable,
            @NotNull ProtoBuf.Callable.Builder proto,
            @NotNull NameTable nameTable
    ) {
    }

    public void serializeValueParameter(
            @NotNull ValueParameterDescriptor descriptor,
            @NotNull ProtoBuf.Callable.ValueParameter.Builder proto,
            @NotNull NameTable nameTable
    ) {
    }
}

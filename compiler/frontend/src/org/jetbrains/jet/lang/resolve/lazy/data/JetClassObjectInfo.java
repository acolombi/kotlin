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

package org.jetbrains.jet.lang.resolve.lazy.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.ClassKind;
import org.jetbrains.jet.lang.psi.JetObjectDeclaration;
import org.jetbrains.jet.lang.psi.JetParameter;
import org.jetbrains.jet.lang.psi.JetTypeParameter;

import java.util.Collections;
import java.util.List;

public class JetClassObjectInfo extends JetClassOrObjectInfo<JetObjectDeclaration> {
    protected JetClassObjectInfo(@NotNull JetObjectDeclaration element) {
        super(element);
    }

    @Override
    public JetObjectDeclaration getClassObject() {
        return null;
    }

    @NotNull
    @Override
    public List<JetTypeParameter> getTypeParameters() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<? extends JetParameter> getPrimaryConstructorParameters() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public ClassKind getClassKind() {
        return ClassKind.CLASS_OBJECT;
    }
}

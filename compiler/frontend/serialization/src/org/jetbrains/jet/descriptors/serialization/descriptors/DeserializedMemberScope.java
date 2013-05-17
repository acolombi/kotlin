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

package org.jetbrains.jet.descriptors.serialization.descriptors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.DescriptorDeserializer;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.lazy.storage.MemoizedFunctionToNotNull;
import org.jetbrains.jet.lang.resolve.lazy.storage.MemoizedFunctionToNotNullImpl;
import org.jetbrains.jet.lang.resolve.lazy.storage.NotNullLazyValue;
import org.jetbrains.jet.lang.resolve.lazy.storage.NotNullLazyValueImpl;
import org.jetbrains.jet.lang.resolve.name.LabelName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;

import java.util.*;

public abstract class DeserializedMemberScope implements JetScope {

    private static final Filter<ProtoBuf.Callable.CallableKind> FUNCTION = new Filter<ProtoBuf.Callable.CallableKind>() {
        @Override
        public boolean accept(ProtoBuf.Callable.CallableKind value) {
            return value == ProtoBuf.Callable.CallableKind.FUN;
        }
    };
    private static final Filter<ProtoBuf.Callable.CallableKind> PROPERTY = new Filter<ProtoBuf.Callable.CallableKind>() {
        @Override
        public boolean accept(ProtoBuf.Callable.CallableKind value) {
            return value == ProtoBuf.Callable.CallableKind.VAL || value == ProtoBuf.Callable.CallableKind.VAR;
        }
    };

    private final DeclarationDescriptor containingDeclaration;
    private final DescriptorDeserializer deserializer;
    private final Map<Name, List<ProtoBuf.Callable>> membersProtos;

    private final MemoizedFunctionToNotNull<Name, Collection<FunctionDescriptor>> functions;
    private final MemoizedFunctionToNotNull<Name, Collection<VariableDescriptor>> properties;
    private final NotNullLazyValue<Collection<DeclarationDescriptor>> allDescriptors;
    private final NotNullLazyValue<Collection<ClassDescriptor>> objectDescriptors;

    public DeserializedMemberScope(
            @NotNull DeclarationDescriptor containingDeclaration,
            @NotNull DescriptorDeserializer deserializer,
            @NotNull List<ProtoBuf.Callable> membersList
    ) {
        this.containingDeclaration = containingDeclaration;
        this.deserializer = deserializer;

        this.membersProtos = groupByName(membersList);
        this.functions = new MemoizedFunctionToNotNullImpl<Name, Collection<FunctionDescriptor>>() {
            @NotNull
            @Override
            protected Collection<FunctionDescriptor> doCompute(Name name) {
                return computeFunctions(name);
            }
        };
        this.properties = new MemoizedFunctionToNotNullImpl<Name, Collection<VariableDescriptor>>() {
            @NotNull
            @Override
            protected Collection<VariableDescriptor> doCompute(Name name) {
                return computeProperties(name);
            }
        };
        this.allDescriptors = new NotNullLazyValueImpl<Collection<DeclarationDescriptor>>() {
            @NotNull
            @Override
            protected Collection<DeclarationDescriptor> doCompute() {
                return computeAllDescriptors();
            }
        };
        this.objectDescriptors = new NotNullLazyValueImpl<Collection<ClassDescriptor>>() {
            @NotNull
            @Override
            protected Collection<ClassDescriptor> doCompute() {
                return computeAllObjectDescriptors();
            }
        };
    }

    @NotNull
    private Map<Name, List<ProtoBuf.Callable>> groupByName(@NotNull Collection<ProtoBuf.Callable> membersList) {
        Map<Name, List<ProtoBuf.Callable>> map = new HashMap<Name, List<ProtoBuf.Callable>>();
        for (ProtoBuf.Callable memberProto : membersList) {
            Name name = deserializer.getNameResolver().getName(memberProto.getName());
            List<ProtoBuf.Callable> protos = map.get(name);
            if (protos == null) {
                protos = new ArrayList<ProtoBuf.Callable>(1);
                map.put(name, protos);
            }
            protos.add(memberProto);
        }
        return map;
    }

    @NotNull
    private <D extends CallableMemberDescriptor> Collection<D> computeMembersByName(Name name, Filter<ProtoBuf.Callable.CallableKind> callableKind) {
        List<ProtoBuf.Callable> memberProtos = membersProtos.get(name);

        Collection<D> descriptors = new LinkedHashSet<D>(memberProtos != null ? memberProtos.size() : 0);
        if (memberProtos != null) {
            for (ProtoBuf.Callable memberProto : memberProtos) {
                if (callableKind.accept(Flags.getCallableKind(memberProto.getFlags()))) {
                    //noinspection unchecked
                    descriptors.add((D) deserializer.loadCallable(memberProto));
                }
            }
        }
        return descriptors;
    }

    @NotNull
    private Collection<FunctionDescriptor> computeFunctions(@NotNull Name name) {
        Collection<FunctionDescriptor> descriptors = computeMembersByName(name, FUNCTION);
        computeNonDeclaredFunctions(name, descriptors);
        return descriptors;
    }

    protected void computeNonDeclaredFunctions(@NotNull Name name, @NotNull Collection<FunctionDescriptor> functions) {
    }

    @NotNull
    @Override
    public final Collection<FunctionDescriptor> getFunctions(@NotNull Name name) {
        return functions.fun(name);
    }

    @NotNull
    private Collection<VariableDescriptor> computeProperties(@NotNull Name name) {
        Collection<PropertyDescriptor> descriptors = computeMembersByName(name, PROPERTY);
        computeNonDeclaredProperties(name, descriptors);
        //noinspection unchecked
        return (Collection) descriptors;
    }

    protected void computeNonDeclaredProperties(@NotNull Name name, @NotNull Collection<PropertyDescriptor> descriptors) {
    }

    @NotNull
    @Override
    public Collection<VariableDescriptor> getProperties(@NotNull Name name) {
        return properties.fun(name);
    }

    @Nullable
    @Override
    public final ClassifierDescriptor getClassifier(@NotNull Name name) {
        return getClassDescriptor(name);
    }

    @Nullable
    protected abstract ClassifierDescriptor getClassDescriptor(@NotNull Name name);

    protected abstract void addAllClassDescriptors(@NotNull Collection<DeclarationDescriptor> result);

    @Nullable
    @Override
    public abstract ClassDescriptor getObjectDescriptor(@NotNull Name name);

    @NotNull
    protected abstract Collection<ClassDescriptor> computeAllObjectDescriptors();

    @NotNull
    @Override
    public Collection<ClassDescriptor> getObjectDescriptors() {
        return objectDescriptors.compute();
    }

    @Nullable
    @Override
    public NamespaceDescriptor getNamespace(@NotNull Name name) {
        return null;
    }

    @Nullable
    @Override
    public VariableDescriptor getLocalVariable(@NotNull Name name) {
        return null;
    }

    @NotNull
    @Override
    public DeclarationDescriptor getContainingDeclaration() {
        return containingDeclaration;
    }

    @NotNull
    @Override
    public Collection<DeclarationDescriptor> getDeclarationsByLabel(@NotNull LabelName labelName) {
        throw new UnsupportedOperationException("Should not be called");
    }

    @Nullable
    @Override
    public PropertyDescriptor getPropertyByFieldReference(@NotNull Name fieldName) {
        throw new UnsupportedOperationException("Should not be called");
    }

    private Collection<DeclarationDescriptor> computeAllDescriptors() {
        Collection<DeclarationDescriptor> result = new LinkedHashSet<DeclarationDescriptor>(0);

        for (Name name : membersProtos.keySet()) {
            result.addAll(getFunctions(name));
            result.addAll(getProperties(name));
        }

        addNonDeclaredDescriptors(result);

        addAllClassDescriptors(result);

        return result;
    }

    protected abstract void addNonDeclaredDescriptors(@NotNull Collection<DeclarationDescriptor> result);

    @NotNull
    @Override
    public final Collection<DeclarationDescriptor> getAllDescriptors() {
        return allDescriptors.compute();
    }

    @NotNull
    @Override
    public List<ReceiverParameterDescriptor> getImplicitReceiversHierarchy() {
        throw new UnsupportedOperationException("Should not be called");
    }

    @NotNull
    @Override
    public Collection<DeclarationDescriptor> getOwnDeclaredDescriptors() {
        return getAllDescriptors();
    }

    private interface Filter<T> {
        boolean accept(T value);
    }

}
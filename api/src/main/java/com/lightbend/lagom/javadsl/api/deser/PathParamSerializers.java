/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.lagom.javadsl.api.deser;

import com.lightbend.lagom.internal.api.UnresolvedOptionalPathParamSerializer;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.*;
import java.util.function.Function;

/**
 * Id Serializers.
 */
public final class PathParamSerializers {

    private PathParamSerializers() {}

    /**
     * Create a PathParamSerializer for required parameters.
     */
    public static <Param> PathParamSerializer<Param> required(String name, Function<String, Param> deserialize,
            Function<Param, String> serialize) {
        return new NamedPathParamSerializer<Param>(name) {
            @Override
            public PSequence<String> serialize(Param parameter) {
                return TreePVector.singleton(serialize.apply(parameter));
            }

            @Override
            public Param deserialize(PSequence<String> parameters) {
                if (parameters.isEmpty()) {
                    throw new IllegalArgumentException(name + " parameter is required");
                } else {
                    return deserialize.apply(parameters.get(0));
                }
            }
        };
    }

    /**
     * Create a PathParamSerializer for optional parameters.
     */
    public static <Param> PathParamSerializer<Optional<Param>> optional(String name, Function<String, Param> deserialize,
            Function<Param, String> serialize) {
        return new NamedPathParamSerializer<Optional<Param>>("Optional(" + name + ")") {
            @Override
            public PSequence<String> serialize(Optional<Param> parameter) {
                return parameter.map(p -> TreePVector.singleton(serialize.apply(p))).orElse(TreePVector.empty());
            }

            @Override
            public Optional<Param> deserialize(PSequence<String> parameters) {
                if (parameters.isEmpty()) {
                    return Optional.empty();
                } else {
                    return Optional.of(deserialize.apply(parameters.get(0)));
                }
            }
        };
    }

    /**
     * A String id serializer.
     */
    public static final PathParamSerializer<String> STRING = required("String", Function.identity(),
            Function.identity());

    /**
     * A Long id serializer.
     */
    public static final PathParamSerializer<Long> LONG = required("Long", Long::parseLong, l -> l.toString());

    /**
     * An Integer id serializer.
     */
    public static final PathParamSerializer<Integer> INTEGER = required("Integer", Integer::parseInt, i -> i.toString());

    /**
     * A Boolean id serializer.
     */
    public static final PathParamSerializer<Boolean> BOOLEAN = required("Boolean", Boolean::parseBoolean,
            b -> b.toString());

    /**
     * A generic (unresolved) optional serializer.
     */
    public static final PathParamSerializer<Optional<Object>> OPTIONAL = new UnresolvedOptionalPathParamSerializer<>();

    private static abstract class NamedPathParamSerializer<Id> implements PathParamSerializer<Id> {
        private final String name;

        public NamedPathParamSerializer(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return "PathParamSerializer(" + name + ")";
        }
    }
}
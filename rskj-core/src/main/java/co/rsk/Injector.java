/*
 * This file is part of RskJ
 * Copyright (C) 2022 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Injector {

    private static Injector instance;

    private final RskContext rskContext;

    public static void start(RskContext rskContext) {
        if (rskContext == null) {
            throw new IllegalStateException("RskContext must not be null");
        }

        if (instance != null) {
            throw new IllegalStateException("Already initialised Injector");
        }

        instance = new Injector(rskContext);
    }

    private Injector(RskContext rskContext) {
        this.rskContext = rskContext;
    }

    public static <T> T getService(Class<T> clazz) {
        if (instance == null) {
            throw new IllegalStateException("Injector not yet initialized");
        }

        // TODO add cache to avoid this iteration
        for (Method method : instance.rskContext.getClass().getMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getReturnType().isAssignableFrom(clazz)) {
                try {
                    return (T) method.invoke(instance.rskContext);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException(getErrorMessage(clazz.toString(), "error"), e);
                }
            }
        }

        throw new IllegalStateException(getErrorMessage(clazz.toString(), "not found"));
    }

    private static String getErrorMessage(String className, String extra) {
        String additional = extra != null ? String.format(",%s", extra) : "";
        return String.format("Could not get dependency %s%s", className, additional);
    }

}

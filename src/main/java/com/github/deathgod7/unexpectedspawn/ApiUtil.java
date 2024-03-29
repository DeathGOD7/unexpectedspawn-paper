/*
 * This file is part of UnexpectedSpawn
 * (see https://github.com/DeathGOD7/unexpectedspawn-paper).
 *
 * Copyright (c) 2021 Shivelight.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.deathgod7.unexpectedspawn;

import java.util.HashMap;

public class ApiUtil {

    private static final HashMap<String, Boolean> availableApiCache = new HashMap<>();

    public static boolean isAvailable(Class<?> klass, String methodName) {
        String method = klass.getName() + "#" + methodName;
        if (availableApiCache.containsKey(method)) {
            return availableApiCache.get(method);
        }

        try {
            klass.getMethod(methodName);
            availableApiCache.put(method, true);
        } catch (NoSuchMethodException e) {
            availableApiCache.put(method, false);
        }

        return availableApiCache.get(method);
    }

    public static void clearCache() {
        availableApiCache.clear();
    }

}

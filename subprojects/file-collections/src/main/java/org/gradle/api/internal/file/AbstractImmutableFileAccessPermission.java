/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.file;

import org.gradle.api.file.FileAccessPermissions;
import org.gradle.api.file.ImmutableFileAccessPermission;
import org.gradle.api.internal.lambdas.SerializableLambdas;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.provider.Provider;

public abstract class AbstractImmutableFileAccessPermission implements ImmutableFileAccessPermission {

    /**
     * Converts the user permission to a numeric Unix permission.
     * See {@link FileAccessPermissions#unix(String)} for details,
     * returned value is equivalent to one of the three octal digits.
     */
    protected Provider<Integer> toUnixNumeric() {
        if (hasTaskDependencies()) {
            return getRead().map(SerializableLambdas.transformer(r -> r ? 4 : 0))
                .zip(getWrite().map(SerializableLambdas.transformer(w -> w ? 2 : 0)), SerializableLambdas.combiner(Integer::sum))
                .zip(getExecute().map(SerializableLambdas.transformer(x -> x ? 1 : 0)), SerializableLambdas.combiner(Integer::sum));
        } else {
            int unixNumeric = (getRead().get() ? 4 : 0) + (getWrite().get() ? 2 : 0) + (getExecute().get() ? 1 : 0);
            return Providers.of(unixNumeric);
        }
    }

    protected abstract boolean hasTaskDependencies();

    protected static boolean isRead(int unixNumeric) {
        return (unixNumeric & 4) >> 2 == 1;
    }

    protected static boolean isRead(String unixSymbolic) {
        char symbol = unixSymbolic.charAt(0);
        if (symbol == 'r') {
            return true;
        } else if (symbol == '-') {
            return false;
        } else {
            throw new IllegalArgumentException("'" + symbol + "' is not a valid Unix permission READ flag, must be 'r' or '-'.");
        }
    }

    protected static boolean isWrite(int unixNumeric) {
        return (unixNumeric & 2) >> 1 == 1;
    }

    protected static boolean isWrite(String unixSymbolic) {
        char symbol = unixSymbolic.charAt(1);
        if (symbol == 'w') {
            return true;
        } else if (symbol == '-') {
            return false;
        } else {
            throw new IllegalArgumentException("'" + symbol + "' is not a valid Unix permission WRITE flag, must be 'w' or '-'.");
        }
    }

    protected static boolean isExecute(int unixNumeric) {
        return (unixNumeric & 1) == 1;
    }

    protected static boolean isExecute(String unixSymbolic) {
        char symbol = unixSymbolic.charAt(2);
        if (symbol == 'x') {
            return true;
        } else if (symbol == '-') {
            return false;
        } else {
            throw new IllegalArgumentException("'" + symbol + "' is not a valid Unix permission EXECUTE flag, must be 'x' or '-'.");
        }
    }

}

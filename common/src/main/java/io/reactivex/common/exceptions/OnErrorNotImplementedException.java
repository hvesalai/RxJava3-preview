/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.common.exceptions;

import io.reactivex.common.annotations.*;

/**
 * Represents an exception used to signal to the {@code RxJavaCommonPlugins.onError()} that a
 * callback-based subscribe() method on a base reactive type didn't specify
 * an onError handler.
 * @since 2.0.6 - experimental
 */
@Experimental
public final class OnErrorNotImplementedException extends RuntimeException {

    private static final long serialVersionUID = -6298857009889503852L;

    /**
     * Customizes the {@code Throwable} with a custom message and wraps it before it
     * is signalled to the {@code RxJavaCommonPlugins.onError()} handler as {@code OnErrorNotImplementedException}.
     *
     * @param message
     *          the message to assign to the {@code Throwable} to signal
     * @param e
     *          the {@code Throwable} to signal; if null, a NullPointerException is constructed
     */
    public OnErrorNotImplementedException(String message, @NonNull Throwable e) {
        super(message, e != null ? e : new NullPointerException());
    }

    /**
     * Wraps the {@code Throwable} before it
     * is signalled to the {@code RxJavaCommonPlugins.onError()}
     * handler as {@code OnErrorNotImplementedException}.
     *
     * @param e
     *          the {@code Throwable} to signal; if null, a NullPointerException is constructed
     */
    public OnErrorNotImplementedException(@NonNull Throwable e) {
        super(e != null ? e.getMessage() : null, e != null ? e : new NullPointerException());
    }
}
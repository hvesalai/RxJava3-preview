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

package io.reactivex.observable.internal.utils;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.ProtocolViolationException;
import io.reactivex.common.internal.disposables.DisposableHelper;
import io.reactivex.common.internal.functions.ObjectHelper;

/**
 * Utility class to help report multiple subscriptions with the same
 * consumer type instead of the internal "Disposable already set!" message
 * that is practically reserved for internal operators and indicate bugs in them.
 */
public final class EndObserverHelper {

    /**
     * Utility class.
     */
    private EndObserverHelper() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Ensures that the upstream Disposable is null and returns true, otherwise
     * disposes the next Disposable and if the upstream is not the shared
     * disposed instance, reports a ProtocolViolationException due to
     * multiple subscribe attempts.
     * @param upstream the upstream current value
     * @param next the Disposable to check for nullness and dispose if necessary
     * @param observer the class of the consumer to have a personalized
     * error message if the upstream already contains a non-cancelled Disposable.
     * @return true if successful, false if the upstream was non null
     */
    public static boolean validate(Disposable upstream, Disposable next, Class<?> observer) {
        ObjectHelper.requireNonNull(next, "next is null");
        if (upstream != null) {
            next.dispose();
            if (upstream != DisposableHelper.DISPOSED) {
                reportDoubleSubscription(observer);
            }
            return false;
        }
        return true;
    }

    /**
     * Atomically updates the target upstream AtomicReference from null to the non-null
     * next Disposable, otherwise disposes next and reports a ProtocolViolationException
     * if the AtomicReference doesn't contain the shared disposed indicator.
     * @param upstream the target AtomicReference to update
     * @param next the Disposable to set on it atomically
     * @param observer the class of the consumer to have a personalized
     * error message if the upstream already contains a non-cancelled Disposable.
     * @return true if successful, false if the content of the AtomicReference was non null
     */
    public static boolean setOnce(AtomicReference<Disposable> upstream, Disposable next, Class<?> observer) {
        ObjectHelper.requireNonNull(next, "next is null");
        if (!upstream.compareAndSet(null, next)) {
            next.dispose();
            if (upstream.get() != DisposableHelper.DISPOSED) {
                reportDoubleSubscription(observer);
            }
            return false;
        }
        return true;
    }

    /**
     * Builds the error message with the consumer class.
     * @param consumer the class of the consumer
     * @return the error message string
     */
    public static String composeMessage(String consumer) {
        return "It is not allowed to subscribe with a(n) " + consumer + " multiple times. "
                + "Please create a fresh instance of " + consumer + " and subscribe that to the target source instead.";
    }

    /**
     * Report a ProtocolViolationException with a personalized message referencing
     * the simple type name of the consumer class and report it via
     * RxJavaPlugins.onError.
     * @param consumer the class of the consumer
     */
    public static void reportDoubleSubscription(Class<?> consumer) {
        RxJavaCommonPlugins.onError(new ProtocolViolationException(composeMessage(consumer.getName())));
    }
}
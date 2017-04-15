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

package io.reactivex.flowable.internal.operators;

import java.util.concurrent.Callable;

import org.reactivestreams.Subscriber;

import io.reactivex.common.functions.BiFunction;
import io.reactivex.flowable.Flowable;

public final class FlowableReduceWith<T, R> extends AbstractFlowableWithUpstream<T, R> {

    final Callable<R> initialValue;
    
    final BiFunction<R, ? super T, R> reducer;
    
    public FlowableReduceWith(Flowable<T> source, Callable<R> initialValue, BiFunction<R, ? super T, R> reducer) {
        super(source);
        this.initialValue = initialValue;
        this.reducer = reducer;
    }

    @Override
    protected void subscribeActual(Subscriber<? super R> s) {
        // TODO Auto-generated method stub
        
    }

}

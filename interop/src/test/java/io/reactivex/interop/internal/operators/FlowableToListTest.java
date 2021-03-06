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

package io.reactivex.interop.internal.operators;

import static io.reactivex.interop.RxJava3Interop.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.mockito.Mockito;

import io.reactivex.common.*;
import io.reactivex.common.exceptions.TestException;
import io.reactivex.flowable.Flowable;
import io.reactivex.flowable.processors.PublishProcessor;
import io.reactivex.interop.TestHelper;
import io.reactivex.observable.*;
import io.reactivex.observable.observers.TestObserver;

public class FlowableToListTest {

    @Test
    public void testList() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> observable = toList(w);

        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void testListViaFlowable() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> observable = toList(w);

        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", "two", "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void testListMultipleSubscribers() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        Single<List<String>> observable = toList(w);

        SingleObserver<List<String>> o1 = TestHelper.mockSingleObserver();
        observable.subscribe(o1);

        SingleObserver<List<String>> o2 = TestHelper.mockSingleObserver();
        observable.subscribe(o2);

        List<String> expected = Arrays.asList("one", "two", "three");

        verify(o1, times(1)).onSuccess(expected);
        verify(o1, Mockito.never()).onError(any(Throwable.class));

        verify(o2, times(1)).onSuccess(expected);
        verify(o2, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    @Ignore("Null values are not allowed")
    public void testListWithNullValue() {
        Flowable<String> w = Flowable.fromIterable(Arrays.asList("one", null, "three"));
        Single<List<String>> observable = toList(w);

        SingleObserver<List<String>> observer = TestHelper.mockSingleObserver();
        observable.subscribe(observer);
        verify(observer, times(1)).onSuccess(Arrays.asList("one", null, "three"));
        verify(observer, Mockito.never()).onError(any(Throwable.class));
    }

    @Test
    public void testListWithBlockingFirst() {
        Flowable<String> o = Flowable.fromIterable(Arrays.asList("one", "two", "three"));
        List<String> actual = toList(o).blockingGet();
        Assert.assertEquals(Arrays.asList("one", "two", "three"), actual);
    }
    @Test
    @Ignore("Single doesn't do backpressure")
    public void testBackpressureHonored() {
        Single<List<Integer>> w = toList(Flowable.just(1, 2, 3, 4, 5));
        TestObserver<List<Integer>> ts = new TestObserver<List<Integer>>();

        w.subscribe(ts);

        ts.assertNoValues();
        ts.assertNoErrors();
        ts.assertNotComplete();

//        ts.request(1);

        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();

//        ts.request(1);

        ts.assertValue(Arrays.asList(1, 2, 3, 4, 5));
        ts.assertNoErrors();
        ts.assertComplete();
    }
    @Test(timeout = 2000)
    @Ignore("PublishProcessor no longer emits without requests so this test fails due to the race of onComplete and request")
    public void testAsyncRequested() {
        Scheduler.Worker w = Schedulers.newThread().createWorker();
        try {
            for (int i = 0; i < 1000; i++) {
                if (i % 50 == 0) {
                    System.out.println("testAsyncRequested -> " + i);
                }
                PublishProcessor<Integer> source = PublishProcessor.create();
                Single<List<Integer>> sorted = toList(source);

                final CyclicBarrier cb = new CyclicBarrier(2);
                final TestObserver<List<Integer>> ts = new TestObserver<List<Integer>>();
                sorted.subscribe(ts);

                w.schedule(new Runnable() {
                    @Override
                    public void run() {
                        await(cb);
//                        ts.request(1);
                    }
                });
                source.onNext(1);
                await(cb);
                source.onComplete();
                ts.awaitTerminalEvent(1, TimeUnit.SECONDS);
                ts.assertTerminated();
                ts.assertNoErrors();
                ts.assertValue(Arrays.asList(1));
            }
        } finally {
            w.dispose();
        }
    }
    static void await(CyclicBarrier cb) {
        try {
            cb.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (BrokenBarrierException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void capacityHint() {
        Flowable.range(1, 10)
        .toList(4)
        .test()
        .assertResult(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void dispose() {
        TestHelper.checkDisposed(toList(Flowable.just(1)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorSingle() {
        toList(Flowable.error(new TestException())
        )
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void collectionSupplierThrows() {
        toList(Flowable.just(1),
        new Callable<Collection<Integer>>() {
            @Override
            public Collection<Integer> call() throws Exception {
                throw new TestException();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void collectionSupplierReturnsNull() {
        toList(Flowable.just(1)
        , new Callable<Collection<Integer>>() {
            @Override
            public Collection<Integer> call() throws Exception {
                return null;
            }
        })
        .test()
        .assertFailure(NullPointerException.class)
        .assertErrorMessage("The collectionSupplier returned a null collection. Null values are generally not allowed in 2.x operators and sources.");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void singleCollectionSupplierThrows() {
        toList(Flowable.just(1)
        , new Callable<Collection<Integer>>() {
            @Override
            public Collection<Integer> call() throws Exception {
                throw new TestException();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void singleCollectionSupplierReturnsNull() {
        toList(Flowable.just(1)
        , new Callable<Collection<Integer>>() {
            @Override
            public Collection<Integer> call() throws Exception {
                return null;
            }
        })
        .test()
        .assertFailure(NullPointerException.class)
        .assertErrorMessage("The collectionSupplier returned a null collection. Null values are generally not allowed in 2.x operators and sources.");
    }

    @Test
    public void onNextCancelRace() {
        for (int i = 0; i < 1000; i++) {
            final PublishProcessor<Integer> pp = PublishProcessor.create();
            final TestObserver<List<Integer>> ts = toList(pp).test();

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    pp.onNext(1);
                }
            };
            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    ts.cancel();
                }
            };

            TestHelper.race(r1, r2);
        }
    }
}

package com.crud.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class WorkersTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Workers getWorkersSample1() {
        return new Workers().id(1L).name("name1").age(1);
    }

    public static Workers getWorkersSample2() {
        return new Workers().id(2L).name("name2").age(2);
    }

    public static Workers getWorkersRandomSampleGenerator() {
        return new Workers().id(longCount.incrementAndGet()).name(UUID.randomUUID().toString()).age(intCount.incrementAndGet());
    }
}

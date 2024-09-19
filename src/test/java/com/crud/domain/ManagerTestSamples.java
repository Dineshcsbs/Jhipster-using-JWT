package com.crud.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ManagerTestSamples {

    private static final Random random = new Random();
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Manager getManagerSample1() {
        return new Manager().id(UUID.fromString("23d8dc04-a48b-45d9-a01d-4b728f0ad4aa")).name("name1").age(1).gender("gender1");
    }

    public static Manager getManagerSample2() {
        return new Manager().id(UUID.fromString("ad79f240-3727-46c3-b89f-2cf6ebd74367")).name("name2").age(2).gender("gender2");
    }

    public static Manager getManagerRandomSampleGenerator() {
        return new Manager()
            .id(UUID.randomUUID())
            .name(UUID.randomUUID().toString())
            .age(intCount.incrementAndGet())
            .gender(UUID.randomUUID().toString());
    }
}

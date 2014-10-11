package com.google.glassware;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ejf3 on 10/5/14.
 */
public class FakeDatabase {
    private static final ConcurrentHashMap<String, HeartyData> heartyData = new ConcurrentHashMap<String, HeartyData>();

    private static Random rand = new Random(System.currentTimeMillis());

    public static void generateFakeUserData(String userId) {
        HeartyData data = new HeartyData();
        data.activeMinutes = rand.nextInt(110);
        data.stepCount = rand.nextInt(25000);
        data.heartRate = rand.nextInt(50) + 50;
        heartyData.put(userId, data);
    }

    public static HeartyData getUserData(String userId) {
        return heartyData.get(userId);
    }
}

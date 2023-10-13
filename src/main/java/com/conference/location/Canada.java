package com.conference.location;

import java.util.Map;

public class Canada {

    @LocationInfoStreamer
    public void sendLocationInformation(String id) {

    }

    @LocationInfoStreamer
    public void sendLocationInformation(String id, Map<Object, String> eventHistory) {

    }

    @LocationInfoStreamer
    public void sendLocationInformation(String id, Map<Object, String> eventHistory, int contractNumber) {

    }

    // Remove the comments below to see the tests failing.
    // The test checks if methods annotated with @LocationInfoStreamer fail if the
    // method does not contain between 1 and 3 arguments

//    @LocationInfoStreamer
//    public void sendLocationInformation() {
//
//    }

//    @LocationInfoStreamer
//    public void sendLocationInformation(String id, Map<Object, String> eventHistory, int contractNumber, boolean agreementNeeded) {
//
//    }
}

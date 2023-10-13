package com.conference.presentations;

public class InternationalPresentation implements Presentation {

    public boolean isScheduled() throws Exception {
        // uncomment line below to break the test that checks for generic exceptions being thrown
        //  new Exception("Throwing an undesired, generic exception");
        return false;
    }
}

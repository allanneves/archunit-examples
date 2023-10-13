package com.conference.logging;

public class InboundAccessLog {

    // This method breaks code design rules we defined.
    // However, our ArchUnit tests are excluding everything
    // that is under the "logging" package.
    public void ruleBreakerMethod() throws Exception {
        System.out.println("I'm breaking a Code Design Rule");
        System.err.println("I'm breaking a Code Design Rule");
        throw new Exception("Throwing an undesired exception");
    }
}

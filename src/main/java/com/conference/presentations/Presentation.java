package com.conference.presentations;

// Rename this interface to "PresentationInterface" to break the test that checks
// that interfaces should not have "Interface" in their names
public interface Presentation {
    // If you add a new implementation to this interface, another test will break too.
    // This is because the test expects this interface to have only two implementations
}

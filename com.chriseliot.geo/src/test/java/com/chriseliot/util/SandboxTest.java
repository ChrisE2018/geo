
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SandboxTest
{
    @ParameterizedTest
    @ValueSource (ints =
    {1, 3, 5, -3, 15, Integer.MAX_VALUE})
    void isOdd_ShouldReturnTrueForOddNumbers (int number)
    {
        assertTrue (isOdd (number));
    }

    private boolean isOdd (int number)
    {
        return (number & 1) != 0;
    }
}

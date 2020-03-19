package com.babysitter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PaymentCalculatorTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    public void whenCalculateIsCalledTotalPayIsZero() throws Exception {
        PaymentCalculator paymentCalculator = new PaymentCalculator();
        Assert.assertEquals(0, paymentCalculator.calculate("18:00", "19:00", "A"), 0.01);
    }

    @Test
    public void whenCalculateIsCalledWithEmptyParametersAnExceptionIsThrown() throws Exception {
        PaymentCalculator paymentCalculator = new PaymentCalculator();
        paymentCalculator.calculate("", "19:00", "A");
        Assert.assertEquals("Start time cannot be blank!", outputStream.toString().trim());
    }
}

/*
 * The MIT License
 *
 * Copyright 2020 dmh.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.dhahaj;

/**

 @author dmh
 */
public interface DevicePins {

    /**
     The Battery enable pin.
     */
    public static final int BATT_PIN = 11;

    /**
     The low battery enable pin.
     */
    public static final int LOW_BATT_PIN = 2;

    /**
     The indicator pin.
     */
    public static final int INDICATOR_PIN = 13;

    public static final int TESTING_INDICATOR_PIN = 5;

    /**
     An array of all the PINS.
     */
    public static int[] PINS = {BATT_PIN, LOW_BATT_PIN, INDICATOR_PIN, TESTING_INDICATOR_PIN};

    /**
     An array containing the names of the pins.
     */
    public static String[] NAMES = {"Battery", "Low Battery", "Indicator", "Test Indicator"};

}

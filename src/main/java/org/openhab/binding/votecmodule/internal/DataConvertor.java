package org.openhab.binding.votecmodule.internal;

import java.util.ArrayList;
import java.util.Arrays;

public class DataConvertor {

    static public byte[] toByteArray(String input) {
        if (input.length() < 1) {
            return null;
        }
        char[] charArray = input.toCharArray();
        byte[] byteArray = new byte[input.length()];
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte) charArray[i];
        }
        return byteArray;
    }

    static public String toStringofInt(String input) {
        if (input.length() < 1) {
            return null;
        }
        return Arrays.toString(input.getBytes());
    }

    static public String arrayToString(ArrayList<Integer> data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int integer : data) {
            stringBuilder.append((char) integer);
        }

        return stringBuilder.toString();
    }

    /**
     * @param stringIntArray ArrayList<Integer>.toString();
     * @return
     */
    static public byte[] stringIntArrayToByteArray(String stringIntAString) {

        String tempString = stringIntAString.substring(1, stringIntAString.length() - 1);
        tempString = tempString.replace(" ", "");
        String[] byteStrings = tempString.split(",");
        byte[] newByteArr = new byte[byteStrings.length];

        for (int i = 0; i < byteStrings.length; i++) {
            newByteArr[i] = (byte) Integer.parseInt(byteStrings[i]);
        }

        return newByteArr;
    }

}

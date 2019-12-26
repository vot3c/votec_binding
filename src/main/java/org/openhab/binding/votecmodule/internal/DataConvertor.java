package org.openhab.binding.votecmodule.internal;

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

}

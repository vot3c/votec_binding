package org.openhab.binding.votecmodule.model;

/*
 *
 */

public class VotecCommand {
    byte[] command;
    byte[] data;
    byte[] packet;
    byte[] header = new byte[2];

    public VotecCommand() {
        command = new byte[4];
        data = new byte[8];
        packet = new byte[14];
        header[0] = 0x56;
        header[1] = 0x2A;
    }

    public void buildPacket() {
        for (int i = 0; i < 14; i++) {
            if (i < 2) {
                packet[i] = header[i];
            }
            if (i >= 2 && i < 6) {
                packet[i] = command[i - 2];
            } else if (i >= 6) {
                packet[i] = data[i - 6];
            }
        }
    }

    public VotecCommand(byte[] mPacket) {
        this.packet = mPacket;
    }

    public boolean setData(byte[] data) {
        if (data.length == 8) {
            this.data = data;
            return true;
        }
        return false;
    }

    public byte[] getPacket() {
        buildPacket();
        if (this.packet != null) {
            return this.packet;
        }
        return new byte[0];
    }

    public byte[] getData() {
        if (this.data != null) {
            return this.data;
        }
        return new byte[0];
    }

    public byte[] getCommand() {
        if (this.command != null) {
            return this.command;
        }
        return new byte[0];
    }

    public boolean setOta(int ota) {
        if (ota > 3) {
            return false;
        }
        command[3] = (byte) ota;
        return true;
    }

    public int getOta() {
        int ota = 0;
        ota = command[3] & 3;
        return ota;
    }

    public boolean setBroadcast(int broadcast) {

        if (broadcast > 7) {
            return false;
        }
        broadcast = (byte) (broadcast << 2);
        broadcast = broadcast | command[3];
        command[3] = (byte) broadcast;
        return true;
    }

    public int getBroadcast() {
        int broadcast = 0;
        broadcast = (command[3] & 0x1C) >> 2;
        return broadcast;
    }

    public boolean setGroupId(int groupId) {
        if (groupId > 31) {
            return false;
        }
        int groupIdUpperBits = groupId >> 3;

        groupId = groupId << 5 & 0xFF | command[3];
        command[3] = (byte) groupId;
        command[2] = (byte) groupIdUpperBits;

        return true;
    }

    public int getGroupId() {
        int groupId = 0;
        groupId = (command[3] & 0xFE) >> 5;
        groupId = groupId | ((command[2] & 3) << 3);
        return groupId;
    }

    public boolean setSubGroupId(int sGroupId) {
        if (sGroupId > 31) {
            return false;
        }
        sGroupId = (sGroupId << 2) | command[2];
        command[2] = (byte) sGroupId;
        return true;
    }

    public int getSubGroupId() {
        int subGroupId = 0;
        subGroupId = (command[2] & 0x7C) >> 2;
        return subGroupId;
    }

    public boolean setDeviceId(int deviceId) {
        if (deviceId > 127) {
            return false;
        }
        int deviceIdUpperBits = deviceId >> 1;

        deviceId = ((deviceId & 1) << 7) | command[2];
        command[2] = (byte) deviceId;
        command[1] = (byte) deviceIdUpperBits;
        return true;
    }

    public int getDeviceId() {
        int deviceId = 0;
        deviceId = ((command[2] & 0x80) >> 7) | ((command[1] & 0x3F) << 1);
        return deviceId;
    }

    public boolean setAtomicId(int atomicId) {
        if (atomicId > 0x7F) {
            return false;
        }

        int atomicIdUpperBits = atomicId;
        atomicIdUpperBits = (atomicIdUpperBits & 0xFC) >> 2;

        atomicId = ((atomicId & 3) << 6) | command[1];
        command[1] = (byte) atomicId;
        command[0] = (byte) atomicIdUpperBits;

        return true;
    }

    public int getAtomicId() {
        int atomicId = 0;

        atomicId = ((command[1] & 0xC0) >> 6) | ((command[0] & 0x1F) << 2);

        return atomicId;
    }

    @Override
    public String toString() {
        String toString = "OTA: " + getOta() + ", BRO: " + getBroadcast() + ", GID: " + getGroupId() + ", SGI: "
                + getSubGroupId() + ", DID: " + getDeviceId() + ", AID: " + getAtomicId();
        return toString;
    }

}

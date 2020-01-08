package org.openhab.binding.votecmodule.model;

import org.openhab.binding.votecmodule.handler.VotecSerialHandler;

public class VotecCommand {
    byte[] packet;

    public VotecCommand() {
        packet = new byte[4];
        if (setOta(3) && setBroadcast(7) && setGroupId(31) && setSubGroupId(31) && setDeviceId(127)
                && setAtomicId(125)) {
            VotecSerialHandler.sendPackage(packet);
            System.out.println(toString());
        }
    }

    public VotecCommand(byte[] mPacket) {
        this.packet = mPacket;
    }

    public boolean setOta(int ota) {
        if (ota > 3) {
            return false;
        }
        packet[3] = (byte) ota;
        return true;
    }

    public int getOta() {
        int ota = 0;
        ota = packet[3] & 3;
        return ota;
    }

    public boolean setBroadcast(int broadcast) {

        if (broadcast > 7) {
            return false;
        }
        broadcast = (byte) (broadcast << 2);
        broadcast = broadcast | packet[3];
        packet[3] = (byte) broadcast;
        return true;
    }

    public int getBroadcast() {
        int broadcast = 0;
        broadcast = (packet[3] & 28) >> 2;
        return broadcast;
    }

    public boolean setGroupId(int groupId) {
        if (groupId > 31) {
            return false;
        }
        int groupIdUpperBits = groupId >> 3;

        groupId = groupId << 5 & 255 | packet[3];
        packet[3] = (byte) groupId;
        packet[2] = (byte) groupIdUpperBits;

        return true;
    }

    public int getGroupId() {
        int groupId = 0;
        groupId = (packet[3] & 224) >> 5;
        groupId = groupId | ((packet[2] & 3) << 3);
        return groupId;
    }

    public boolean setSubGroupId(int sGroupId) {
        if (sGroupId > 31) {
            return false;
        }
        sGroupId = (sGroupId << 2) | packet[2];
        packet[2] = (byte) sGroupId;
        return true;
    }

    public int getSubGroupId() {
        int subGroupId = 0;
        subGroupId = (packet[2] & 124) >> 2;
        return subGroupId;
    }

    public boolean setDeviceId(int deviceId) {
        if (deviceId > 127) {
            return false;
        }
        int deviceIdUpperBits = deviceId >> 1;

        deviceId = ((deviceId & 1) << 7) | packet[2];
        packet[2] = (byte) deviceId;
        packet[1] = (byte) deviceIdUpperBits;
        return true;
    }

    public int getDeviceId() {
        int deviceId = 0;
        deviceId = ((packet[2] & 128) >> 7) | ((packet[1] & 63) << 1);
        return deviceId;
    }

    public boolean setAtomicId(int atomicId) {
        if (atomicId > 127) {
            return false;
        }

        int atomicIdUpperBits = atomicId;
        atomicIdUpperBits = (atomicIdUpperBits & 252) >> 2;

        atomicId = ((atomicId & 3) << 6) | packet[1];
        packet[1] = (byte) atomicId;
        packet[0] = (byte) atomicIdUpperBits;

        return true;
    }

    public int getAtomicId() {
        int atomicId = 0;

        atomicId = ((packet[1] & 192) >> 6) | ((packet[0] & 31) << 2);

        return atomicId;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String toString = "OTA: " + getOta() + ", BRO: " + getBroadcast() + ", GID: " + getGroupId() + ", SGI: "
                + getSubGroupId() + ", DID: " + getDeviceId() + ", AID: " + getAtomicId();
        return toString;
    }

}

package com.binderror.mqtt;

import com.binderror.mqtt.common.ServerException;
import com.binderror.mqtt.device.SmartDevice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class DeviceEmulator {

    public static void main(String[] args) throws ServerException {
        Config config = ConfigFactory.load();
        int deviceCount = config.getInt("emulator.device.count");
        for (int i = 1; i <= deviceCount; i++) {
            Thread device = new Thread(new SmartDevice("device-" + i, config.getString("emulator.device.password"), config));
            device.start();
        }
    }
}

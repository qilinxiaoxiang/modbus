package com.xiangzheng.modbus.main;

import com.github.zengfr.easymodbus4j.client.ModbusClient;
import com.github.zengfr.easymodbus4j.processor.ModbusMasterResponseProcessor;
import com.github.zengfr.easymodbus4j.processor.ModbusSlaveRequestProcessor;
import com.xiangzheng.modbus.entity.ConnectionProperty;
import com.xiangzheng.modbus.processor.MainModbusClientRequestProcessor;
import com.xiangzheng.modbus.processor.MainModbusMasterResponseProcessor;

public class ClientContainer {
    private static ModbusClient instance;

    public static ModbusClient getModbusClient(ConnectionProperty connectionProperty) {
        if (instance == null) {
            synchronized (ClientContainer.class) {
                if (instance == null) {
                    short transactionIdentifierOffset = connectionProperty.getTransactionIdentifierOffset();
                    int port = connectionProperty.getPort();
                    String host = connectionProperty.getHost();
                    int prepareTime = connectionProperty.getPrepareTime();
                    ModbusMasterResponseProcessor masterProcessor = new MainModbusMasterResponseProcessor(transactionIdentifierOffset);
                    ModbusSlaveRequestProcessor slaveProcessor = new MainModbusClientRequestProcessor(transactionIdentifierOffset);
                    ModbusSetup setup = new ModbusSetup();
                    try {
                        setup.initProperties();
                        setup.initHandler(masterProcessor, slaveProcessor);
                        setup.setupClient4TcpSlave(host, port);
                        setup.setupClient4TcpMaster(host, port);
                        ModbusClient modbusClient = setup.getModbusClient();
                        Thread.sleep(prepareTime);
                        instance = modbusClient;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return instance;
    }

}

package com.xiangzheng.modbus.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public class ConnectionProperty {
    private static final Integer DEFAULT_PORT = 502;
    private static final Short DEFAULT_TRANSACTION_IDENTIFIER_OFFSET = 0;
    private static final Integer DEFAULT_PREPARE_TIME = 3;

    /**
     * server的地址
     */
    @NotBlank
    private String host;

    /**
     * server的端口
     */
    @NotNull
    private Integer port;

    /**
     * 偏移量
     */
    @NotNull
    private Short transactionIdentifierOffset;

    /**
     * 建联等待时间, 单位秒
     */
    @NotNull
    private Integer prepareTime;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = Optional.of(port).orElse(DEFAULT_PORT);
    }

    public Short getTransactionIdentifierOffset() {
        return transactionIdentifierOffset;
    }

    public void setTransactionIdentifierOffset(Short transactionIdentifierOffset) {
        this.transactionIdentifierOffset = Optional.of(transactionIdentifierOffset).orElse(DEFAULT_TRANSACTION_IDENTIFIER_OFFSET);
    }

    public Integer getPrepareTime() {
        return prepareTime;
    }

    public void setPrepareTime(Integer prepareTime) {
        this.prepareTime = Optional.of(prepareTime).orElse(DEFAULT_PREPARE_TIME);
    }

    public ConnectionProperty() {
        this.port = DEFAULT_PORT;
        this.transactionIdentifierOffset = DEFAULT_TRANSACTION_IDENTIFIER_OFFSET;
        this.prepareTime = DEFAULT_PREPARE_TIME;
    }

    @Override
    public String toString() {
        return "ConnectionProperty{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", transactionIdentifierOffset=" + transactionIdentifierOffset +
                ", prepareTime=" + prepareTime +
                '}';
    }
}

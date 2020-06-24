package com.xiangzheng.modbus.entity;

public class PressureInfo {
    private String code;
    private String pressure;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public PressureInfo(String code, String pressure) {
        this.code = code;
        this.pressure = pressure;
    }

    @Override
    public String toString() {
        return "PressureInfo{" +
                "code='" + code + '\'' +
                ", pressure='" + pressure + '\'' +
                '}';
    }
}

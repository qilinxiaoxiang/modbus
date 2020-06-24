package com.xiangzheng.modbus.controller;

import com.xiangzheng.modbus.entity.ConnectionProperty;
import com.xiangzheng.modbus.entity.PressureInfo;
import com.xiangzheng.modbus.entity.Result;
import com.xiangzheng.modbus.main.ClientContainer;
import com.xiangzheng.modbus.service.ModbusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

@Controller
public class ModbusController {
    @Autowired
    private ModbusService modbusService;
    /**
     * 获取连接信息
     * @return 连接信息
     */
    @ResponseBody
    @GetMapping("/getConnectionProperty")
    public Result<ConnectionProperty> getConnectionProperty() {
        return Result.successResult(modbusService.getConnectionProperty());
    }

    /**
     * 建立连接
     */
    @ResponseBody
    @PostMapping("/connect")
    public Result<String> connect(@RequestBody @Valid ConnectionProperty connectionProperty) {
        modbusService.saveConnectionProperty(connectionProperty);
        ClientContainer.getModbusClient(modbusService.getConnectionProperty());
        return Result.successResult();
    }

    /**
     * 获取压力值
     * @return 压力值
     */
    @ResponseBody
    @GetMapping("/getPressureInfoList")
    public Result<List<PressureInfo>> getPressureInfoList() {
        return Result.successResult(modbusService.getPressureInfoList());
    }

    /**
     * 输出excel表格
     */
    @GetMapping("/getExcel")
    public void getExcel(HttpServletResponse response) {
        modbusService.exportExcel(modbusService.getPressureInfoList(), response);
    }
}

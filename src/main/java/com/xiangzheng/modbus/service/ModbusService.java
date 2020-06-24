package com.xiangzheng.modbus.service;

import com.github.zengfr.easymodbus4j.client.ModbusClient;
import com.github.zengfr.easymodbus4j.func.response.ReadHoldingRegistersResponse;
import com.github.zengfr.easymodbus4j.sender.ChannelSender;
import com.github.zengfr.easymodbus4j.sender.ChannelSenderFactory;
import com.xiangzheng.modbus.entity.ConnectionProperty;
import com.xiangzheng.modbus.entity.PressureInfo;
import com.xiangzheng.modbus.main.ClientContainer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class ModbusService {
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String TRANSACTION_IDENTIFIER_OFFSET = "transactionIdentifierOffset";
    public static final String PREPARE_TIME = "prepareTime";
    public static final String SEPARATOR = "=";
    public static final String CONFIG_PATH = "connectionProperty.txt";
    public static final String EXCEL_PATH = "data.xlsx";

    /**
     * 获取连接信息
     * @return 连接信息
     */
    public ConnectionProperty getConnectionProperty() {
        ConnectionProperty connectionProperty = new ConnectionProperty();
        // 判断配置文件是否存在
        Path path = Paths.get(CONFIG_PATH);
        if (!path.toFile().exists()) {
            return connectionProperty;
        }
        // 存在则读取属性
        try(Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            stream.forEach(line -> {
                String[] split = line.split(SEPARATOR);
                String property = StringUtils.trim(split[0]);
                String value = StringUtils.trim(split[1]);
                switch (property) {
                    case HOST: connectionProperty.setHost(value);
                    break;
                    case PORT: connectionProperty.setPort(Integer.valueOf(value));
                    break;
                    case TRANSACTION_IDENTIFIER_OFFSET: connectionProperty.setTransactionIdentifierOffset(Short.valueOf(value));
                    break;
                    case PREPARE_TIME: connectionProperty.setPrepareTime(Integer.valueOf(value));
                    break;
                    default:
                    break;
                }
            });
            return connectionProperty;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 保存连接信息
     */
    public void saveConnectionProperty(ConnectionProperty connectionProperty) {
        if (connectionProperty == null) {
            return;
        }
        List<String> config = new ArrayList<>();
        if (connectionProperty.getHost() != null) {
            config.add(HOST + SEPARATOR + connectionProperty.getHost());
        }
        if (connectionProperty.getPort() != null) {
            config.add(PORT + SEPARATOR + connectionProperty.getPort());
        }
        if (connectionProperty.getTransactionIdentifierOffset() != null) {
            config.add(TRANSACTION_IDENTIFIER_OFFSET + SEPARATOR + connectionProperty.getTransactionIdentifierOffset());
        }
        if (connectionProperty.getPrepareTime() != null) {
            config.add(PREPARE_TIME + SEPARATOR + connectionProperty.getPrepareTime());
        }
        Path path = Paths.get(CONFIG_PATH);
        try {
            Files.write(path, config, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取压力值
     * @return 压力值
     */
    public List<PressureInfo> getPressureInfoList() {
        ModbusClient modbusClient = ClientContainer.getModbusClient(getConnectionProperty());
        Channel clientChannel = modbusClient.getChannel();
        ChannelSender clientSender = ChannelSenderFactory.getInstance().get(clientChannel);
        ReadHoldingRegistersResponse response;
        try {
            response = clientSender.readHoldingRegisters(0, 100);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        int[] registers = response.getRegisters();
        return IntStream.range(0, registers.length).mapToObj(i -> new PressureInfo(String.valueOf(i), String.valueOf(registers[i]))).collect(Collectors.toList());
    }

    /**
     * 输出excel表格
     */
    public void exportExcel(List<PressureInfo> data, HttpServletResponse response) {
        response.setHeader("Content-disposition", "attachment; filename=" + EXCEL_PATH);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        try (ServletOutputStream out = response.getOutputStream()) {
//        try (FileOutputStream out = new FileOutputStream(EXCEL_PATH)) {
            Workbook workbook = new XSSFWorkbook();
            //新建表格
            Sheet sheet = workbook.createSheet();
            //生成表头
            Row thead = sheet.createRow(0);
            String[] fieldsName = getFieldsName(data.get(0).getClass());
            for (int i = 0; i < fieldsName.length; i++) {
                Cell cell = thead.createCell(i);
                cell.setCellValue(fieldsName[i]);
            }
            //保存所有属性的getter方法名
            Method[] methods = new Method[fieldsName.length];
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i+1);
                Object obj = data.get(i);
                for (int j = 0; j < fieldsName.length; j++) {
                    //加载第一行数据时，初始化所有属性的getter方法
                    if(i == 0){
                        String fieldName = fieldsName[j];
                        //处理布尔值命名 "isXxx" -> "setXxx"
                        if (fieldName.contains("is")) {
                            fieldName = fieldName.split("is")[1];
                        }
                        methods[j] = obj.getClass().getMethod("get" +
                                fieldName.substring(0,1).toUpperCase() +
                                fieldName.substring(1));
                    }
                    Cell cell = row.createCell(j);
                    Object value = methods[j].invoke(obj);
                    //注意判断 value 值是否为空
                    if(value == null){
                        value = "无";
                    }
                    cell.setCellValue(value.toString());
                }
            }
            workbook.write(out);
            out.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取对象的属性名数组
     * @param clazz Class 对象，用于获取该类的信息
     * @return 该类的所有属性名数组
     */
    private static String[] getFieldsName(Class clazz){
        Field[] fields = clazz.getDeclaredFields();
        String[] fieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            fieldNames[i] = fields[i].getName();
        }
        return fieldNames;
    }
}

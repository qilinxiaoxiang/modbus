package com.xiangzheng.modbus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com")
public class ModbusApplication {
	public static void main(String[] args) {
		SpringApplication.run(ModbusApplication.class, args);
	}

}

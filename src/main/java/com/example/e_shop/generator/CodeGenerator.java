package com.example.e_shop.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

public class CodeGenerator {
    public static void main(String[] args) {
        FastAutoGenerator.create(
                        "jdbc:postgresql://aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres",
                        "postgres.lnviiveexzabxkhrkmyn",
                        "WOSHILEI123a"
                )
                .globalConfig(builder -> {
                    builder.author("e-shop")
                            .outputDir(System.getProperty("user.dir") + "/src/main/java")
                            .commentDate("yyyy-MM-dd");
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.e_shop")
                            .entity("entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    System.getProperty("user.dir") + "/src/main/resources/mapper"
                            ));
                })
                .strategyConfig(builder -> {
                    builder.addInclude("productbanner")
                            .entityBuilder()
                            .enableLombok()
                            .enableFileOverride() // 这一行是关键！对所有文件开启覆盖
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .controllerBuilder()
                            .enableRestStyle()
                            .serviceBuilder()
                            .formatServiceFileName("%sService");
                })
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}

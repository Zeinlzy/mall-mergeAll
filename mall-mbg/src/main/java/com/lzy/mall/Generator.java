package com.lzy.mall;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * 这是一个用于执行 MyBatis Generator 的主类。
 * 它负责读取并解析 Mybatis Generator 的配置文件 (generatorConfig.xml)，
 * 然后调用 MyBatis Generator 的核心 API 来生成实体类 (Model)、Mapper 接口和 Mapper XML 文件。
 */
public class Generator {

    /**
     * 应用程序的入口方法。
     * 该方法包含了启动和运行 MyBatis Generator 的所有步骤。
     *
     * @param args 命令行参数 (此程序未使用)
     * @throws Exception 如果在读取配置文件、解析或生成过程中发生错误，则抛出异常
     */
    public static void main(String[] args) throws Exception {
        // MBG 执行过程中的警告信息列表
        // MyBatis Generator 在生成过程中可能会产生警告（例如，如果配置有问题或遇到数据库的某些情况），这些警告会被收集到这个列表中。
        List<String> warnings = new ArrayList<String>();

        // 当生成的代码文件已经存在时，是否覆盖原文件
        // 设置为 true 表示如果生成的目标文件（如实体类、Mapper 接口、XML）已经存在，则直接替换掉旧文件。
        // 设置为 false 则会保留旧文件，并在控制台或日志中输出警告。
        boolean overwrite = true;

        // 读取我们的 MBG 配置文件 generatorConfig.xml
        // 使用类加载器获取位于 classpath 根目录下的 generatorConfig.xml 文件作为输入流。
        InputStream is = Generator.class.getResourceAsStream("/generatorConfig.xml");

        // 创建一个配置解析器
        // ConfigurationParser 用于解析 generatorConfig.xml 文件，它需要一个 warnings 列表来收集解析过程中产生的警告。
        ConfigurationParser cp = new ConfigurationParser(warnings);

        // 解析配置文件，生成 Configuration 对象
        // Configuration 对象包含了 generatorConfig.xml 中定义的所有配置信息，如数据库连接、生成的包路径、要生成的表等。
        Configuration config = cp.parseConfiguration(is);

        // 关闭输入流
        // 及时关闭文件流是良好的编程习惯。
        is.close();

        // 创建一个 ShellCallback 对象
        // ShellCallback 用于控制 MyBatis Generator 如何与文件系统进行交互（如创建目录、覆盖文件等）。
        // DefaultShellCallback 是默认的实现，通过构造函数传入 overwrite 标志来控制是否覆盖文件。
        DefaultShellCallback callback = new DefaultShellCallback(overwrite);

        // 创建 MyBatisGenerator 核心对象
        // MyBatisGenerator 是生成器的核心类，需要传入配置对象、ShellCallback 对象和 warnings 列表。
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);

        // 执行代码生成过程
        // 调用 generate() 方法开始生成代码。参数可以是一个 ProgressCallback 对象，用于报告生成进度，这里传入 null 表示不报告进度。
        myBatisGenerator.generate(null);

        // 输出生成过程中收集到的警告信息
        // 遍历 warnings 列表，将所有警告打印到控制台。
        System.out.println("Generated code with warnings:");
        for (String warning : warnings) {
            System.out.println(warning);
        }
        System.out.println("Code generation finished!");
    }
}

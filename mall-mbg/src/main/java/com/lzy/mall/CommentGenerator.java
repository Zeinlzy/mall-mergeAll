package com.lzy.mall;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Properties;

/**
 * 自定义注释生成器
 * 继承 DefaultCommentGenerator，以保留 Mybatis Generator 默认的注释行为
 * 主要目的是为了给生成的实体类字段添加 Swagger 的 @ApiModelProperty 注解，
 * 并根据数据库字段的备注信息生成该注解的 value 属性。
 * 同时，控制生成的 Java 文件（尤其是 Model 类）导入 ApiModelProperty 注解。
 */
public class CommentGenerator extends DefaultCommentGenerator {

    // 用于控制是否添加数据库字段备注作为注释或 Swagger 注解的旗标
    private boolean addRemarkComments = false;
    // Example 类的文件后缀，用于在 addJavaFileComment 方法中判断文件类型
    private static final String EXAMPLE_SUFFIX="Example";
    // Mapper 接口的文件后缀，用于在 addJavaFileComment 方法中判断文件类型
    private static final String MAPPER_SUFFIX="Mapper";
    // Swagger 的 ApiModelProperty 注解的完整类名，用于在 addJavaFileComment 方法中导入该类
    private static final String API_MODEL_PROPERTY_FULL_CLASS_NAME="io.swagger.v3.oas.annotations.media.Schema";

    /**
     * 设置用户在 generatorConfig.xml 中配置的参数。
     * Mybatis Generator 在初始化 CommentGenerator 时会调用此方法，传入配置文件中的属性。
     *
     * @param properties 从配置文件中读取的属性集合
     */
    @Override
    public void addConfigurationProperties(Properties properties) {
        // 调用父类方法，处理 DefaultCommentGenerator 支持的属性
        super.addConfigurationProperties(properties);
        // 从 properties 中获取名为 "addRemarkComments" 的属性值，并将其转换为 boolean 类型
        // 这个属性通常在 generatorConfig.xml 的 commentGenerator 标签中配置
        this.addRemarkComments = StringUtility.isTrue(properties.getProperty("addRemarkComments"));
    }

    /**
     * 给生成的 Model 类中的字段（对应数据库表的列）添加注释或注解。
     * Mybatis Generator 在生成每个字段时都会调用此方法。
     *
     * @param field 正在生成的字段对象 (org.mybatis.generator.api.dom.java.Field)
     * @param introspectedTable 正在处理的数据库表信息
     * @param introspectedColumn 正在处理的数据库列信息 (org.mybatis.generator.api.IntrospectedColumn)
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        // 获取数据库字段的备注信息 (comment)
        String remarks = introspectedColumn.getRemarks();

        // 根据 addRemarkComments 配置和数据库字段是否有备注，判断是否添加 Swagger 注解
        if(addRemarkComments && StringUtility.stringHasValue(remarks)){
            // 注释掉的这行是添加标准的 Javadoc 注释，如果需要同时有 Javadoc 和 Swagger 注解，可以取消注释
            // addFieldJavaDoc(field, remarks);

            // 数据库字段的备注信息可能包含双引号 (")，这会破坏注解的字符串格式。
            // 这里将备注中的双引号替换为单引号 (') 进行转义，以便放入 @ApiModelProperty 的 value 属性中
            if(remarks.contains("\"")){
                remarks = remarks.replace("\"", "'");
            }

            // 给生成的 Model 类字段添加 @ApiModelProperty(value = "...") 注解
            // 这个注解用于 Swagger UI 显示字段的描述信息
            field.addJavaDocLine("@Schema(description = \"" + remarks + "\")");
        }
    }

    /**
     * (这是一个自定义的辅助方法，用于添加标准的 Javadoc 形式的字段注释，但在 addFieldComment 方法中目前被注释掉了)
     * 给 Model 类的字段添加标准的 Javadoc 注释。
     *
     * @param field 正在生成的字段对象
     * @param remarks 数据库字段的备注信息
     */
    private void addFieldJavaDoc(Field field, String remarks) {
        // 添加 Javadoc 注释的开始标记
        field.addJavaDocLine("/**");
        // 将数据库字段的备注信息按行分割
        String[] remarkLines = remarks.split(System.getProperty("line.separator"));
        // 遍历备注的每一行，并添加到 Javadoc 中
        for(String remarkLine : remarkLines){
            field.addJavaDocLine(" * " + remarkLine);
        }
        // 添加 Javadoc 标签 (如 @param, @return 等)，这里调用父类或 DefaultCommentGenerator 的方法
        addJavadocTag(field, false);
        // 添加 Javadoc 注释的结束标记
        field.addJavaDocLine(" */");
    }

    /**
     * 给生成的 Java 文件（如 Model, Mapper, Example）添加文件级别的注释或导入语句。
     * Mybatis Generator 在生成每个 Java 文件时都会调用此方法。
     *
     * @param compilationUnit 正在生成的 Java 文件单元 (org.mybatis.generator.api.dom.java.CompilationUnit)
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        // 调用父类方法，添加默认的文件注释（如生成时间戳等）
        super.addJavaFileComment(compilationUnit);

        // 获取当前正在生成的 Java 文件的完全限定名 (Fully Qualified Name, FQN)
        String fullyQualifiedName = compilationUnit.getType().getFullyQualifiedName();

        // 判断当前文件是否是 Model 类。通过检查文件名是否包含 Mapper 后缀或 Example 后缀来排除 Mapper 接口和 Example 类
        // 如果文件名既不包含 "Mapper" 也不包含 "Example"，则认为是 Model 类
        if (!fullyQualifiedName.contains(MAPPER_SUFFIX) && !fullyQualifiedName.contains(EXAMPLE_SUFFIX)) {
            // 如果是 Model 类，则添加对 Swagger 的 ApiModelProperty 注解类的导入语句
            // 这是因为我们在 addFieldComment 方法中给 Model 类的字段使用了 @ApiModelProperty 注解
            compilationUnit.addImportedType(new FullyQualifiedJavaType(API_MODEL_PROPERTY_FULL_CLASS_NAME));
        }
    }


}
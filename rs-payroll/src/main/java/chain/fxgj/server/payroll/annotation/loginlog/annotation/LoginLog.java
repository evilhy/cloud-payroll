package chain.fxgj.server.payroll.annotation.loginlog.annotation;

import java.lang.annotation.*;

/**
 * 创建自定义注解
 *
 * 注意:①注解方法不能有参数。
 *      ②注解方法的返回类型局限于原始类型，字符串，枚举，注解，或以上类型构成的数组。
 *      ③注解方法可以包含默认值。
 * 注解可以包含与其绑定的元注解，元注解为注解提供信息，有四种元注解类型：
 * @Documented – 表示使用该注解的元素应被javadoc或类似工具文档化，它应用于类型声明，类型声明的注解会影响客户端对注解元素的使用。
 *                  如果一个类型声明添加了Documented注解，那么它的注解会成为被注解元素的公共API的一部分。
 * @Target – 表示支持注解的程序元素的种类，一些可能的值有TYPE, METHOD, CONSTRUCTOR, FIELD等等。
 *                  如果Target元注解不存在，那么该注解就可以使用在任何程序元素之上。
 * @Inherited – 表示一个注解类型会被自动继承，如果用户在类声明的时候查询注解类型，同时类声明中也没有这个类型的注解，
 *                  那么注解类型会自动查询该类的父类，这个过程将会不停地重复，直到该类型的注解被找到为止，或是到达类结构的顶层（Object）。
 * @Retention – 表示注解类型保留时间的长短，它接收RetentionPolicy参数，可能的值有SOURCE, CLASS, 以及RUNTIME。
 *
 * Java提供3种内置注解。
 * 1. @Override – 当我们想要覆盖父类的一个方法时，需要使用该注解告知编译器我们正在覆盖一个方法。
 *      这样的话，当父类的方法被删除或修改了，编译器会提示错误信息。大家可以学习一下为什么我们总是应该在覆盖方法时使用Java覆盖注解。
 * 2. @Deprecated – 当我们想要让编译器知道一个方法已经被弃用(deprecate)时，应该使用这个注解。
 *      Java推荐在javadoc中提供信息，告知用户为什么这个方法被弃用了，以及替代方法是什么。
 * 3. @SuppressWarnings – 这个注解仅仅是告知编译器，忽略它们产生了特殊警告，比如：在java泛型中使用原始类型。
 *      它的保持性策略(retention policy)是SOURCE，在编译器中将被丢弃。
 *
 * ps:有关注解的反射方法的作用：
 * isAnnotationPresent(Class<? extends Annotation> annotationClass)	判断是否包含指定类型的注解
 * getAnnotation(Class annotationClass)	获取该元素的指定类型的注解
 * getAnnotation()	获取此元素上存在的所有注解
 *
 *
 * 微信登录日志注解事项 [重点]
 *      使用此注解时:
 *      ①方法入参需要有 @RequestHeader("jsession-id") String jsessionId
 *      或者
 *      ②返回参数中需要有 openId
 *      举例：详见接口 /wechat/wxCallback 注解使用
 */
@Inherited
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginLog {

    /**
     * 注解属性的声明
     * 注解属性的作用是，原来写在配置文件中的信息，可以通过注解的属性进行描述。
     * 注解的属性声明方式：String name();
     * 属性默认值声明方式：String name() default “xxx”;
     * 特殊属性value：如果注解中有一个名称value的属性，那么使用注解时可以省略value=部分，如@MyAnnotation("xxx")
     * @return
     */
    String value() default "";



    //注解可以使用如下类型配置注解包含的信息
//    String name();//字符串
//    String password() default "123";//带默认值的字符串
//    double age() default 12;//double型
//    Gender gender() default Gender.FEMALE;//枚举型
//    Class clazz();//类类型
//    MyAnnotation3 my3();//注解类型，（在注解中包含注解）
//    int[] arr() default {1,2,3};//一元数组类型
//    Gender[] gs();
//
//
//    enum Gender{MALE,FEMALE};
//
//    @interface MyAnnotation3 {
//        String[] value();
//    }
}

package chain.fxgj.server.payroll;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.RootDoc;
import org.springframework.restdocs.payload.FieldDescriptor;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

/**
 * @author chain
 * create by chain on 2019-01-24 18:12
 **/

public class JavaDocReader extends Doclet {
    private static RootDoc root;

    // 一个简单Doclet,收到 RootDoc对象保存起来供后续使用
    // 参见参考资料6

    public static boolean start(RootDoc root) {
        JavaDocReader.root = root;
        return true;
    }

    // 显示DocRoot中的基本信息
    public static FieldDescriptor[] show(String[] needFields) {
        ClassDoc[] classes = root.classes();
        List<FieldDescriptor> fieldDescriptors = new ArrayList<>();
        for (int i = 0; i < classes.length; ++i) {

            for (FieldDoc fieldDoc : classes[i].fields()) {
                boolean addFlag = true;
                if (needFields != null && needFields.length > 0) {
                    addFlag = false;
                    for (String needFiled : needFields) {
                        if (needFiled.equals(fieldDoc.name())) {
                            addFlag = true;
                        }
                    }
                }
                if (addFlag) {
                    FieldDescriptor fieldDescriptor = fieldWithPath(fieldDoc.name())
                            .description(fieldDoc.commentText()).type(fieldDoc.type().typeName())
                            .optional();
                    fieldDescriptors
                            .add(fieldDescriptor);

                }
            }
            return fieldDescriptors.toArray(new FieldDescriptor[fieldDescriptors.size()]);
        }
        return null;
    }

    public static RootDoc getRoot() {
        return root;
    }

    public static FieldDescriptor[] javaDoc(Class clas) {
        return JavaDocReader.javaDoc(clas, new String[]{});
    }

    public static FieldDescriptor[] javaDoc(Class clas, String... needFields) {

        // 调用com.sun.tools.javadoc.Main执行javadoc,参见 参考资料3
        // javadoc的调用参数，参见 参考资料1
        // -doclet 指定自己的docLet类名
        // -classpath 参数指定 源码文件及依赖库的class位置，不提供也可以执行，但无法获取到完整的注释信息(比如annotation)
        // -encoding 指定源码文件的编码格式
        String[] args = new String[]{"-private", "-doclet",
                JavaDocReader.class.getName(),
// 因为自定义的Doclet类并不在外部jar中，就在当前类中，所以这里不需要指定-docletpath 参数，
                "-docletpath",
                JavaDocReader.class.getResource("/").getPath(),
                "-encoding", "utf-8",
//                "-cp",
//                "/workspaces/cloud/kjb/core-fbo/build/classes/java/main:/Users/chain/.m2/repository/org/projectlombok/lombok/1.18.2/lombok-1.18.2.jar",
//                System.getProperty("user.dir") + "/src/main/java/" +
                "D:/Code/cloud-payroll/rs-payroll/src/main/java/" +
                        clas.getPackage().getName().replace(".", "/") + "/" +
                        clas.getSimpleName() + ".java"};

        com.sun.tools.javadoc.Main.execute(args);
        return show(needFields);
    }
}


package cn.xiaosheng996.NettyProtobufWebsocketServer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;


public class ClassUtils {

    /**
     * 获取同一路径下所有子类或接口实现类
     * @param cls
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static List<Class<?>> getAllAssignedClass(Class<?> cls) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Class<?> c : getClasses(cls)) {
            if (cls.isAssignableFrom(c) && !cls.equals(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    /**
     * 取得当前类路径下的所有类
     * @param cls
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getClasses(Class<?> cls) throws IOException, ClassNotFoundException {
        String pk = cls.getPackage().getName();
        String path = pk.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        return getClasses(new File(url.getFile()), pk);
    }

    /**
     * 迭代查找类
     * @param dir
     * @param pk
     * @return
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> getClasses(File dir, String pk) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!dir.exists()) {
            return classes;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                classes.addAll(getClasses(f, pk + "." + f.getName()));
            }
            String name = f.getName();
            if (name.endsWith(".class")) {
                classes.add(Class.forName(pk + "." + name.substring(0, name.length() - 6)));
            }
        }
        return classes;
    }

    /**
     * 迭代组装协议
     * @param packageName
     * @param clazz
     * @param delimiter
     * @return
     * @throws ClassNotFoundException
     */
    public static Map<Integer, Class<?>> getClasses(String packageName, Class<?> clazz, String delimiter)
        throws ClassNotFoundException {
        Map<Integer, Class<?>> map = new HashMap<>();
        String path = packageName.replace('.', '/');
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL url = classloader.getResource(path);
        for (Class<?> c : getClasses(new File(url.getFile()), packageName)) {
            if (Message.class.isAssignableFrom(c) && !Message.class.equals(c)) {
                if (c.getSimpleName().contains(delimiter)) {
                    int protocol = Integer.parseInt(
                        c.getSimpleName().substring(c.getSimpleName().indexOf(delimiter) + delimiter.length()));
                    map.put(protocol, c);
                }
            }
        }
        return map;
    }
}

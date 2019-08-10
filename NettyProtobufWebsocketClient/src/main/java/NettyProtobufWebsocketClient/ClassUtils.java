package NettyProtobufWebsocketClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    
    public static Method findMethod(Class<?> clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName))
                return method;
        }
        return null;
    }
    
    
    
    /**********************************************************************
     * 加载时判断指定包
     * @param filePath
     * @param specialName
     * @param childPackage
     * @return
     */
    public static List<Class<?>> getClassWithSpecialName(String filePath, String specialName, boolean childPackage) {
        List<Class<?>> classes = new ArrayList<>();
        List<String> classNames = getClassName(filePath, childPackage);
        for (String className : classNames) {
            if (!className.contains(specialName)) 
                continue;
            
            try {
                classes.add(Class.forName(className));
            } catch (Exception ex) {
                ex.printStackTrace();;
            }
        }
        return classes;
    }

    /**
     * 获取某路径下所有的class
     * @param filePath
     * @param childPackage
     * @return
     */
    public static List<Class<?>> getClassListByFilePath(String filePath, boolean childPackage) {
        List<Class<?>> classes = new ArrayList<>();
        List<String> classNames = getClassName(filePath, childPackage);
        for (String className : classNames) {
            try {
                classes.add(Class.forName(className));
            } catch (Exception ex) {
                ex.printStackTrace();;

            }
        }
        return classes;
    }

    /**
     * 从所有jar中搜索该包，并获取该包下所有类
     * @param urls URL集合
     * @param packagePath 包路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJars(URL[] urls, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                URL url = urls[i];
                String urlPath = url.getPath();
                // 不必搜索classes文件夹
                if (urlPath.endsWith("classes/")) {
                    continue;
                }
                String jarPath = urlPath + "!/" + packagePath;
                System.err.println("jarPath : " + jarPath);
                myClassName.addAll(getClassNameByJar(jarPath, childPackage));
            }
        }
        return myClassName;
    }

    /**
     * 获取某包下（包括该包的所有子包）所有类
     * @param packageName 包名
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName) {
        return getClassName(packageName, true);
    }

    /**
     * 获取某包下所有类
     * @param packageName 包名
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassName(String packageName, boolean childPackage) {
        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String type = url.getProtocol();
            if (type.equals("file")) {
                fileNames = getClassNameByFile(url.getPath(), childPackage);
            } else if (type.equals("jar")) {
                fileNames = getClassNameByJar(url.getPath(), childPackage);
            }
        } else {
            fileNames = getClassNameByJars(((URLClassLoader) loader).getURLs(), packagePath, childPackage);
        }
        return fileNames;
    }

    /**
     * 从项目文件获取某包下所有类
     * @param filePath 文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                if (childPackage) {
                    myClassName.addAll(getClassNameByFile(childFile.getPath(), childPackage));
                }
            } else {
                String childFilePath =  childFile.toURI().toString(); //childFile.getPath();
//                URI uri = childFile.toURI();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("/classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("/", ".");
                    myClassName.add(childFilePath);
                } else if (childFilePath.endsWith(".jar")) {
//                    childFilePath = childFilePath.substring(0, childFilePath.indexOf(".jar"));
//                    System.err.println("try to init jar :childFilePath" + childFilePath);
                    myClassName.addAll(getClassNameByJar(childFile.getPath(), "", childPackage));
                }

            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * @param jarPath jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJar(String jarPath, String packagePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<String>();
//        String[] jarInfo = jarPath.split("!");
//        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
//        String packagePath = jarInfo[1].substring(1);
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    if (childPackage) {
                        if (entryName.startsWith(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    } else {
                        int index = entryName.lastIndexOf("/");
                        String myPackagePath;
                        if (index != -1) {
                            myPackagePath = entryName.substring(0, index);
                        } else {
                            myPackagePath = entryName;
                        }
                        if (myPackagePath.equals(packagePath)) {
                            entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                            myClassName.add(entryName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	if (jarFile != null) {
        		try {
        			jarFile.close();
        		} catch (Exception e) {}
        	}
        }
        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * @param jarPath jar文件路径
     * @param childPackage 是否遍历子包
     * @return 类的完整名称
     */
    public static List<String> getClassNameByJar(String jarPath, boolean childPackage) {
    	List<String> myClassName = new ArrayList<String>();
    	String[] jarInfo = jarPath.split("!");
    	String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
    	String packagePath = jarInfo[1].substring(1);
    	JarFile jarFile = null;
    	try {
    		jarFile = new JarFile(jarFilePath);
    		Enumeration<JarEntry> entrys = jarFile.entries();
    		while (entrys.hasMoreElements()) {
    			JarEntry jarEntry = entrys.nextElement();
    			String entryName = jarEntry.getName();
    			if (entryName.endsWith(".class")) {
    				if (childPackage) {
    					if (entryName.startsWith(packagePath)) {
    						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
    						myClassName.add(entryName);
    					}
    				} else {
    					int index = entryName.lastIndexOf("/");
    					String myPackagePath;
    					if (index != -1) {
    						myPackagePath = entryName.substring(0, index);
    					} else {
    						myPackagePath = entryName;
    					}
    					if (myPackagePath.equals(packagePath)) {
    						entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
    						myClassName.add(entryName);
    					}
    				}
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (jarFile != null) {
    			try {
    				jarFile.close();
    			} catch (Exception e) {}
    		}
    	}
        return myClassName;
    }

    /**
     * 判断a是否为b的子类
     * @param a
     * @param b
     * @return
     */
    public static boolean isSubclass(Class<?> a, Class<?> b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        for (Class<?> x = a; x != null; x = x.getSuperclass()) {
            if (x == b) {
                return true;
            }
            if (b.isInterface()) {
                Class<?>[] interfaces = x.getInterfaces();
                for (Class<?> anInterface : interfaces) {
                    if (isSubclass(anInterface, b)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

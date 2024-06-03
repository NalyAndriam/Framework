package util;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Vector;

import annotation.Get;

public class Utilitaire {
    public static HashMap getAllClassesSelonAnnotation(String packageToScan,Class<?>annotation) throws Exception{
        //List<String> controllerNames = new ArrayList<>();
        HashMap<String,Mapping> hm=new HashMap<>();
        try {
            
            //String path = getClass().getClassLoader().getResource(packageToScan.replace('.', '/')).getPath();
            String path = Thread.currentThread().getContextClassLoader().getResource(packageToScan.replace('.', '/')).getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            File packageDir = new File(decodedPath);

            File[] files = packageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageToScan + "." + file.getName().replace(".class", "");
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotation.asSubclass(java.lang.annotation.Annotation.class))) {
                            //controllerNames.add(clazz.getSimpleName());
                            Method[]methods=clazz.getDeclaredMethods();
                            for (Method m : methods) {
                                if (m.isAnnotationPresent(Get.class)) {
                                    Get getAnnotation= m.getAnnotation(Get.class);
                                    hm.put(getAnnotation.url(),new Mapping(clazz.getName(),m.getName()));
                                }
                            }
                        }
                    }
                }
            }
           
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        }
        return hm;
    }

   
}

package com.biubiu;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Haibiao.Zhang on 2018/8/13.
 */
public class CustomBeanUtil {

    /**
     * List<JavaBean>转换为List<Map>
     *
     * @param beans beans
     * @param clazz clazz
     * @return List<Map>
     * @throws Exception 异常
     */
    public static List<Map> listBean2listMap(List beans, Class<?> clazz) throws Exception {
        List<Map> maps = new ArrayList<>();
        for (Object bean : beans) {
            maps.add(bean2map(bean, clazz));
        }
        return maps;
    }

    /**
     * JavaBean转换为Map
     *
     * @param bean  bean
     * @param clazz clazz
     * @return map
     * @throws Exception 异常
     */
    public static Map<String, Object> bean2map(Object bean, Class<?> clazz) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //获取指定类（Person）的BeanInfo 对象
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        //获取所有的属性描述器
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String key = pd.getName();
            Method getter = pd.getReadMethod();
            Object value = getter.invoke(bean);
            map.put(key, value);
        }
        return map;
    }

    /**
     * Map转换为JavaBean
     *
     * @param map   map
     * @param clazz clazz
     * @return T
     * @throws Exception 异常
     */
    private static <T> T map2bean(Map<String, Object> map, Class<T> clazz) throws Exception {
        //创建JavaBean对象
        T obj = clazz.newInstance();
        //获取指定类的BeanInfo对象
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        //获取所有的属性描述器
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            Object value = map.get(pd.getName());
            Method setter = pd.getWriteMethod();
            setter.invoke(obj, value);
        }
        return obj;
    }

}

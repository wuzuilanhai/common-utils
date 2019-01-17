package com.biubiu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Haibiao.Zhang on 2018/8/25.
 */
public class ObjectConverterUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectConverterUtil.class);

    /**
     * 对象转换器
     *
     * @param source 源对象
     * @param target 目标class对象
     * @return 转换对象
     */
    public static <T> T convert(Object source, Class<T> target) {
        T object = null;
        try {
            object = target.newInstance();
            BeanUtils.copyProperties(source, object);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return object;
    }

    /**
     * 对象列表转换器
     *
     * @param sourceList 源对象列表
     * @param target     目标class对象
     * @return 转换对象列表
     */
    public static <T> List<T> convertList(List<?> sourceList, Class<T> target) {
        List<T> result = new ArrayList<>();
        sourceList.forEach(source -> convert(source, target));
        return result;
    }

}

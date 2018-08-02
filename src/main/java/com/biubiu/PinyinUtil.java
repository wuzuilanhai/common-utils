package com.biubiu;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: Haibiao.Zhang
 * @Date: 2018/6/6 13:51
 */
public class PinyinUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PinyinUtil.class);

    private static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();

    static {
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    public static enum Type {
        UPPERCASE,              //全部大写
        LOWERCASE,              //全部小写
        FIRST_UPPER              //首字母大写
    }

    public static String toPinYin(String str) {
        String result = null;
        try {
            result = toPinYin(str, "", Type.LOWERCASE);
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            LOGGER.error(badHanyuPinyinOutputFormatCombination.getMessage());
        }
        return result;
    }

    public static String getPinYinHeadChar(String str) {
        StringBuilder convert = new StringBuilder();
        for (int j = 0; j < str.length(); j++) {
            char word = str.charAt(j);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                convert.append(pinyinArray[0].charAt(0));
            } else {
                convert.append(word);
            }
        }
        return convert.toString();
    }

    public static String toPinYin(String str, String split) {
        String result = null;
        try {
            result = toPinYin(str, split, Type.LOWERCASE);
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            LOGGER.error(badHanyuPinyinOutputFormatCombination.getMessage());
        }
        return result;
    }

    /**
     * 将str转换成拼音，如果不是汉字或者没有对应的拼音，则不作转换
     * 如： 明天 转换成 minttian
     *
     * @param str   要转化的汉字
     * @param split 转化结果的分割符
     * @param type  转化结果类型
     * @return 拼音字符串
     * @throws BadHanyuPinyinOutputFormatCombination
     */
    private static String toPinYin(String str, String split, Type type) throws BadHanyuPinyinOutputFormatCombination {
        if (str == null || str.trim().length() == 0)
            return "";
        if (type == Type.UPPERCASE)
            format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        else
            format.setCaseType(HanyuPinyinCaseType.LOWERCASE);

        StringBuilder py = new StringBuilder();
        String temp;
        String[] t;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((int) c <= 128)
                py.append(c);
            else {
                t = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (t == null || t.length == 0)
                    py.append(c);
                else {
                    temp = t[0];
                    if (type == Type.FIRST_UPPER)
                        temp = t[0].toUpperCase().charAt(0) + temp.substring(1);
                    py.append(temp).append(i == str.length() - 1 ? "" : split);
                }
            }
        }
        return py.toString().trim();
    }

}

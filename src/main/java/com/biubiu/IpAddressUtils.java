package com.biubiu;

import com.google.common.base.Strings;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 张海彪
 * @create 2018-02-11 下午6:51
 */
public class IpAddressUtils {

    /**
     * 获取客户端实际IP地址
     *
     * @param request 请求实体
     * @return 客户端实际IP地址
     */
    public String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (Strings.isNullOrEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            return ip.split(",")[0];
        } else {
            return ip;
        }
    }

}

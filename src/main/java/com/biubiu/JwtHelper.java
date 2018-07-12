package com.biubiu;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * Created by Haibiao.Zhang on 2018/7/12.
 */
public class JwtHelper {

    private final static String BASE64_SECRET = "MDk4ZjZiY2Q0NjIxZDM3M2NhZGU0ZTgzMjYyN2I0ZjY=";

    private final static int EXPIRES_SECOND = 172800000;

    /**
     * 解析jwt信息
     *
     * @param jsonWebToken jwt信息
     * @return 信息
     */
    public static Claims parseJWT(String jsonWebToken) {
        return Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(BASE64_SECRET))
                .parseClaimsJws(jsonWebToken)
                .getBody();
    }

    /**
     * 生成JWT
     *
     * @param username   用户名
     * @param roles      角色信息
     * @param privileges 权限信息
     * @return JWT字符串
     */
    public static String createJWT(String username, String roles, String privileges) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //生成签名密钥
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(BASE64_SECRET);
        Key signInKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //添加构成JWT的参数
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                .claim("user_name", username)
                .claim("user_role", roles)
                .claim("user_privilege", privileges)
                .signWith(signatureAlgorithm, signInKey);
        //添加Token过期时间
        if (EXPIRES_SECOND >= 0) {
            long expMillis = nowMillis + EXPIRES_SECOND;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp).setNotBefore(now);
        }
        return builder.compact();
    }

    public static void main(String[] args) {
        String jwt = createJWT("zhanghaibiao", "admin", "admin:query");
        System.out.println(jwt);
        Claims claims = parseJWT(jwt);
        System.out.println(claims.get("user_name"));
        System.out.println(claims.get("user_role"));
        System.out.println(claims.get("user_privilege"));
    }


}

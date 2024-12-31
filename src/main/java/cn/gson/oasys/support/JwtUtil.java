package cn.gson.oasys.support;
 
import cn.gson.oasys.entity.User;
import cn.gson.oasys.support.exception.UnknownAccountException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
 
/**
 * @description: Jwt工具类，生成JWT和认证
 * @author: heshi
 */
@Component
public class JwtUtil {

    /**
     * 密钥
     */
    private static final String SECRET = "my_secret";
 
    /**
     * 过期时间
     **/
    private static final long EXPIRATION = 1800L;//单位为秒
 
    /**
     * 生成用户token,设置token超时时间
     */
    public static String createToken(User user) {
        //过期时间
        Date expireDate = new Date(System.currentTimeMillis() + EXPIRATION * 100000);
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        user.setToken(null);
        user.setPassword(null);
        user.setPhone(null);

        String parse = JacksonUtil.parse(user);

        String token = JWT.create()
                .withHeader(map)// 添加头部
                //可以将基本信息放到claims中
                .withClaim("user", parse)//
                .withExpiresAt(expireDate) //超时设置,设置过期的日期
                .withIssuedAt(new Date()) //签发时间
                .sign(Algorithm.HMAC256(SECRET)); //SECRET加密
        return token;
    }
 
    /**
     * 校验token并解析token
     */
    public static User verifyToken(String token) {
        DecodedJWT jwt;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            jwt = verifier.verify(token);
        } catch (Exception e) {
            throw new UnknownAccountException();
        }

        Claim userIdClaim = jwt.getClaim("user");

        if (!userIdClaim.isNull()) {
            String aLong = userIdClaim.asString();
            User user = JacksonUtil.parse(aLong, User.class);
            return user;
        }

        return null;
    }
 
}
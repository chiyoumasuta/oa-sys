package cn.gson.oasys.support.kaptcha;

import com.google.code.kaptcha.Producer;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 生成和校验验证码
 *
 * @author 不愿透露
 * @date 2022/5/31 13:34
 */
@Component
public class CaptchaUtil {
    public static final String CAPTCHA_CODE_KEY = "captcha_codes";
    private static final String CAPTCHA_TYPE = "char";
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    /**
     * 生成验证码
     *
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author 不愿透露
     * @date 2022/5/31 13:02
     */
    public Map<String, String> getCode(boolean getCode) {
        try {
            Map<String, String> map = new HashMap<>();
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            String verifyKey = CAPTCHA_CODE_KEY + uuid;

            String capStr = null, code = null;
            BufferedImage image = null;
            //数字计算
            if ("math".equals(CAPTCHA_TYPE)) {

                String capText = captchaProducerMath.createText();
                capStr = capText.substring(0, capText.lastIndexOf("@"));
                code = capText.substring(capText.lastIndexOf("@") + 1);
                image = captchaProducerMath.createImage(capStr);
            } else if ("char".equals(CAPTCHA_TYPE)) {
                //数字字母
                capStr = code = captchaProducer.createText();
                image = captchaProducer.createImage(capStr);
            }
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            //3分钟有效
            HttpSession session = req.getSession();
            //session.setMaxInactiveInterval(3 * 60);
            session.setAttribute(verifyKey, code);
            FastByteArrayOutputStream os = new FastByteArrayOutputStream();
            try {
                ImageIO.write(image, "jpg", os);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            map.put("uuid", uuid);
            if (getCode) {
                map.put("code", Base64.getEncoder().encodeToString(code.getBytes()));
            }
            map.put("img", Base64.getEncoder().encodeToString(os.toByteArray()));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 验证
     *
     * @param code 用户输入的验证码值
     * @param uuid 随机数
     * @return org.gcm.fiber.support.UtilResultSet
     * @author 不愿透露
     * @date 2022/5/31 13:01
     */
    public boolean validateCaptcha(String code, String uuid) {
        try {
            if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid)) {
                return false;
            }
            String verifyKey = CAPTCHA_CODE_KEY + uuid;
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            HttpSession session = req.getSession();
            String captcha = (String) session.getAttribute(verifyKey);
            session.removeAttribute(verifyKey);
            if (StringUtils.isEmpty(captcha)) {
                return false;
            }
            if (!code.equalsIgnoreCase(captcha)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

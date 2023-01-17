package com.jeesite.common.shiro.realm;

import cn.hutool.core.codec.Base64;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSONObject;
import com.jeesite.common.shiro.authc.FormToken;
import com.jeesite.common.shiro.authc.PigxToken;
import com.jeesite.common.utils.SpringUtils;
import com.jeesite.modules.sys.entity.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class PigxAuthRealm extends BaseAuthorizingRealm {


    public PigxAuthRealm() {
        super();
        this.setAuthenticationTokenClass(PigxToken.class);
    }


    /**
     * 获取登录凭证，将 authcToken 转换为 FormToken，参考 CAS 实现
     */
    @Override
    protected FormToken getFormToken(AuthenticationToken authcToken) {

        if (authcToken == null) {
            return null;
        }
        PigxToken pigxToken = (PigxToken) authcToken;

        String body = getAccessToken(pigxToken.getCode());

        String token = JSONObject.parseObject(body).getString("access_token");

        String ssoUser = getSsoUser(token);
        JSONObject sysUser = JSONObject.parseObject(ssoUser).getJSONObject("data").getJSONObject("sysUser");

        pigxToken.setUsername(sysUser.getString("username"));

        // 扩展其他数据
        return pigxToken;
    }


    /**
     * 用于用户根据登录信息获取用户信息<br>
     * 1、默认根据登录账号登录信息，如：UserUtils.getByLoginCode(token.getUsername(), token.getParam("corpCode"));<br>
     * 2、如果增加其它登录，请重写此方法，如根据手机号或邮箱登录返回用户信息。
     */
    @Override
    protected User getUserInfo(FormToken token) {
        // 扩展用户信息获取

        return super.getUserInfo(token);
    }

    /**
     * 校验登录凭证，如密码验证，token验证，验证失败抛出 AuthenticationException 异常
     */
    @Override
    protected void assertCredentialsMatch(AuthenticationToken authcToken, AuthenticationInfo authcInfo) throws AuthenticationException {

    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return super.supports(token);
    }


    public String getAccessToken(String code) {
        Map<String, String> stringStringMap = buildRequestHeader();

        try {
            Environment environment = SpringUtils.getBean(Environment.class);
            Map<String, Object> map = new HashMap<>();
            map.put("grant_type", "authorization_code");
            map.put("scope", environment.getProperty("sso.scope"));
            map.put("code", code);

            String callback = environment.getProperty("sso.callback-url");
            String auth = environment.getProperty("sso.gateway-server");
            map.put("redirect_uri", callback);
            HttpResponse execute = HttpRequest.post(auth + "/auth/oauth2/token")
                    .headerMap(stringStringMap, true)
                    .form(map)
                    .execute();
            return execute.body();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map<String, String> buildRequestHeader() {
        HashMap<String, String> objectObjectHashMap = new HashMap<>();
        Environment environment = SpringUtils.getBean(Environment.class);
        String clientId = environment.getProperty("sso.client-id");
        String clientSecret = environment.getProperty("sso.client-secret");

        final String basicAuthorization = String.format("%s:%s", clientId, clientSecret);

        HttpHeaders headers = new HttpHeaders();

        String encodeToString = Base64.encode(basicAuthorization.getBytes());
        objectObjectHashMap.put(HttpHeaders.AUTHORIZATION, "Basic " + encodeToString);
        objectObjectHashMap.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        return objectObjectHashMap;
    }

    private String getSsoUser(String accessToken) {
        Environment environment = SpringUtils.getBean(Environment.class);
        String auth = environment.getProperty("sso.gateway-server");
        HttpResponse execute = HttpRequest.get(auth + "/admin/user/info")
                .header("Authorization","Bearer "+accessToken)
                .execute();
        return execute.body();

    }

}

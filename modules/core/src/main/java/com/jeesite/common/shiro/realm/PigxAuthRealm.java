package com.jeesite.common.shiro.realm;

import com.jeesite.common.shiro.authc.FormToken;
import com.jeesite.common.shiro.authc.LdapToken;
import com.jeesite.common.shiro.authc.PigxToken;
import com.jeesite.modules.sys.entity.User;
import com.jeesite.modules.sys.service.UserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.cas.CasToken;

public class PigxAuthRealm extends BaseAuthorizingRealm{



    public PigxAuthRealm(){
        super();
        this.setAuthenticationTokenClass(PigxToken.class);
    }


    /**
     * 获取登录凭证，将 authcToken 转换为 FormToken，参考 CAS 实现
     */
    @Override
    protected FormToken getFormToken(AuthenticationToken authcToken) {

        if (authcToken == null){
            return null;
        }
        PigxToken pigxToken = (PigxToken) authcToken;

        // 根据 code 获取信息

        pigxToken.setUsername("admin");
        pigxToken.setPassword("123456");


        return pigxToken;
    }



    /**
     * 用于用户根据登录信息获取用户信息<br>
     * 1、默认根据登录账号登录信息，如：UserUtils.getByLoginCode(token.getUsername(), token.getParam("corpCode"));<br>
     * 2、如果增加其它登录，请重写此方法，如根据手机号或邮箱登录返回用户信息。
     */
    @Override
    protected User getUserInfo(FormToken token) {
        // 根据 token 生成一个用户信息

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
}

/**
 * Copyright (c) 2013-Now http://jeesite.com All rights reserved.
 * No deletion without permission, or be held responsible to law.
 */
package com.jeesite.modules.config;

import java.util.Collection;
import java.util.Map;

import javax.servlet.Filter;

import com.jeesite.common.shiro.realm.PigxAuthRealm;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cas.CasSubjectFactory;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.web.filter.InvalidRequestFilter;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.jeesite.common.collect.ListUtils;
import com.jeesite.common.config.Global;
import com.jeesite.common.shiro.cas.CasOutHandler;
import com.jeesite.common.shiro.config.FilterChainDefinitionMap;
import com.jeesite.common.shiro.filter.CasFilter;
import com.jeesite.common.shiro.filter.FormFilter;
import com.jeesite.common.shiro.filter.InnerFilter;
import com.jeesite.common.shiro.filter.LdapFilter;
import com.jeesite.common.shiro.filter.LogoutFilter;
import com.jeesite.common.shiro.filter.PermissionsFilter;
import com.jeesite.common.shiro.filter.RolesFilter;
import com.jeesite.common.shiro.filter.UserFilter;
import com.jeesite.common.shiro.realm.AuthorizingRealm;
import com.jeesite.common.shiro.realm.CasAuthorizingRealm;
import com.jeesite.common.shiro.realm.LdapAuthorizingRealm;
import com.jeesite.common.shiro.session.SessionDAO;
import com.jeesite.common.shiro.session.SessionManager;
import com.jeesite.common.shiro.web.ShiroFilterFactoryBean;
import com.jeesite.common.shiro.web.WebSecurityManager;

/**
 * Shiro??????
 * @author ThinkGem
 * @version 2021-7-6
 */
@SuppressWarnings("deprecation")
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name="user.enabled", havingValue="true", matchIfMissing=true)
public class ShiroConfig {
	
	/**
	 * Apache Shiro Filter
	 */
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE + 5000)
	@ConditionalOnMissingBean(name="shiroFilterProxy")
	public FilterRegistrationBean<Filter> shiroFilterProxy(ShiroFilterFactoryBean shiroFilter) throws Exception {
		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
		bean.setFilter(shiroFilter.getObject());
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 5000);
		return bean;
	}
	
	/**
	 * ???????????????????????????
	 */
	private InnerFilter shiroInnerFilter() {
		return new InnerFilter();
	}
	
	/**
	 * CAS???????????????
	 */
	private CasFilter shiroCasFilter(CasAuthorizingRealm casAuthorizingRealm) {
		CasFilter bean = new CasFilter();
		bean.setAuthorizingRealm(casAuthorizingRealm);
		return bean;
	}

	private FormFilter shiroPigxFilter(PigxAuthRealm pigxAuthRealm) {
		FormFilter bean = new FormFilter();
		bean.setAuthorizingRealm(pigxAuthRealm);
		return bean;
	}
	
	/**
	 * LDAP???????????????
	 */
	private LdapFilter shiroLdapFilter(LdapAuthorizingRealm ldapAuthorizingRealm) {
		LdapFilter bean = new LdapFilter();
		bean.setAuthorizingRealm(ldapAuthorizingRealm);
		return bean;
	}

	/**
	 * Form???????????????
	 */
	private FormFilter shiroAuthcFilter(AuthorizingRealm authorizingRealm) {
		FormFilter bean = new FormFilter();
		bean.setAuthorizingRealm(authorizingRealm);
		return bean;
	}

	/**
	 * ???????????????
	 */
	private LogoutFilter shiroLogoutFilter(AuthorizingRealm authorizingRealm) {
		LogoutFilter bean = new LogoutFilter();
		bean.setAuthorizingRealm(authorizingRealm);
		return bean;
	}

	/**
	 * ????????????????????????
	 */
	private PermissionsFilter shiroPermsFilter() {
		return new PermissionsFilter();
	}

	/**
	 * ?????????????????????
	 */
	private RolesFilter shiroRolesFilter() {
		return new RolesFilter();
	}

	/**
	 * ?????????????????????
	 */
	private UserFilter shiroUserFilter() {
		return new UserFilter();
	}
	
	/**
	 * ?????????????????????
	 */
	private InvalidRequestFilter invalidRequestFilter() {
		InvalidRequestFilter bean = new InvalidRequestFilter();
		bean.setBlockNonAscii(false);
		return bean;
	}
	
	/**
	 * Shiro???????????????
	 */
	@Bean
	public ShiroFilterFactoryBean shiroFilter(WebSecurityManager webSecurityManager, AuthorizingRealm authorizingRealm,
											  CasAuthorizingRealm casAuthorizingRealm, LdapAuthorizingRealm ldapAuthorizingRealm, PigxAuthRealm pigxAuthRealm) {
		ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
		bean.setSecurityManager(webSecurityManager);
		bean.setLoginUrl(Global.getProperty("shiro.loginUrl"));
		bean.setSuccessUrl(Global.getProperty("adminPath")+"/index");
		Map<String, Filter> filters = bean.getFilters();
		filters.put("inner", shiroInnerFilter());
		filters.put("cas", shiroCasFilter(casAuthorizingRealm));
		filters.put("pigx", shiroPigxFilter(pigxAuthRealm));
		filters.put("ldap", shiroLdapFilter(ldapAuthorizingRealm));
		filters.put("authc", shiroAuthcFilter(authorizingRealm));
		filters.put("logout", shiroLogoutFilter(authorizingRealm));
		filters.put("perms", shiroPermsFilter());
		filters.put("roles", shiroRolesFilter());
		filters.put("user", shiroUserFilter());
		filters.put("invalidRequest", invalidRequestFilter());
		FilterChainDefinitionMap chains = new FilterChainDefinitionMap();
		chains.setFilterChainDefinitions(Global.getProperty("shiro.filterChainDefinitions"));
		chains.setDefaultFilterChainDefinitions(Global.getProperty("shiro.defaultFilterChainDefinitions"));
		bean.setFilterChainDefinitionMap(chains.getObject());
		return bean;
	}
	
	/**
	 * ???????????????????????????
	 */
	@Bean
	public AuthorizingRealm authorizingRealm(SessionDAO sessionDAO) {
		AuthorizingRealm bean = new AuthorizingRealm();
		bean.setSessionDAO(sessionDAO);
		return bean;
	}


	@Bean
	public PigxAuthRealm pigxAuthRealm(SessionDAO sessionDAO) {
		PigxAuthRealm bean = new PigxAuthRealm();
		bean.setSessionDAO(sessionDAO);
		return bean;
	}
	
	/**
	 * ??????????????????????????????????????????
	 */
	@Bean
	public CasOutHandler casOutHandler() {
		return new CasOutHandler();
	}
	
	/**
	 * CAS?????????????????????
	 */
	@Bean
	public CasAuthorizingRealm casAuthorizingRealm(SessionDAO sessionDAO, CasOutHandler casOutHandler) {
		CasAuthorizingRealm bean = new CasAuthorizingRealm();
		bean.setSessionDAO(sessionDAO);
		bean.setCasOutHandler(casOutHandler);
		bean.setCasServerUrl(Global.getProperty("shiro.casServerUrl"));
		bean.setCasServerCallbackUrl(Global.getProperty("shiro.casClientUrl") + Global.getAdminPath() + "/login-cas");
		return bean;
	}
	
	/**
	 * LDAP?????????????????????
	 */
	@Bean
	public LdapAuthorizingRealm ldapAuthorizingRealm(SessionDAO sessionDAO, CasOutHandler casOutHandler) {
		LdapAuthorizingRealm bean = new LdapAuthorizingRealm();
		JndiLdapContextFactory contextFactory = (JndiLdapContextFactory) bean.getContextFactory();
		contextFactory.setUrl(Global.getProperty("shiro.ldapUrl"/*, "ldap://127.0.0.1:389"*/));
		bean.setUserDnTemplate(Global.getProperty("shiro.ldapUserDn"/*, "uid={0},ou=users,dc=mycompany,dc=com"*/));
		bean.setSessionDAO(sessionDAO);
		return bean;
	}

	/**
	 * ??????Shiro??????????????????
	 */
	@Bean
	public WebSecurityManager webSecurityManager(AuthorizingRealm authorizingRealm, CasAuthorizingRealm casAuthorizingRealm,
			LdapAuthorizingRealm ldapAuthorizingRealm, SessionManager sessionManager, CacheManager shiroCacheManager, PigxAuthRealm pigxAuthRealm) {
		WebSecurityManager bean = new WebSecurityManager();
		Collection<Realm> realms = ListUtils.newArrayList();
		realms.add(authorizingRealm); // ?????????????????????????????????
		realms.add(casAuthorizingRealm);
		realms.add(ldapAuthorizingRealm);
		realms.add(ldapAuthorizingRealm);
		realms.add(pigxAuthRealm);
		bean.setRealms(realms);
		bean.setSessionManager(sessionManager);
		bean.setCacheManager(shiroCacheManager);
		bean.setSubjectFactory(new CasSubjectFactory());
		//bean.setRememberMeManager(null); // ?????? RememberMe
		return bean;
	}
	
	/**
	 * Shiro ??????????????????????????????????????????????????????
	 */
	@Bean(name="lifecycleBeanPostProcessor")
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * Shiro ?????????????????????
	 */
	@Bean
	@DependsOn({ "lifecycleBeanPostProcessor" })
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator bean = new DefaultAdvisorAutoProxyCreator();
		bean.setProxyTargetClass(true);
		return bean;
	}

	/**
	 * ??????Shrio???????????????????????????AOP????????????????????????
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(WebSecurityManager webSecurityManager) {
		AuthorizationAttributeSourceAdvisor bean = new AuthorizationAttributeSourceAdvisor();
		bean.setSecurityManager(webSecurityManager);
		return bean;
	}
	
//	/**
//	 * ???????????? ?????? webSecurityManager ??????????????????
//	 */
//	@Bean
//	public MethodInvokingFactoryBean methodInvokingFactoryBean(DefaultWebSecurityManager webSecurityManager) {
//		MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
//		bean.setStaticMethod("org.apache.shiro.SecurityUtils.setSecurityManager");
//		bean.setArguments(new Object[] { webSecurityManager });
//		return bean;
//	}
	
}

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringSecTest.SecContext.class, SpringSecTest.SecBean.class,
        SpringSecTest.SecBean2.class, SpringSecTest.SecBean3.class})
public class SpringSecTest {

    @Rule
    public CaptureRule capture = new CaptureRule(true);

    @Autowired
    private SecBeanInt bean;

    @Before
    public void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSec() {
        bean.doSomething();
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
    static class SecContext extends SecurityConfigurerAdapter {

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication()
                    .withUser("user")
                    .password("password")
                    .roles("USER");
        }
    }

    interface SecBeanInt {
        @PreAuthorize("hasRole('ROLE_USER')")
        void doSomething();
    }

    static class SecBean implements SecBeanInt {

        @Autowired
        private SecBeanInt2 bean2;

        @Override
        public void doSomething() {
            bean2.doSomething2(new String[]{"nothing"});
        }
    }

    interface SecBeanInt2 {
        @PostAuthorize("returnObject.token == principal.username.toUpperCase()")
        SecuredObject doSomething2(String... something);
    }

    static class SecBean2 implements SecBeanInt2 {
        @Autowired
        private SecBeanInt3 bean3;

        @Override
        public SecuredObject doSomething2(String... something) {
            try {
                bean3.doSomething3(1);
            } catch (AccessDeniedException whatever) {
            }

            return new SecuredObject("USER");
        }
    }

    static class SecuredObject {
        String token;

        SecuredObject(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    interface SecBeanInt3 {
        @Secured("ROLE_ADMIN")
        void doSomething3(int x);
    }

    static class SecBean3 implements SecBeanInt3 {

        public void doSomething3(int x) {
        }
    }

}

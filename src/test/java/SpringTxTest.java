import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringTxTest.TxService.class, SpringTxTest.Config.class,
        SpringTxTest.TxServicePropReuired.class, SpringTxTest.TxServiceNewRequired.class})
public class SpringTxTest {

    @Rule
    public CaptureRule capture = new CaptureRule();

    @Autowired
    private TxIface bean;

    @Test(expected = TxException.class)
    @CaptureRule.CompareTo("spring-tx.json")
    public void testTx() throws IOException {
        bean.doInTx();
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {

        @Bean
        public DataSource dataSource() {
            EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
            return builder.setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        public PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

    }

    static class TxService implements TxIface {
        @Autowired
        private TxIface2 service2;

        @Autowired
        private TxIface3 service3;

        @Override
        @Transactional(noRollbackFor = TxException.class)
        public void doInTx() {
            doSomething();
            service2.doInTx();
            service3.doInTx();
        }

        private void doSomething() {
        }
    }

    interface TxIface {
        void doInTx();
    }

    static class TxServicePropReuired implements TxIface2 {

        @Override
        @Transactional
        public void doInTx() {
            doSomething();
        }

        private void doSomething() {
        }
    }

    interface TxIface2 {
        void doInTx();
    }

    static class TxServiceNewRequired implements TxIface3 {

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void doInTx() {
            doSomething();
            throw new TxException("rollback me");
        }

        private void doSomething() {
        }
    }

    interface TxIface3 {
        void doInTx();
    }

    static class TxException extends RuntimeException {

        public TxException(String msg) {
            super(msg);
        }
    }
}

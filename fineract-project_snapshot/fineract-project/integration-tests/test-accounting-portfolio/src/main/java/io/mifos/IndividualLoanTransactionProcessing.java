/*
 * Copyright 2017 The Mifos Initiative.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mifos;

import com.google.gson.Gson;
import io.mifos.accounting.api.v1.client.LedgerManager;
import io.mifos.accounting.api.v1.domain.Account;
import io.mifos.accounting.api.v1.domain.AccountType;
import io.mifos.accounting.api.v1.domain.Ledger;
import io.mifos.core.api.config.EnableApiFactory;
import io.mifos.core.api.context.AutoUserContext;
import io.mifos.core.api.util.ApiFactory;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import io.mifos.core.test.fixture.mariadb.MariaDBInitializer;
import io.mifos.core.test.listener.EventRecorder;
import io.mifos.core.test.servicestarter.ActiveMQForTest;
import io.mifos.core.test.servicestarter.EurekaForTest;
import io.mifos.core.test.servicestarter.InitializedMicroservice;
import io.mifos.core.test.servicestarter.IntegrationTestEnvironment;
import io.mifos.individuallending.api.v1.domain.product.ProductParameters;
import io.mifos.portfolio.api.v1.client.PortfolioManager;
import io.mifos.portfolio.api.v1.client.ProductDefinitionIncomplete;
import io.mifos.portfolio.api.v1.domain.*;
import io.mifos.portfolio.api.v1.events.EventConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.mifos.accounting.api.v1.EventConstants.POST_ACCOUNT;
import static io.mifos.accounting.api.v1.EventConstants.POST_LEDGER;
import static io.mifos.portfolio.api.v1.events.EventConstants.PUT_PRODUCT;
import static java.math.BigDecimal.ROUND_HALF_EVEN;


@RunWith(SpringRunner.class)
@SpringBootTest()
public class IndividualLoanTransactionProcessing {
  @Configuration
  @ActiveMQForTest.EnableActiveMQListen
  @EnableApiFactory
  @ComponentScan("io.mifos.listener")
  public static class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("test-logger");
    }
  }


  private final static EurekaForTest eurekaForTest = new EurekaForTest();
  private final static ActiveMQForTest activeMQForTest = new ActiveMQForTest();
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static MariaDBInitializer mariaDBInitializer = new MariaDBInitializer();
  private final static IntegrationTestEnvironment integrationTestEnvironment = new IntegrationTestEnvironment(cassandraInitializer, mariaDBInitializer);

  private final static InitializedMicroservice<LedgerManager> thoth = new InitializedMicroservice<>(LedgerManager.class, "accounting", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);
  private final static InitializedMicroservice<PortfolioManager> bastet= new InitializedMicroservice<>(PortfolioManager.class, "portfolio", "0.1.0-BUILD-SNAPSHOT", integrationTestEnvironment);

  @ClassRule
  public static TestRule orderedRules = RuleChain
          .outerRule(eurekaForTest)
          .around(activeMQForTest)
          .around(cassandraInitializer)
          .around(mariaDBInitializer)
          .around(integrationTestEnvironment)
          .around(thoth)
          .around(bastet);

  @Autowired
  private ApiFactory apiFactory;

  @Autowired
  protected EventRecorder eventRecorder;

  public IndividualLoanTransactionProcessing() {
    super();
  }

  @Before
  public void before()
  {
    bastet.setApiFactory(apiFactory);
    thoth.setApiFactory(apiFactory);
  }

  @Test
  public void test() throws InterruptedException {
    try (final AutoUserContext ignored = integrationTestEnvironment.createAutoUserContext("blah")) {
      final List<Pattern> patterns = bastet.api().getAllPatterns();
      Assert.assertTrue(patterns != null);
      Assert.assertTrue(patterns.size() >= 1);
      Assert.assertTrue(patterns.get(0) != null);
      Assert.assertEquals(patterns.get(0).getParameterPackage(), "io.mifos.individuallending.api.v1");

      final Product product = defineProductWithoutAccountAssignments(
              patterns.get(0).getParameterPackage(),
              bastet.getProcessEnvironment().generateUniqueIdentifer("agro"));

      bastet.api().createProduct(product);
      Assert.assertTrue(this.eventRecorder.wait(EventConstants.POST_PRODUCT, product.getIdentifier()));
      final Set<AccountAssignment> incompleteAccountAssignments = bastet.api().getIncompleteAccountAssignments(product.getIdentifier());
      Assert.assertTrue(!incompleteAccountAssignments.isEmpty());
      try {
        bastet.api().enableProduct(product.getIdentifier(), true);
        Assert.fail("Enable shouldn't work without the account assignments.");
      }
      catch (final ProductDefinitionIncomplete ignored2) { }

      final Product changedProduct = bastet.api().getProduct(product.getIdentifier());

      final Ledger ledger = defineLedger(thoth.getProcessEnvironment().generateUniqueIdentifer("001-", 3));
      thoth.api().createLedger(ledger);
      Assert.assertTrue(this.eventRecorder.wait(POST_LEDGER, ledger.getIdentifier()));

      final Set<AccountAssignment> accountAssignments = incompleteAccountAssignments.stream()
              .map(x -> new AccountAssignment(x.getDesignator(), createAccount(ledger).getIdentifier()))
              .collect(Collectors.toSet());
      for (final AccountAssignment accountAssignment : accountAssignments) {
        Assert.assertTrue(this.eventRecorder.wait(POST_ACCOUNT, accountAssignment.getAccountIdentifier()));
      }
      changedProduct.setAccountAssignments(accountAssignments);

      bastet.api().changeProduct(changedProduct.getIdentifier(), changedProduct);
      Assert.assertTrue(this.eventRecorder.wait(PUT_PRODUCT, changedProduct.getIdentifier()));
    }
  }

  private Account createAccount(final Ledger ledger) {
    final Account account = defineAccount(ledger.getIdentifier(), thoth.getProcessEnvironment().generateUniqueIdentifer("001-", 3));

    thoth.api().createAccount(account);
    return account;
  }

  private Ledger defineLedger(final String identifier)
  {
    final Ledger ledger = new Ledger();
    ledger.setIdentifier(identifier);
    ledger.setName("Anyname");
    ledger.setDescription("Anydescription");
    ledger.setType(AccountType.ASSET.name());
    return ledger;
  }

  private Account defineAccount(final String ledgerIdentifier, final String identifier)
  {
    final Account account = new Account();
    account.setIdentifier(identifier);
    account.setLedger(ledgerIdentifier);
    account.setHolders(Collections.singleton("humptyDumpty"));
    account.setState(Account.State.OPEN.name());
    account.setType(AccountType.ASSET.name());
    account.setBalance(0d);
    return account;
  }

  private Product defineProductWithoutAccountAssignments(final String patternPackage, final String identifier) {
    final Product product = new Product();
    product.setIdentifier(identifier);
    product.setPatternPackage(patternPackage);

    product.setName("Agricultural Loan");
    product.setDescription("Loan for seeds or agricultural equipment");
    product.setTermRange(new TermRange(ChronoUnit.MONTHS, 12));
    product.setBalanceRange(new BalanceRange(fixScale(BigDecimal.ZERO), fixScale(new BigDecimal(10000))));
    product.setInterestRange(new InterestRange(BigDecimal.valueOf(3, 2), BigDecimal.valueOf(12, 2)));
    product.setInterestBasis(InterestBasis.CURRENT_BALANCE);

    product.setCurrencyCode("XXX");
    product.setMinorCurrencyUnitDigits(2);

    product.setAccountAssignments(Collections.emptySet());

    final ProductParameters productParameters = new ProductParameters();

    productParameters.setMoratoriums(Collections.emptyList());
    productParameters.setMaximumDispersalCount(5);

    final Gson gson = new Gson();
    product.setParameters(gson.toJson(productParameters));
    return product;
  }

  static public BigDecimal fixScale(final BigDecimal bigDecimal)
  {
    return bigDecimal.setScale(4, ROUND_HALF_EVEN);
  }
}
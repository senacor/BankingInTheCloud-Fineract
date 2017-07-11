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
import io.mifos.anubis.api.v1.client.Anubis;
import io.mifos.anubis.api.v1.domain.ApplicationSignatureSet;
import io.mifos.anubis.api.v1.domain.Signature;
import io.mifos.anubis.example.simple.Example;
import io.mifos.anubis.example.simple.ExampleConfiguration;
import io.mifos.anubis.test.v1.TenantApplicationSecurityEnvironmentTestRule;
import io.mifos.core.api.context.AutoSeshat;
import io.mifos.core.api.util.NotFoundException;
import io.mifos.core.lang.security.RsaKeyPairFactory;
import io.mifos.core.test.env.TestEnvironment;
import io.mifos.core.test.fixture.TenantDataStoreContextTestRule;
import io.mifos.core.test.fixture.cassandra.CassandraInitializer;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author Myrle Krantz
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestAnubisKeyRotation {
  private static final String APP_NAME = "anubis-v1";

  @Configuration
  @EnableFeignClients(basePackages = {"io.mifos.anubis.example.simple"})
  @RibbonClient(name = APP_NAME)
  @Import({ExampleConfiguration.class})
  static public class TestConfiguration {
    public TestConfiguration() {
      super();
    }

    @Bean()
    public Logger logger() {
      return LoggerFactory.getLogger("key-rotation-test-logger");
    }
  }

  private final static TestEnvironment testEnvironment = new TestEnvironment(APP_NAME);
  private final static CassandraInitializer cassandraInitializer = new CassandraInitializer();
  private final static TenantDataStoreContextTestRule tenantDataStoreContext = TenantDataStoreContextTestRule.forRandomTenantName(cassandraInitializer);

  @ClassRule
  public static TestRule orderClassRules = RuleChain
          .outerRule(testEnvironment)
          .around(cassandraInitializer)
          .around(tenantDataStoreContext);

  @Rule
  public final TenantApplicationSecurityEnvironmentTestRule tenantApplicationSecurityEnvironment
          = new TenantApplicationSecurityEnvironmentTestRule(testEnvironment);

  @SuppressWarnings({"SpringAutowiredFieldsWarningInspection", "SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
  @Autowired
  protected Example example;

  @Test
  public void testKeyRotation()
  {
    final Anubis anubis = tenantApplicationSecurityEnvironment.getAnubis();

    final String systemToken = tenantApplicationSecurityEnvironment.getSystemSecurityEnvironment().systemToken(APP_NAME);

    try (final AutoSeshat ignored1 = new AutoSeshat(systemToken)) {
      //Create a signature set then test that it is listed.
      final RsaKeyPairFactory.KeyPairHolder identityManagerKeyPair = RsaKeyPairFactory.createKeyPair();
      final Signature identityManagerSignature = new Signature(identityManagerKeyPair.getPublicKeyMod(), identityManagerKeyPair.getPublicKeyExp());

      anubis.createSignatureSet(identityManagerKeyPair.getTimestamp(), identityManagerSignature);
      {
        final List<String> signatureSets = anubis.getAllSignatureSets();
        Assert.assertTrue(signatureSets.contains(identityManagerKeyPair.getTimestamp()));
      }

      //Get the newly created signature set, and test that its contents are correct.
      final ApplicationSignatureSet signatureSet = anubis.getSignatureSet(identityManagerKeyPair.getTimestamp());
      Assert.assertEquals(identityManagerSignature, signatureSet.getIdentityManagerSignature());

      //Get just the application signature, and test that its contents match the results of the whole signature set.
      final Signature applicationSignature = anubis.getApplicationSignature(identityManagerKeyPair.getTimestamp());
      Assert.assertEquals(signatureSet.getApplicationSignature(), applicationSignature);

      //Create a second signature set and test that it and the previous signature set are listed.
      final RsaKeyPairFactory.KeyPairHolder identityManagerKeyPair2 = RsaKeyPairFactory.createKeyPair();
      final Signature identityManagerSignature2 = new Signature(identityManagerKeyPair2.getPublicKeyMod(), identityManagerKeyPair2.getPublicKeyExp());

      anubis.createSignatureSet(identityManagerKeyPair2.getTimestamp(), identityManagerSignature2);
      {
        final List<String> signatureSets = anubis.getAllSignatureSets();
        Assert.assertTrue(signatureSets.contains(identityManagerKeyPair.getTimestamp()));
        Assert.assertTrue(signatureSets.contains(identityManagerKeyPair2.getTimestamp()));
      }
      final ApplicationSignatureSet latestSignatureSet = anubis.getLatestSignatureSet();
      Assert.assertEquals(identityManagerKeyPair2.getTimestamp(), latestSignatureSet.getTimestamp());

      final Signature latestApplicationSignature = anubis.getLatestApplicationSignature();
      Assert.assertEquals(latestSignatureSet.getApplicationSignature(), latestApplicationSignature);

      //Get the newly created signature set, and test that it's contents are correct.
      final ApplicationSignatureSet signatureSet2 = anubis.getSignatureSet(identityManagerKeyPair2.getTimestamp());
      Assert.assertEquals(identityManagerSignature2, signatureSet2.getIdentityManagerSignature());

      //Delete one of the signature sets and test that it is no longer listed.
      anubis.deleteSignatureSet(identityManagerKeyPair.getTimestamp());
      {
        final List<String> signatureSets = anubis.getAllSignatureSets();
        Assert.assertFalse(signatureSets.contains(identityManagerKeyPair.getTimestamp()));
      }

      //Getting the newly deleted signature set should fail.
      try {
        anubis.getSignatureSet(identityManagerKeyPair.getTimestamp());
        Assert.fail("Not found exception should be thrown.");
      } catch (final NotFoundException ignored) {
      }

      //Getting the newly deleted application signature set should likewise fail.
      try {
        anubis.getApplicationSignature(identityManagerKeyPair.getTimestamp());
        Assert.fail("Not found exception should be thrown.");
      } catch (final NotFoundException ignored) {
      }
    }
  }
}

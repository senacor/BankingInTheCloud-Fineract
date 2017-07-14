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
package io.mifos.anubis.security;

import io.mifos.anubis.api.v1.domain.AllowedOperation;
import io.mifos.core.api.util.ApiConstants;
import io.mifos.core.lang.ApplicationName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.when;

/**
 * @author Myrle Krantz
 */
@RunWith(Parameterized.class)
public class ApplicationPermissionTest {

  private static class TestCase
  {
    private final String caseName;
    private String permittedPath = "/heart";
    private AllowedOperation allowedOperation = AllowedOperation.READ;
    private boolean acceptTokenIntendedForForeignApplication = false;
    private String calledApplication = "graincounter-v1";
    private String requestedPath = "/heart";
    private String requestedOperation = "GET";
    private String user = "Nebamun";
    private String forApplication = "graincounter-v1";
    private String sourceApplication = "identity-v1";
    private boolean expectedResult = true;

    private TestCase(final String caseName) {
      this.caseName = caseName;
    }

    String getPermittedPath() {
      return permittedPath;
    }

    TestCase permittedPath(String permittedPath) {
      this.permittedPath = permittedPath;
      return this;
    }

    AllowedOperation getAllowedOperation() {
      return allowedOperation;
    }

    TestCase allowedOperation(AllowedOperation allowedOperation) {
      this.allowedOperation = allowedOperation;
      return this;
    }

    boolean isAcceptTokenIntendedForForeignApplication() {
      return acceptTokenIntendedForForeignApplication;
    }

    TestCase acceptTokenIntendedForForeignApplication(boolean newVal) {
      this.acceptTokenIntendedForForeignApplication = newVal;
      return this;
    }

    ApplicationName getCalledApplication() {
      return ApplicationName.fromSpringApplicationName(calledApplication);
    }

    TestCase calledApplication(final String newVal)
    {
      this.calledApplication = newVal;
      return this;
    }


    String getRequestedPath() {
      return requestedPath;
    }

    TestCase requestedPath(String requestedPath) {
      this.requestedPath = requestedPath;
      return this;
    }

    String getRequestedOperation() {
      return requestedOperation;
    }

    TestCase requestedOperation(String requestedOperation) {
      this.requestedOperation = requestedOperation;
      return this;
    }

    AnubisPrincipal getPrincipal() {
      return new AnubisPrincipal(user, forApplication, sourceApplication);
    }

    TestCase user(final String newVal)
    {
      this.user = newVal;
      return this;
    }

    TestCase forApplication(final String newVal)
    {
      this.forApplication = newVal;
      return this;
    }

    TestCase sourceApplication(final String newVal)
    {
      this.sourceApplication = newVal;
      return this;
    }

    boolean getExpectedResult() {
      return expectedResult;
    }

    TestCase expectedResult(boolean expectedResult) {
      this.expectedResult = expectedResult;
      return this;
    }

    @Override public String toString() {
      return caseName;
    }
  }

  private final TestCase testCase;

  public ApplicationPermissionTest(final TestCase testCase) {
    this.testCase = testCase;
  }

  @Parameterized.Parameters
  public static Collection testCases() {
    final Collection<TestCase> ret = new ArrayList<>();

    ret.add(new TestCase("Happy case"));
    ret.add(new TestCase("Different operation").requestedOperation("DELETE").expectedResult(false));
    ret.add(new TestCase("Delete operation").allowedOperation(AllowedOperation.DELETE).requestedOperation("DELETE").expectedResult(true));
    ret.add(new TestCase("Different path requested").requestedPath("/soul").expectedResult(false));
    ret.add(new TestCase("* in request").requestedPath("/heart/*").expectedResult(false));
    ret.add(new TestCase("* in permission pattern matching").permittedPath("/heart/*").expectedResult(true));
    ret.add(new TestCase("safe").permittedPath("/w-x+y.z/").requestedPath("/w-x+y.z/").expectedResult(true));
    ret.add(new TestCase("escape").permittedPath("/%f005/").requestedPath("/%f005/").expectedResult(true));
    ret.add(new TestCase("digit")
        .permittedPath("/555/555").requestedPath("/555/555")
        .expectedResult(true));
    ret.add(new TestCase("multiple end segments matched to *")
        .permittedPath("/heart/*").requestedPath("/heart/soul/body")
        .expectedResult(true));
    ret.add(new TestCase("* in the middle")
        .permittedPath("/*/heart").requestedPath("/soul/heart")
        .expectedResult(true));
    ret.add(new TestCase("{useridentifier} in the middle")
        .permittedPath("/{useridentifier}/ka").requestedPath("/Nebamun/ka")
        .expectedResult(true));
    ret.add(new TestCase("wrong {useridentifier} in request")
        .permittedPath("/{useridentifier}/ka").requestedPath("/Menna/ka")
        .expectedResult(false));
    ret.add(new TestCase("{useridentifier} and *")
        .permittedPath("/{useridentifier}/*").requestedPath("/Nebamun/ka/arua")
        .expectedResult(true));
    ret.add(new TestCase("{parameter} with su").user(ApiConstants.SYSTEM_SU)
        .permittedPath("/{parameter}/").requestedPath("/value")
        .expectedResult(true));
    ret.add(new TestCase("{parameter} without su")
         .permittedPath("/{parameter}/").requestedPath("/value")
         .expectedResult(false));
    ret.add(new TestCase("* at end with request containing more segments")
         .permittedPath("/roles/*").requestedPath("/users/antony/password")
         .expectedResult(false));
    ret.add(new TestCase("* at end with request containing same # segments")
            .permittedPath("/x/y/z/*").requestedPath("/m/n/o/")
            .expectedResult(false));
    ret.add(new TestCase("{applicationidentifier} but permission doesn't allow foreign forApplication")
            .permittedPath("/m/{applicationidentifier}/o").requestedPath("/m/bcde-v1/o/")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("abcd-v1").forApplication("bcde-v1")
            .expectedResult(false));
    ret.add(new TestCase("{applicationidentifier} and permission does allow foreign forApplication")
            .permittedPath("/m/{applicationidentifier}/o").requestedPath("/m/bcde-v1/o/")
            .acceptTokenIntendedForForeignApplication(true)
            .calledApplication("abcd-v1").forApplication("bcde-v1")
            .expectedResult(true));
    ret.add(new TestCase("No {applicationidentifier} even though permission does allow foreign forApplication")
            .permittedPath("/m/n/o").requestedPath("/m/bcde-v1/o/")
            .acceptTokenIntendedForForeignApplication(true)
            .calledApplication("abcd-v1").forApplication("bcde-v1")
            .expectedResult(false));
    ret.add(new TestCase("{applicationidentifier} and permission does allow foreign forApplication, but application isn't foreign.")
            .permittedPath("/m/{applicationidentifier}/o").requestedPath("/m/abcd-v1/o/")
            .acceptTokenIntendedForForeignApplication(true)
            .calledApplication("abcd-v1").forApplication("abcd-v1")
            .expectedResult(true));
    ret.add(new TestCase("initialize")
            .permittedPath("/initialize").requestedPath("/initialize")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("abcd-v1").forApplication("abcd-v1")
            .allowedOperation(AllowedOperation.CHANGE)
            .requestedOperation("POST")
            .expectedResult(true));
    ret.add(new TestCase("use case from identity")
            .permittedPath("/applications/*/permissions/*/users/{useridentifier}/enabled")
            .requestedPath("/applications/bop-v1/permissions/identity__v1__roles/users/Nebamun/enabled")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("identity-v1").forApplication("identity-v1")
            .allowedOperation(AllowedOperation.CHANGE)
            .requestedOperation("PUT")
            .expectedResult(true));
    ret.add(new TestCase("access token acquired from application refresh token accessing its own resource.")
            .permittedPath("/applications/{applicationidentifier}/permissions")
            .requestedPath("/applications/bop-v1/permissions")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("identity-v1").forApplication("identity-v1")
            .sourceApplication("bop-v1")
            .allowedOperation(AllowedOperation.CHANGE)
            .requestedOperation("POST")
            .expectedResult(true));
    ret.add(new TestCase("access token acquired from application refresh token accessing another apps resource.")
            .permittedPath("/applications/{applicationidentifier}/permissions")
            .requestedPath("/applications/bop-v1/permissions")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("identity-v1").forApplication("identity-v1")
            .sourceApplication("bee-v1")
            .allowedOperation(AllowedOperation.CHANGE)
            .requestedOperation("POST")
            .expectedResult(false));
    ret.add(new TestCase("access token acquired from application refresh token accessing sub resource not allowed.")
            .permittedPath("/applications/{applicationidentifier}/permissions")
            .requestedPath("/applications/bop-v1/permissions/identity__v1__roles/users/Nebamun/enabled")
            .acceptTokenIntendedForForeignApplication(false)
            .calledApplication("identity-v1").forApplication("identity-v1")
            .sourceApplication("bop-v1")
            .allowedOperation(AllowedOperation.CHANGE)
            .requestedOperation("PUT")
            .expectedResult(false));

    return ret;
  }

  @Test public void test() {
    final ApplicationPermission testSubject =
        new ApplicationPermission(testCase.getPermittedPath(), testCase.getAllowedOperation(), testCase.isAcceptTokenIntendedForForeignApplication());

    final HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
    when(requestMock.getServletPath()).thenReturn(testCase.getRequestedPath());
    when(requestMock.getMethod()).thenReturn(testCase.getRequestedOperation());

    final boolean matches = testSubject.matches(requestMock, testCase.getCalledApplication(), testCase.getPrincipal());

    Assert.assertEquals("Testcase gave wrong result: '" + testCase.toString() + "'",
        testCase.getExpectedResult(), matches);
    Assert.assertEquals("Testcase contains wrong allowed operation: '" + testCase.toString() + "'",
        testCase.getAllowedOperation(), testSubject.getAllowedOperation());
    Assert.assertEquals("Testcase contains wrong authority: '" + testCase.toString() + "'",
        "maats_feather", testSubject.getAuthority());
  }
}

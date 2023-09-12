/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.enforcer.rules.dependency;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The Class RequireDependenciesTest.
 *
 * @author <a href="https://github.com/Aresxue">Ares</a>
 */
@ExtendWith(MockitoExtension.class)
public class RequireDependenciesTest {

  private static final ArtifactStubFactory ARTIFACT_STUB_FACTORY = new ArtifactStubFactory();

  @Mock
  private MavenProject project;

  @Mock
  private MavenSession session;

  @Mock
  private ResolverUtil resolverUtil;

  @InjectMocks
  private RequireDependencies rule;

  @Test
  void includes() throws Exception {
    when(session.getCurrentProject()).thenReturn(project);
    when(project.getDependencyArtifacts()).thenReturn(ARTIFACT_STUB_FACTORY.getScopedArtifacts());

    rule.setSearchTransitive(false);
    List<String> includeList = new ArrayList<>();
    includeList.add("g:runtime-not-exist:*");
    rule.setIncludes(includeList);
    assertThatCode(rule::execute).isInstanceOf(EnforcerRuleException.class)
        .hasMessageContaining("Not found Require Dependency: g:runtime-not-exist:*");

    includeList.clear();
    includeList.add("g:runtime:*");
    assertThatCode(rule::execute).doesNotThrowAnyException();
  }

}

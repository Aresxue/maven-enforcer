package org.apache.maven.enforcer.rules.dependency;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rules.AbstractStandardEnforcerRule;
import org.apache.maven.enforcer.rules.utils.ArtifactUtils;
import org.apache.maven.execution.MavenSession;
import org.eclipse.aether.graph.DependencyNode;

/**
 * This rule checks require dependencies are included.
 *
 * @author <a href="https://github.com/Aresxue">Ares</a>
 */
@Named("requireDependencies")
public class RequireDependencies extends AbstractStandardEnforcerRule {

  /**
   * Specify the banned dependencies. This can be a list of artifacts in the format
   * <code>groupId[:artifactId][:version]</code>. Any of the sections can be a wildcard
   * by using '*' (ie group:*:1.0) <br> The rule will fail if any dependency matches any exclude,
   * unless it also matches an include rule.
   *
   * @see #setExcludes(List)
   * @see #getExcludes()
   */
  private List<String> excludes = null;

  /**
   * Specify the allowed dependencies. This can be a list of artifacts in the format
   * <code>groupId[:artifactId][:version]</code>. Any of the sections can be a wildcard
   * by using '*' (ie group:*:1.0) <br> Includes override the exclude rules. It is meant to allow
   * wide exclusion rules with wildcards and still allow a smaller set of includes. <br> For
   * example, to ban all xerces except xerces-api -&gt; exclude "xerces", include
   * "xerces:xerces-api"
   *
   * @see #setIncludes(List)
   * @see #getIncludes()
   */
  private List<String> includes = null;

  /**
   * Specify if transitive dependencies should be searched (default) or only look at direct
   * dependencies.
   */
  private boolean searchTransitive = true;

  private final MavenSession session;

  private final ResolverUtil resolverUtil;

  @Inject
  RequireDependencies(MavenSession session, ResolverUtil resolverUtil) {
    this.session = Objects.requireNonNull(session);
    this.resolverUtil = Objects.requireNonNull(resolverUtil);
  }

  @Override
  public void execute() throws EnforcerRuleException {
    StringBuilder messageBuilder = new StringBuilder();

    List<String> includeList = getIncludes();
    List<String> excludeList = getExcludes();
    if (isNotEmpty(includeList)) {
      List<String> newIncludeList = new ArrayList<>(includeList);
      if (searchTransitive) {
        DependencyNode rootNode = resolverUtil.resolveTransitiveDependenciesVerbose(
            Collections.emptyList());
        validate(rootNode, newIncludeList, excludeList);
      } else {
        session.getCurrentProject().getDependencyArtifacts()
            .forEach(artifact -> validate(artifact, newIncludeList, excludeList));
      }

      if (isNotEmpty(newIncludeList)) {
        includeList.forEach(include -> messageBuilder.append(getErrorMessage(include)));
        String message = "";
        if (getMessage() != null) {
          message = getMessage() + System.lineSeparator();
        }
        throw new EnforcerRuleException(message + messageBuilder);
      }
    }

  }

  protected String getErrorMessage(String include) {
    return "Not found Require Dependency: " + include + System.lineSeparator();
  }

  private void validate(DependencyNode node, List<String> includeList, List<String> excludeList) {
    Artifact artifact = ArtifactUtils.toArtifact(node);
    if (validate(artifact, includeList, excludeList)) {
      return;
    }
    List<DependencyNode> dependencyNodeList = node.getChildren();
    if (isNotEmpty(dependencyNodeList)) {
      for (DependencyNode childNode : dependencyNodeList) {
        if (!isNotEmpty(includeList)) {
          break;
        }
        validate(childNode, includeList, excludeList);
      }
    }
  }

  private boolean validate(Artifact artifact, List<String> includeList, List<String> excludeList) {
    if (ArtifactUtils.matchDependencyArtifact(artifact, excludeList)) {
      return true;
    }
    includeList.removeIf(include -> ArtifactUtils.compareDependency(include, artifact));
    return false;
  }

  private boolean isNotEmpty(List<?> includeList) {
    return null != includeList && !includeList.isEmpty();
  }

  public List<String> getExcludes() {
    return excludes;
  }

  public void setExcludes(List<String> excludes) {
    this.excludes = excludes;
  }

  public List<String> getIncludes() {
    return includes;
  }

  public void setIncludes(List<String> includes) {
    this.includes = includes;
  }

  public boolean isSearchTransitive() {
    return searchTransitive;
  }

  public void setSearchTransitive(boolean searchTransitive) {
    this.searchTransitive = searchTransitive;
  }

  public MavenSession getSession() {
    return session;
  }

}

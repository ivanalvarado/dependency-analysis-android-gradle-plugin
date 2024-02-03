package com.autonomousapps

import com.autonomousapps.extension.AbiHandler
import com.autonomousapps.extension.DependenciesHandler
import com.autonomousapps.extension.IssueHandler
import com.autonomousapps.extension.ProjectHandler
import com.autonomousapps.extension.ProjectIssueHandler
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.naming.OperationNotSupportedException

/**
 * See also [ProjectIssueHandler]. Note that this differs from [DependencyAnalysisExtension], in that you cannot specify
 * the project being configured, as it is _this_ project being configured.
 *
 * ```
 * dependencyAnalysis {
 *   // Configure the severity of issues, and exclusion rules, for this project.
 *   issues {
 *     ignoreKtx(<true|false>)
 *     onAny { ... }
 *     onUnusedDependencies { ... }
 *     onUsedTransitiveDependencies { ... }
 *     onIncorrectConfiguration { ... }
 *     onCompileOnly { ... }
 *     onUnusedAnnotationProcessors { ... }
 *     onRedundantPlugins { ... }
 *   }
 * }
 * ```
 */
open class DependencyAnalysisSubExtension(
  objects: ObjectFactory,
  private val rootExtProvider: () -> DependencyAnalysisExtension,
  private val path: String
) : AbstractExtension(objects) {

  override val issueHandler: IssueHandler by lazy {
    rootExtProvider().issueHandler
  }

  override val abiHandler: AbiHandler by lazy {
    rootExtProvider().abiHandler
  }

  val projectProperties: ProjectHandler by lazy {
    rootExtProvider().projectHandler
  }

  fun issues(action: Action<ProjectIssueHandler>) {
    issueHandler.project(path, action)
  }

  fun abi(action: Action<AbiHandler>) {
    action.execute(abiHandler)
  }

  fun projectProperties(action: Action<ProjectHandler>) {
    action.execute(projectProperties)
  }

  @Suppress("UNUSED_PARAMETER")
  fun dependencies(action: Action<DependenciesHandler>) {
    throw OperationNotSupportedException("Dependency bundles must be declared in the root project only")
  }
}

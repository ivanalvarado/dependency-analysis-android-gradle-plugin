package com.autonomousapps.jvm.projects

import com.autonomousapps.AbstractProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.Source
import com.autonomousapps.model.ProjectAdvice

import static com.autonomousapps.AdviceHelper.actualProjectAdvice
import static com.autonomousapps.AdviceHelper.emptyProjectAdviceFor
import static com.autonomousapps.kit.gradle.Dependency.implementation
import static com.autonomousapps.kit.gradle.Dependency.testFixturesImplementation

abstract class Kotlin2Migration extends AbstractProject {

  // latest stable Kotlin 2.0.x at time of writing
  protected static final String KOTLIN_2 = '2.0.21'

  Kotlin2Migration(String kotlinVersion) {
    super(kotlinVersion, null)
  }

  protected List<Source> sourcesConsumer = [
    Source.kotlin(
      '''\
          package com.example.consumer
          
          import com.example.producer.Simpsons.HOMER

          class Consumer {
            private val homer = HOMER
          }
        '''
    )
      .withPath('com.example.consumer', 'Consumer')
      .build(),
    Source.kotlin(
      '''\
          package com.example.consumer
          
          import com.example.producer.Person

          class TestFixture {
            private val testPerson = Person("Emma", "Goldman")
          }
        '''
    )
      .withPath('com.example.consumer', 'TestFixture')
      .withSourceSet('testFixtures')
      .build(),
  ]

  protected List<Source> sourcesProducer = [
    Source.kotlin(
      '''\
          package com.example.producer

          data class Person(val firstName: String, val lastName: String)
        '''
    )
      .withPath('com.example.producer', 'Person')
      .build(),
    Source.kotlin(
      '''\
          package com.example.producer

          object Simpsons {
            val HOMER = Person("Homer", "Simpson")
          }
        '''
    )
      .withPath('com.example.producer', 'Simpsons')
      .withSourceSet('testFixtures')
      .build(),
  ]
  
  static final class CompilesWithTestFixturesDependency extends Kotlin2Migration {

    final GradleProject gradleProject

    CompilesWithTestFixturesDependency() {
      super(KOTLIN_2)
      gradleProject = build()
    }

    private GradleProject build() {
      return newGradleProjectBuilder()
        .withSubproject('consumer') { s ->
          s.sources = sourcesConsumer
          s.withBuildScript { bs ->
            bs.plugins = kotlin + plugins.javaTestFixtures
            bs.dependencies(
              implementation(':producer'),
              implementation(':producer').onTestFixtures(),
              testFixturesImplementation(':producer'),
            )
          }
        }
        .withSubproject('producer') { s ->
          s.sources = sourcesProducer
          s.withBuildScript { bs ->
            bs.plugins = kotlin + plugins.javaTestFixtures
          }
        }
        .write()
    }

    Set<ProjectAdvice> actualBuildHealth() {
      return actualProjectAdvice(gradleProject)
    }

    final Set<ProjectAdvice> expectedBuildHealth = [
      emptyProjectAdviceFor(':consumer'),
      emptyProjectAdviceFor(':producer'),
    ]
  }

  static final class CompilationFailsWithoutTestFixturesDependencies extends Kotlin2Migration {

    final GradleProject gradleProject

    CompilationFailsWithoutTestFixturesDependencies() {
      super(KOTLIN_2)
      gradleProject = build()
    }

    private GradleProject build() {
      return newGradleProjectBuilder()
        .withSubproject('consumer') { s ->
          s.sources = sourcesConsumer
          s.withBuildScript { bs ->
            bs.plugins = kotlin + plugins.javaTestFixtures
            bs.dependencies(
              implementation(':producer'),
              implementation(':producer').onTestFixtures(),
            )
          }
        }
        .withSubproject('producer') { s ->
          s.sources = sourcesProducer
          s.withBuildScript { bs ->
            bs.plugins = kotlin + plugins.javaTestFixtures
          }
        }
        .write()
    }
  }
}

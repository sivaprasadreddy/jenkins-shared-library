# jenkins-java-shared-library
jenkins-java-shared-library

## Usage

### Declarative Pipeline

**Jenkinsfile**

```groovy
#!groovy
@Library('jenkins-java-shared-library')
import com.sivalabs.JenkinsJavaLib

def utils = new JenkinsJavaLib(this, env, scm, currentBuild)

pipeline {
    agent any

    stages {
        stage("Checkout") {
            steps {
                script {
                    utils.checkout()
                }
            }
        }
        stage("Test") {
            steps {
                script {
                    utils.runMavenTests()
                }
            }
        }
    }
}
```

### Scripted Pipeline

**Jenkinsfile**

```groovy
#!groovy
@Library('jenkins-java-shared-library')
import com.sivalabs.JenkinsSharedLib

def utils = new JenkinsSharedLib(this, env, scm, currentBuild)

node {

    try {
        utils.checkout()
        utils.runMavenTests()
    }
    catch(err) {
        echo "ERROR: ${err}"
        currentBuild.result = currentBuild.result ?: "FAILURE"
    }
}
```

### Dynamically loading library

**Jenkinsfile**

```groovy
#!groovy

jsl = library(
    identifier: 'jenkins-java-shared-library@master',
    retriever: modernSCM(
        [
            $class: 'GitSCMSource',
            remote: 'https://github.com/sivaprasadreddy/jenkins-java-shared-library.git'
        ]
    )
)

def utils = jsl.com.sivalabs.JenkinsSharedLib.new(this, env, scm, currentBuild)

node {

    try {
        utils.checkout()
        utils.runMavenTests()
    }
    catch(err) {
        echo "ERROR: ${err}"
        currentBuild.result = currentBuild.result ?: "FAILURE"
    }
}
```

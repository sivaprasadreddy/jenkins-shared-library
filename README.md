# jenkins-shared-library

## Usage

### Configure Global Shared Library

* Go to **Manage Jenkins -> Configure System**
* In **Global Pipeline Libraries** Section
    * Library Name: jenkins-shared-library
    * Default version: master
    * Check "Allow default version to be overridden", "Include @Library changes in job recent changes"
    * Retrieval method: "Modern SCM"
    * Source Code Management: Git, Project Repository: "https://github.com/sivaprasadreddy/jenkins-shared-library.git"
    
### Scripted Pipeline

**Jenkinsfile**

```groovy
#!groovy
@Library('jenkins-shared-library')
import com.sivalabs.JenkinsMavenLib
import com.sivalabs.JenkinsGradleLib

def dockerUsername = 'DOCKER_USERNAME'
def dockerImageName = 'IMAGE_NAME'

def project = new JenkinsMavenLib(this, scm, env, params, currentBuild)
//def project = new JenkinsGradleLib(this, scm, env, params, currentBuild)

node {

    try {
        stage("Checkout") {
            project.checkout()
        }
        stage("Build") {
            project.runTests()
        }
        stage("Publish Docker Image") {
            project.buildSpringBootDockerImage(dockerUsername, dockerImageName)
            def tags = []
            if(env.BRANCH_NAME == 'master' || env.BRANCH_NAME == 'main') {
                tags << "latest"
            }
            project.publishDockerImage(dockerUsername, dockerImageName, tags)
        }
    }
    catch(err) {
        echo "ERROR: ${err}"
        currentBuild.result = currentBuild.result ?: "FAILURE"
    }
}
```

### Declarative Pipeline

**Jenkinsfile**

```groovy
#!groovy
@Library('jenkins-shared-library')
import com.sivalabs.JenkinsMavenLib
//import com.sivalabs.JenkinsGradleLib

def project = new JenkinsMavenLib(this, scm, env, params, currentBuild)
//def project = new JenkinsGradleLib(this, scm, env, params, currentBuild)

pipeline {
    agent any

    stages {
        stage("Checkout") {
            steps {
                script {
                    project.checkout()
                }
            }
        }
        stage("Test") {
            steps {
                script {
                    project.runTests()
                }
            }
        }
    }
}
```

### Dynamically loading library

**Jenkinsfile**

```groovy
#!groovy

jsl = library(
    identifier: 'jenkins-shared-library@master',
    retriever: modernSCM(
        [
            $class: 'GitSCMSource',
            remote: 'https://github.com/sivaprasadreddy/jenkins-shared-library.git'
        ]
    )
)

def project = jsl.com.sivalabs JenkinsMavenLib.new(this, scm, env, params, currentBuild)

node {

    try {
        stage("Checkout") {
            project.checkout()
        }
        stage("Build") {
            project.runTests()
        }
    }
    catch(err) {
        echo "ERROR: ${err}"
        currentBuild.result = currentBuild.result ?: "FAILURE"
    }
}
```

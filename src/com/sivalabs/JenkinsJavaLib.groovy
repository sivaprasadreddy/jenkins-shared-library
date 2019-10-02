package com.sivalabs

class JenkinsJavaLib implements Serializable {

    // pipeline global properties
    def steps
    def env
    def scm
    def currentBuild

    JenkinsJavaLib(steps, env, scm, currentBuild) {
        this.steps = steps
        this.env = env
        this.scm = scm
        this.currentBuild = currentBuild
    }

    def checkout() {
        steps.checkout scm
    }
    
    def runMavenTests() {
        try {
            steps.sh './mvnw clean verify'
        } finally {
            steps.junit 'target/surefire-reports/*.xml'
            steps.junit 'target/failsafe-reports/*.xml'
        }
    }
}

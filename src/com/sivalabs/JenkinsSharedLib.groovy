package com.sivalabs

class JenkinsSharedLib implements Serializable {

    // pipeline global properties
    def steps
    def env
    def scm
    def currentBuild

    JenkinsSharedLib(steps, env, scm, currentBuild) {
        this.steps = steps
        this.env = env
        this.scm = scm
        this.currentBuild = currentBuild
    }

    def checkout() {
        steps.stage("Checkout") {
            steps.checkout scm
        }
    }

    def runMavenTests() {
        steps.stage("Test") {
            try {
                steps.sh './mvnw clean verify'
            } finally {
                steps.junit 'target/surefire-reports/*.xml'
                steps.junit 'target/failsafe-reports/*.xml'
            }
        }
    }
}

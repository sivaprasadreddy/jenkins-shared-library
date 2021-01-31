package com.sivalabs

class JenkinsGradleLib implements Serializable {

    // pipeline global properties
    def steps
    def env
    def params
    def scm
    def currentBuild

    JenkinsGradleLib(steps, scm, env, params, currentBuild) {
        this.steps = steps
        this.scm = scm
        this.env = env
        this.params = params
        this.currentBuild = currentBuild
    }

    def checkout() {
        steps.checkout scm
    }

    def runTests() {
        try {
            steps.sh './gradlew clean build'
        } finally {
            steps.junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
            steps.junit allowEmptyResults: true, testResults: 'build/test-results/integrationTest/*.xml'
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/reports/jacoco/test/html',
                    reportFiles: 'index.html',
                    reportName: "Jacoco Unit Test Report"
            ])
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/reports/jacoco/integrationTest/html',
                    reportFiles: 'index.html',
                    reportName: "Jacoco Integration Test Report"
            ])
        }
    }

    def buildSpringBootDockerImage(dockerUsername, dockerImageName) {
        steps.sh "./gradlew bootBuildImage --imageName=${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER}"
    }

    def buildDockerImageFromDockerfile(dockerUsername, dockerImageName) {
        steps.docker.build("${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER}")
    }

    def publishDockerImage(dockerUsername, dockerImageName, additionalTags = []) {
        steps.docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials') {
            def appImage = steps.docker.image("${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER}")
            appImage.push()
            additionalTags.each {
                appImage.push("$it")
            }
        }
    }

    def runOWASPChecks() {
        try {
            steps.sh './gradlew dependencyCheckAnalyze'
        } finally {
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'build/reports',
                    reportFiles: 'dependency-check-report.html',
                    reportName: "OWASP Dependency Check Report"
            ])
        }
    }
}

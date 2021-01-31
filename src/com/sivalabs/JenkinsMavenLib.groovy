package com.sivalabs

class JenkinsMavenLib implements Serializable {

    // pipeline global properties
    def steps
    def env
    def params
    def scm
    def currentBuild

    JenkinsMavenLib(steps, scm, env, params, currentBuild) {
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
            steps.sh './mvnw clean verify -Pci'
        } finally {
            steps.junit allowEmptyResults: true, testResults: 'target/test-results/test/*.xml'
            steps.junit allowEmptyResults: true, testResults: 'target/test-results/integrationTest/*.xml'
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/jacoco/test',
                    reportFiles: 'index.html',
                    reportName: "Jacoco Unit Test Report"
            ])
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target/jacoco/integrationTest',
                    reportFiles: 'index.html',
                    reportName: "Jacoco Integration Test Report"
            ])
        }
    }

    def buildSpringBootDockerImage(dockerUsername, dockerImageName) {
        steps.sh "./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER}"
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
            steps.sh './mvnw dependency-check:check'
        } finally {
            steps.publishHTML(target:[
                    allowMissing: true,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target',
                    reportFiles: 'dependency-check-report.html',
                    reportName: "OWASP Dependency Check Report"
            ])
        }
    }
}

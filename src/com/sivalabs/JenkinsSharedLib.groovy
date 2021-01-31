package com.sivalabs

class JenkinsSharedLib implements Serializable {

    // pipeline global properties
    def steps
    def env
    def params
    def scm
    def currentBuild

    JenkinsSharedLib(steps, env, params, scm, currentBuild) {
        this.steps = steps
        this.env = env
        this.params = params
        this.scm = scm
        this.currentBuild = currentBuild
    }

    def checkout() {
        steps.stage("Checkout") {
            steps.checkout scm
        }
    }

    def runMavenTests(stageName = "Test") {
        steps.stage(stageName) {
            try {
                steps.sh './mvnw clean verify'
            } finally {
                steps.junit 'target/surefire-reports/*.xml'
                steps.junit 'target/failsafe-reports/*.xml'
                steps.publishHTML(target:[
                        allowMissing: true,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco-aggregate',
                        reportFiles: 'index.html',
                        reportName: "Jacoco Report"
                ])
            }
        }
    }

    def runMavenGatlingTests(stageName = "Performance Test") {
        steps.echo "RUN_PERF_TESTS: ${params.RUN_PERF_TESTS}"
        if(params.RUN_PERF_TESTS) {
            steps.stage(stageName) {
                try {
                    steps.sh './mvnw clean gatling:test'
                } finally {
                    steps.gatlingArchive()
                    /*
                steps.publishHTML(target:[
                     allowMissing: true,
                     alwaysLinkToLastBuild: true,
                     keepAll: true,
                     reportDir: 'target/gatling/results/',
                     reportFiles: 'index.html',
                     reportName: "Gatling Report"
                ])
                */
                }
            }
        }
    }

    def runOWASPChecks(stageName = "OWASP Checks") {
        steps.stage(stageName) {
            try {
                steps.sh './mvnw dependency-check:check -Pci'
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

    def deployOnHeroku(stageName = "Heroku Deployment") {
        steps.echo "DEPLOY_ON_HEROKU: ${params.DEPLOY_ON_HEROKU}"
        if(params.DEPLOY_ON_HEROKU) {
            steps.stage(stageName) {
                steps.withCredentials([steps.string(credentialsId: 'HEROKU_API_KEY', variable: 'HEROKU_API_KEY')]) {
                    steps.sh "HEROKU_API_KEY=\"${env.HEROKU_API_KEY}\" ./mvnw heroku:deploy -P ci"
                }
            }
        }
    }

    def publishDockerImage(stageName = "Publish Docker Image", dockerUsername, dockerImageName) {
        steps.stage(stageName) {
            steps.echo "PUBLISH_TO_DOCKERHUB: ${params.PUBLISH_TO_DOCKERHUB}"
            if(params.PUBLISH_TO_DOCKERHUB) {
                steps.echo "Publishing to dockerhub. DOCKER_USERNAME=${dockerUsername}, APPLICATION_NAME=${dockerImageName}"
                steps.sh "docker build -t ${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER} -t ${dockerUsername}/${dockerImageName}:latest ."

                steps.withCredentials([[$class: 'UsernamePasswordMultiBinding',
                                  credentialsId: 'docker-hub-credentials',
                                  usernameVariable: 'DOCKERHUB_USERNAME', passwordVariable: 'DOCKERHUB_PASSWORD']]) {
                    steps.sh "docker login --username ${env.DOCKERHUB_USERNAME} --password ${env.DOCKERHUB_PASSWORD}"
                }
                steps.sh "docker push ${dockerUsername}/${dockerImageName}:latest"
                steps.sh "docker push ${dockerUsername}/${dockerImageName}:${env.BUILD_NUMBER}"
            } else {
                steps.echo "Skipping Publish Docker Image"
            }
        }
    }

    def npmBuild(stageName = "NPM Build") {
        steps.stage(stageName) {
            steps.sh 'npm install'
            steps.sh 'npm run build'
        }
    }

    def npmTest(stageName = "NPM Test") {
        steps.stage(stageName) {
            steps.sh 'npm run test:ci'
        }
    }
}

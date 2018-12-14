package com.sivalabs

class JenkinsJavaLib {
    def runMavenTests() {
        try {
            sh './mvnw clean verify'
        } finally {
            junit 'target/surefire-reports/*.xml'
            junit 'target/failsafe-reports/*.xml'
        }
    }
}
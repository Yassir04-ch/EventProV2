pipeline {
    agent any

    environment {
        JAVA_HOME = '/opt/java17'
        PATH = "/opt/java17/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'EventPro repository cloned successfully!'
            }
        }

        stage('Java Version') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('Build & Tests') {
            steps {
                sh 'mvn clean verify'
            }
        }
    }
}

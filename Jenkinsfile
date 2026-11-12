pipeline {
    agent any

    environment {
        JAVA_HOME = '/opt/java17'
        PATH = "/opt/java17/bin:${env.PATH}"

        SPRING_DATASOURCE_URL = 'jdbc:postgresql://postgres:5432/eventpro_db'
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

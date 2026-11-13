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

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube-EventPro') {
                    sh '''
                        mvn org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar \
                        -Dsonar.projectKey=EventPro \
                        -Dsonar.projectName=EventPro \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                 timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                 }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -f Dockerfile.app -t eventpro:latest .'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true,
                  testResults:'target/surefile-reports/*.xml'

            archiveArtifacts (
                  artifacts: 'target/*.jar,target/site/jacoco/**',
                  allowEmptyArchive: true,
                  fingerprint: true
            )
        }
    }
}

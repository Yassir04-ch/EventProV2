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

                junit testResults: 'target/surefire-reports/TEST-*.xml'
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

        stage('Docker Build & Push') {
            when {
                branch 'master'
            }
            steps {
                withCredentials([
                        usernamePassword(
                                credentialsId: 'dockerhub-eventpro',
                                usernameVariable: 'DOCKERHUB_USER',
                                passwordVariable: 'DOCKERHUB_TOKEN'
                        )
                ]) {
                    sh '''
                         echo "$DOCKERHUB_TOKEN" | docker login \
                            -u "$DOCKERHUB_USER" --password-stdin

                         docker build -f Dockerfile.app \
                            -t yassircherqui/eventpro:1.0.0 \
                            -t yassircherqui/eventpro:latest .

                        docker push yassircherqui/eventpro:1.0.0
                        docker push yassircherqui/eventpro:latest

                        docker logout
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts (
                  artifacts: 'target/*.jar,target/site/jacoco/**',
                  allowEmptyArchive: true,
                  fingerprint: true
            )
        }
    }
}

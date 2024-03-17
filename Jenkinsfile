pipeline {
    agent any

    environment {
        // 환경 변수 설정
        EC2_IP = '' // EC2 인스턴스 IP 주소를 Jenkins 설정에서 환경 변수로 설정
        DOCKER_CREDENTIALS_ID = 'DaitdaJenkinscredentialsidDOCKER' // Docker Hub Credentials ID를 Jenkins 설정에서 환경 변수로 설정
        SSH_CREDENTIALS_ID = 'DaitdaPrivateKeyId' // SSH Private Key의 Credentials ID를 Jenkins 설정에서 환경 변수로 설정
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("DatidaJenkinsDockerapp:${BUILD_NUMBER}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("DatidaJenkinsDockerapp:${BUILD_NUMBER}").push()
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    sshagent(["${SSH_CREDENTIALS_ID}"]) {
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@${EC2_IP} 'docker run -d -p 80:8080 DatidaJenkinsDockerapp:${BUILD_NUMBER}'"
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Cleaning up...'
        }
    }
}
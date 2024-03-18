pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                script {
                    // Maven Docker 이미지를 사용하여 Maven 빌드 실행
                    docker.image('maven:3.6.3-jdk-11').inside {
                        sh 'mvn clean package'
                    }
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // 현재 디렉토리에 있는 Dockerfile을 사용하여 Docker 이미지 빌드
                    // "DatidaJenkinsDockerapp:${BUILD_NUMBER}"는 이미지 태그입니다.
                    docker.build("DatidaJenkinsDockerapp:${BUILD_NUMBER}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    // Docker Hub에 이미지 푸시
                    // "${DOCKER_CREDENTIALS_ID}"는 Jenkins에 설정된 Docker Hub의 credentials ID입니다.
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("DatidaJenkinsDockerapp:${BUILD_NUMBER}").push()
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // SSH를 사용하여 EC2 인스턴스에서 Docker 컨테이너 실행
                    // "${SSH_CREDENTIALS_ID}"는 Jenkins에 설정된 SSH 접속 정보의 credentials ID입니다.
                    // "${EC2_IP}"는 대상 EC2 인스턴스의 IP 주소입니다.
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

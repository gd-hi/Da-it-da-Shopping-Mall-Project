pipeline {
    agent any
    
    tools {
        // Maven 설치 이름을 정의합니다.
        maven 'Maven_3_8_4'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                script {
                    // Maven 설치 이름을 사용하지 않고 직접 Maven 명령 실행
                    // Jenkins가 Maven을 올바르게 설치하고 PATH에 추가했다면, 이 명령어는 작동해야 합니다.
                    sh "mvn clean package"
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    // 현재 디렉토리에 있는 Dockerfile을 사용하여 Docker 이미지 빌드
                    // "DatidaJenkinsDockerapp:${BUILD_NUMBER}"는 이미지 태그입니다.
                    docker.build("gdhi/datidajenkinsdockerapp:${BUILD_NUMBER}")
                }
            }
        }
        stage('Push Docker Image') {
            steps {
                script {
                    // Docker Hub에 이미지 푸시
                    // "${DOCKER_CREDENTIALS_ID}"는 Jenkins에 설정된 Docker Hub의 credentials ID입니다.
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("gdhi/datidajenkinsdockerapp:${BUILD_NUMBER}").push()
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
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@52.78.29.160 'docker run -d -p 80:8080 gdhi/datidaJenkinsDockerapp:${BUILD_NUMBER}'"
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

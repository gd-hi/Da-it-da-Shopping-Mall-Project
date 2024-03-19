pipeline {
    agent any
    
    tools {
        // Maven 설치 이름을 정의합니다.
        maven 'Maven_3_8_4'
    }
    
    stages {
        // 소스 코드를 SCM(Git 등)에서 체크아웃
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        // Maven을 사용하여 Java 애플리케이션을 빌드하고 패키징
        stage('Build') {
            steps {
                script {
                    // .jar 파일 생성
                    sh "mvn clean package"
                }
            }
        }
        // Dockerfile을 사용하여 Docker 이미지를 빌드
        stage('Build Docker Image') {
            steps {
                script {
                    // 이미지 태그는 Jenkins 빌드 번호를 사용
                    docker.build("gdhi/datidajenkinsdockerapp:${BUILD_NUMBER}")
                }
            }
        }
        // 생성된 Docker 이미지를 Docker Hub에 푸시
        stage('Push Docker Image') {
            steps {
                script {
                    //  Jenkins의 credentials를 사용하여 Docker Hub 인증을 수행
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS_ID}") {
                        docker.image("gdhi/datidajenkinsdockerapp:${BUILD_NUMBER}").push()
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                script {
                    // SSH를 사용하여 EC2 인스턴스에 접속한 후, 새로 푸시된 Docker 이미지를 바탕으로 Docker 컨테이너를 실행
                    // 보안을 위해 Jenkins의 관리 메뉴에서 설정하고, 파이프라인에서는 ID를 참조하여 사용
                    sshagent(["${SSH_CREDENTIALS_ID}"]) {
                        sh "ssh -o StrictHostKeyChecking=no ec2-user@${SERVER_IP} 'docker run -d -p 80:8080 gdhi/datidajenkinsdockerapp:${BUILD_NUMBER}'"
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

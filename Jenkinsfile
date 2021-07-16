pipeline {
    agent any
    environment{
		registry = 'dikshasingla/inventory'
		docker_port = 7100
		username = 'dikshasingla'
		container_exist = "${bat(script:'docker ps -q -f name=c-${username}-master', returnStdout: true).trim().readLines().drop(1).join("")}"
	}
    stages {
        stage ('Clean workspace') {
          steps {
            cleanWs()
          }
        }
        stage ('git') {
          steps {
            git 'https://github.com/dikshasingla2015/inventory-system.git'
          }
        }
        stage ('build') {
          steps {
            bat "mvn clean compile"
          }
        }
        stage ('Test: Unit Test') {
          steps {
            bat "mvn test"
          }
        }
        stage('SonarQube analysis') {
            steps{
                withSonarQubeEnv(installationName: 'Test_Sonar') {
                  bat "mvn clean install sonar:sonar -Dsonar.projectKey=sonar-dikshasingla -Dsonar.name=sonar-dikshasingla -Dsonar.login=5a4ef9631762fcc4e0a40db2afa95885752afdfb"
                }
            }
        }
        stage('Docker image'){
			steps{
				echo "Docker Image Step"
				bat "mvn install"
				bat "docker build -t i-${username}-master --no-cache -f Dockerfile ."
			}
		}
		stage('Move image to docker hub'){
			steps{
				echo "Move Image to docker hub"
				bat "docker tag i-${username}-master ${registry}:${BUILD_NUMBER}"
				withDockerRegistry([credentialsId: 'DockerHub',url:""]){
				    bat "docker push ${registry}:${BUILD_NUMBER}"
				}
			}
		}
		stage('Docker Deployment'){
			steps{
			    script {
                    echo "c-${username}-master container already exist with container id = ${env.container_exist}"
                    if (env.container_exist != null) {
                        echo "Deleting existing c-${username}-master container"
                        bat "docker stop c-${username}-master && docker rm c-${username}-master"
                    }
                    echo "Docker Deployment"
                    bat "docker run --name c-${username}-master -d -p 7100:3515 ${registry}:${BUILD_NUMBER}"
                }
            }
		}
    }
}
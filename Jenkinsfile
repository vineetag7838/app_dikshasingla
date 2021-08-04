pipeline{
    agent any
    tools {
        maven 'Maven3' 
    }
    environment{
        registry = 'dikshasingla/inventory'
        docker_port = null
        username = 'dikshasingla'
        container_name = "c-${username}-${BRANCH_NAME}"
        container_exist = "${bat(script:"docker ps -a -q -f name=${env.container_name}", returnStdout: true).trim().readLines().drop(1).join("")}"
    }
    options{
        timestamps()
        timeout(time:1,unit:'HOURS')
        skipDefaultCheckout()
        buildDiscarder(logRotator(
            numToKeepStr:'3',
            daysToKeepStr:'15'
        ))
    }
    stages{
        stage('Checkout'){
            steps{
                echo "Checkout from git repository for branch - ${BRANCH_NAME}"
                checkout scm
                script{
                    if (BRANCH_NAME == 'master') {
                        docker_port = 7200
                    } else {
                        docker_port = 7300
                    }
                }
            }
        }
        stage('Build'){
            steps{
                echo "Running build ${JOB_NAME} # ${BUILD_NUMBER} with docker port as ${docker_port}"
                echo "Build Step"
                bat "mvn clean compile"
            }
        }
        stage('Sonar Analysis') {
            when {
                branch 'develop'
            }
            steps{
                echo "Start sonarqube analysis step"
                withSonarQubeEnv(installationName: 'Test_Sonar') {
                    bat "mvn clean install sonar:sonar -Dsonar.projectKey=sonar-dikshasingla -Dsonar.name=sonar-dikshasingla -Dsonar.login=5a4ef9631762fcc4e0a40db2afa95885752afdfb"
                }
            }
        }
        stage('Unit Testing') {
            when {
                branch 'master'
            }
            steps{
                echo "Unit Testing Step"
                bat "mvn test"
            }
        }
        stage('Docker Image'){
            steps{
                echo "Docker Image Step"
                bat "mvn install"
                bat "docker build -t i-${username}-${BRANCH_NAME}:${BUILD_NUMBER} --no-cache -f Dockerfile ."
            }
        }
        stage('Containers') {
            steps{
                parallel(
                    "PrecontainerCheck": {
                        script {
                            echo "check if ${env.container_name} already exist with container id = ${env.container_exist}"
                            if (env.container_exist != null) {
                                echo "deleting existing ${env.container_name} container"
                                bat "docker stop ${env.container_name} && docker rm ${env.container_name}"
                            }
                        }
                    },
                    "Push to Docker Hub": {
                        script{
                            echo "Push to Docker Hub"
                            bat "docker tag i-${username}-${BRANCH_NAME}:${BUILD_NUMBER} ${registry}:${BRANCH_NAME}-${BUILD_NUMBER}"
							bat "docker tag i-${username}-${BRANCH_NAME}:${BUILD_NUMBER} ${registry}:latest-${BRANCH_NAME}"
                            withDockerRegistry([credentialsId:'DockerHub',url:""]){
                                bat "docker push ${registry}:${BRANCH_NAME}-${BUILD_NUMBER}"
								bat "docker push ${registry}:latest-${BRANCH_NAME}"
                            }
                        }
                    }
                )
            }
        }
        stage('Docker Deployment'){
            steps{
                echo "Docker Deployment"
                bat "docker run --name ${env.container_name} -d -p ${docker_port}:3515 ${registry}:${BRANCH_NAME}-${BUILD_NUMBER}"
            }
        }
        stage('Kubernetes Deployment'){
            steps{
                echo "Kubernetes Deployment"
                withCredentials([file(credentialsId: 'TestJenkinsApi', variable: 'TOKEN')]) {
                    bat 'kubectl apply -f deployment.yaml --token $TOKEN'
                    bat 'kubectl apply -f service.yaml --token $TOKEN'
                }
            }
        }
    }
}
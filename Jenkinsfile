pipeline{
    agent any
    environment{
        registry = 'dikshasingla/inventory'
        properties = null
        docker_port = null
        username = 'dikshasingla'
        project_id = 'testjenkinsapi-321216'
        cluster_name = 'java-api'
        location = 'us-central1-c'
        credentials_id = 'TestJenkinsApi'
        container_exist = "${bat(script:'docker ps -a -q -f name=c-dikshasingla-master', returnStdout: true).trim().readLines().drop(1).join("")}"
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
                git 'https://github.com/dikshasingla2015/inventory-system.git'
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
                echo "Running build ${JOB_NAME} # ${BUILD_NUMBER} for ${properties['user.employeeid']} with docker as ${docker_port}"
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
                            echo "check if c-${username}-master already exist with container id = ${env.container_exist}"
                            if (env.container_exist != null) {
                                echo "deleting existing c-${username}-master container"
                                bat "docker stop c-${username}-master && docker rm c-${username}-master"
                            }
                        }
                    },
                    "Push to Docker Hub": {
                        script{
                            echo "Push to Docker Hub"
                            bat "docker tag i-${username}-${BRANCH_NAME}:${BUILD_NUMBER} ${registry}:${BUILD_NUMBER}"
							bat "docker tag i-${username}-${BRANCH_NAME}:${BUILD_NUMBER} ${registry}:latest"
                            withDockerRegistry([credentialsId:'DockerHub',url:""]){
                                bat "docker push ${registry}:${BUILD_NUMBER}"
								bat "docker push ${registry}:latest"
                            }
                        }
                    }
                )
            }
        }
        stage('Docker Deployment'){
            steps{
                echo "Docker Deployment"
                bat "docker run --name c-${username}-master -d -p ${docker_port}:3515 ${registry}:${BUILD_NUMBER}"
            }
        }
        stage('Kubernetes Deployment'){
            steps{
                echo "Kubernetes Deployment"
                step([$class:'KubernetesEngineBuilder',projectId:env.project_id,clusterName:env.cluster_name,location:env.location,manifestPattern:'deployment.yaml',credentialsId:env.credentials_id,verifyDeployments:false])
            }
        }
    }
}
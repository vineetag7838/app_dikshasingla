pipeline{
    agent any
    environment{
        registry = 'dikshasingla/inventory'
        properties = null
        docker_port = null
        username = 'dikshasingla'
        container_exist = null
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
        stage('Start'){
            steps{
                git 'https://github.com/dikshasingla2015/inventory-system.git'
            }
        }
        stage('Build'){
            steps{
                echo "Running build ${JOB_NAME} # ${BUILD_NUMBER} for ${properties['user.employeeid']} with docker as ${docker_port}"
                echo "Build Step"
                bat "mvn clean compile"
            }
        }
        stage('Unit Testing'){
            steps{
                echo "Unit Testing Step"
                bat "mvn test"
            }
        }
        stage('Docker Image'){
            steps{
                echo "Docker Image Step"
                bat "mvn install"
                bat "docker build -t i-${username}-master --no-cache -f Dockerfile ."
            }
        }
        stage('Containers') {
            parallel(
                "PrecontainerCheck": {
                    steps{
                        script {
                            container_exist = "${bat(script:'docker ps -q -f name=c-dikshasingla-master', returnStdout: true).trim().readLines().drop(1).join("")}"
                            echo "check if c-${username}-master already exist with container id = ${env.container_exist}"
                            if (env.container_exist != null) {
                                echo "deleting existing c-${username}-master container"
                                bat "docker stop c-${username}-master && docker rm c-${username}-master"
                            }
                        }
                    }
                },
                "Push to Docker Hub": {
                    steps{
                        echo "Push to Docker Hub"
                        bat "docker tag i_${user_name}_master ${registry}:${BUILD_NUMBER}"
                        withDockerRegistry([credentialsId:'DockerHub',url:""]){
                            bat "docker push ${registry}:${BUILD_NUMBER}"
                        }
                    }
                }
            )
        }
        stage('Docker Deployment'){
            steps{
                echo "Docker Deployment"
                bat "docker run --name c-${username}-master -d -p 7200:3515 ${registry}:${BUILD_NUMBER}"
            }
        }
        stage('End'){
            steps{
                echo "Build End"
            }
        }
    }
}
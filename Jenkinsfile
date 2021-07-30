pipeline{
    agent any
    environment{
        registry = 'dikshasingla/inventory'
        properties = null
        docker_port = null
        username = 'dikshasingla'
		branch = null
		cname = 'c-${username}-master'
        container_exist = "${bat(script:'docker ps -a -q -f name=env.cname', returnStdout: true).trim().readLines().drop(1).join("")}"
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
                bat "docker build -t i-${username}-master --no-cache -f Dockerfile ."
            }
        }
        stage('Containers') {
            steps{
                parallel(
                    "PrecontainerCheck": {
						steps{
							bat "echo "${cname}""
						}
						environment {
							containerId = bat(script:'docker ps -a -q -f name=env.cname', returnStdout: true)
						}
						when {
							expression {
								return containerId != null
							}
						}
						steps {
							echo "check if env.cname already exist"
							echo "Stopping running container - env.cname"
							bat "docker stop env.cname && docker rm env.cname"
						}
                    },
                    "Push to Docker Hub": {
                        script{
                            echo "Push to Docker Hub"
                            bat "docker tag i-${username}-master ${registry}:${BUILD_NUMBER}"
                            withDockerRegistry([credentialsId:'DockerHub',url:""]){
                                bat "docker push ${registry}:${BUILD_NUMBER}"
                            }
                        }
                    }
                )
            }
        }
        stage('Docker Deployment'){
            steps{
                echo "Docker Deployment"
                bat "docker run --name c-${username}-master -d -p 7200:3515 ${registry}:${BUILD_NUMBER}"
            }
        }
    }
}
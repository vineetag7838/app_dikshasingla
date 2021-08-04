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
        stage('Kubernetes Deployment'){
            steps{
                echo "Kubernetes Deployment"
                bat 'kubectl apply -f deployment.yaml'
                bat 'kubectl apply -f service.yaml'
            }
        }
    }
}
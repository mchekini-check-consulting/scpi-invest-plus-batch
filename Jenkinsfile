node("ci-node"){
    def GIT_COMMIT_HASH = ""

    stage("Checkout"){
        checkout scm
        GIT_COMMIT_HASH = sh (script: "git log -n 1 --pretty=format:'%H'", returnStdout: true)
    }

    stage("Build Jar file"){
        sh "chmod 777 mvnw && ./mvnw package -DskipTests"
    }

    stage("Build Docker Image"){
        sh "sudo docker build -t mchekini/scpi-invest-plus-batch:$GIT_COMMIT_HASH ."
    }

    stage("Push Docker image"){
        withCredentials([usernamePassword(credentialsId: 'mchekini', passwordVariable: 'password', usernameVariable: 'username')]) {
            sh "sudo docker login -u $username -p $password"
            sh "sudo docker push mchekini/scpi-invest-plus-batch:$GIT_COMMIT_HASH"
            sh "sudo docker rmi mchekini/scpi-invest-plus-batch:$GIT_COMMIT_HASH"
        }
    }
}